(ns riemann.service
  "Lifecycle protocol for stateful services bound to a core."
  (:require wall.hack
            riemann.instrumentation)
  (:use clojure.tools.logging
        [riemann.time :only [unix-time]])
  (:import (riemann.instrumentation Instrumented)
           (java.util.concurrent TimeUnit
                                 ThreadFactory
                                 AbstractExecutorService
                                 Executor
                                 ExecutorService
                                 BlockingQueue
                                 LinkedBlockingQueue
                                 RejectedExecutionException
                                 ArrayBlockingQueue
                                 SynchronousQueue
                                 ThreadPoolExecutor)))

(defprotocol Service
  "Services are components of a core with a managed lifecycle. They're used for
  stateful things like connection pools, network servers, and background
  threads."
  (reload! [service core]
          "Informs the service of a change in core.")
  (start! [service]
          "Starts a service. Must be idempotent.")
  (stop!  [service]
         "Stops a service. Must be idempotent.")
  (conflict? [service1 service2]
             "Do these two services conflict with one another? Adding
             a service to a core *replaces* any conflicting services."))

(defprotocol ServiceEquiv
  (equiv? [service1 service2]
          "Used to identify which services can remain running through a core
          transition, like reloading. If the old service is equivalent to the
          new service, the old service may be preserved and used by the new
          core. Otherwise, the old service may be shut down and replaced by
          the new."))

(defrecord ThreadService [name equiv-key f core running thread]
  ServiceEquiv
  (equiv? [this other]
          (and
            (instance? ThreadService other)
            (= name (:name other))
            (= equiv-key (:equiv-key other))))

  Service
  (conflict? [this other]
             (and
               (instance? ThreadService other)
               (= name (:name other))))

  (reload! [this new-core]
           (reset! core new-core))

  (start! [this]
          (locking this
            (when-not @running
              (reset! running true)
              (let [t (Thread. (fn thread-service-runner []
                                 (while @running
                                   (try
                                     (f @core)
                                     (catch InterruptedException e
                                       :interrupted)))))]
                (reset! thread t)
                (.start t)))))

  (stop! [this]
         (locking this
           (when @running
             (reset! running false)
             (.interrupt ^Thread @thread)
             ; Wait for exit
             (while (.isAlive ^Thread @thread)
               (Thread/sleep 5))))))

(defn thread-service
  "Returns a ThreadService which will call (f core) repeatedly when started.
  Will only stop between calls to f. Start and stop are blocking operations.
  Equivalent to other ThreadServices with the same name and equivalence key--
  if not provided, defaults nil. Conflicts with other ThreadServices of the
  same name."
  ([name f]
   (thread-service name nil f))
  ([name equiv-key f]
   (ThreadService. name equiv-key f (atom nil) (atom false) (atom nil))))

(defmacro all-equal?
  "Takes two objects to compare and a list of forms to compare them by.

  (all-equiv? foo bar
    (class)
    (foo 2)
    (.getSize))

  becomes

  (let [a foo
        b bar]
    (and (= (class a) (class b))
         (= (foo 2 a) (foo 2 b))
         (= (.getSize a) (.getSize b))))"
  [a b & forms]
  (let [asym (gensym "a__")
        bsym (gensym "b__")]
  `(let [~asym ~a
         ~bsym ~b]
     (and ~@(map (fn [[fun & args]]
                  `(= (~fun ~asym ~@args) (~fun ~bsym ~@args)))
                forms)))))

; Wraps an ExecutorService with a start/stop lifecycle
(defprotocol IExecutorServiceService
  (getExecutor [this]))

(deftype ExecutorServiceService
  [name equiv-key f ^:volatile-mutable ^ExecutorService executor stats]

  IExecutorServiceService
  (getExecutor [this] executor)

  ServiceEquiv
  (equiv? [a b]
          (all-equal? a ^ExecutorServiceService b
                      (class)
                      (.name)
                      (.equiv-key)))

  Service
  (conflict? [a b]
             (all-equal? a ^ExecutorServiceService b
                         (class)
                         (.name)))

  (reload! [this new-core])

  (start! [this]
          (locking this
            (when-not executor
              (info "Executor Service" name "starting")
              (let [x              (f)
                    queue-capacity (if (instance? ThreadPoolExecutor x)
                                     (.remainingCapacity
                                       (.getQueue ^ThreadPoolExecutor x))
                                     Integer/MAX_VALUE)]
                (reset! stats {:accepted        0
                               :completed       0
                               :rejected        0
                               :time            (unix-time)
                               :queue-capacity  queue-capacity})
                (set! executor x)))))

  (stop! [this]
         (locking this
           (when executor
             (info "Executor Service" name "stopping")
             (.shutdown executor)
             (set! executor nil))))

  Executor
  (execute [this runnable]
           (if-let [x executor]
             (try
               (.execute x runnable)
               (catch RejectedExecutionException e
                 ; Update rejected stats and rethrow
                 (swap! stats (fn [stats] (assoc stats :rejected
                                                 (inc (:rejected stats)))))
                 (throw e)))

             (throw (RejectedExecutionException.
                      (str "ExecutorServiceService " name
                           " isn't running.")))))

  Instrumented
  (events [this]
    (when (instance? ThreadPoolExecutor executor)
      (let [time               (unix-time)
            tasks-completed    (.getCompletedTaskCount executor)
            tasks-accepted     (.getTaskCount executor)
            {:keys [queue-capacity
                    dcompleted
                    daccepted
                    drejected
                    dtime]}    (swap!
                                 stats
                                 (fn [stats]
                                   (merge stats
                                          {:dcompleted (- tasks-completed
                                                          (:completed stats))
                                           :completed  tasks-completed

                                           :daccepted  (- tasks-accepted
                                                          (:accepted stats))
                                           :accepted   tasks-accepted

                                           :drejected  (:rejected stats)
                                           :rejected   0

                                           :dtime      (- time (:time stats))
                                           :time       time})))
            threads-active  (.getActiveCount executor)
            queue-size      (max 0 (- tasks-accepted
                                      tasks-completed
                                      threads-active))
            queue-used      (/ queue-size queue-capacity)
            queue-used-state (condp < queue-used
                               3/4 "critical"
                               1/2 "warning"
                               "ok")
            s (partial str "riemann executor " (clojure.core/name name) " ")]
        [{:service (s "accepted rate")
          :metric  (/ daccepted dtime)
          :state   "ok"
          :time    time}
         {:service (s "completed rate")
          :metric  (/ dcompleted dtime)
          :state   "ok"
          :time    time}
         {:service (s "rejected rate")
          :metric  (/ drejected dtime)
          :state   (if (pos? drejected) "warning" "ok")
          :time    time}
         {:service (s "queue capacity")
          :metric  queue-capacity
          :state   "ok"
          :time    time}
         {:service (s "queue size")
          :metric  queue-size
          :state   queue-used-state
          :time    time}
         {:service (s "queue used")
          :metric  queue-used
          :state   queue-used-state
          :time    time}
         {:service (s "threads active")
          :metric  threads-active
          :state   "ok"
          :time    time}
         {:service (s "threads alive")
          :metric  (.getPoolSize executor)
          :state   "ok"
          :time    time}]))))

(defn executor-service
  "Creates a new threadpool executor service ... service! Takes a function
  which generates an ExecutorService. Returns an ExecutorServiceService which
  provides start/stop/reload/equiv? lifecycle management of that service.

  Equivalence-key controls how services are compared to tell if they are
  equivalent. Services with a nil equivalence key are *never* equivalent.
  Otherwise, services are equivalent when their class, name, and equiv-key are
  equal.

  (executor-service* :graphite {foo: 4}
    #(ThreadPoolExecutor. 2 ...))"
  ([name f] (executor-service name nil f))
  ([name equiv-key f]
   (ExecutorServiceService. name equiv-key f nil (atom nil))))

(defmacro literal-executor-service
  "Like executor-service, but captures the *expression* passed to it as the
  equivalence key. This only works if the expression is literal; if there are
  any variables or function calls, they will be compared as code, not as
  their evaluated values.

  OK:  (literal-executor-service :io (ThreadPoolExecutor. 2 ...))
  OK:  (literal-executor-service :io (ThreadPoolExecutor. (inc 1) ...))
  BAD: (literal-executor-service :io (ThreadPoolExecutor. x ...))"
  [name executor-service-expr]
  `(executor-service
     ~name
     (quote ~executor-service-expr)
     (fn [] ~executor-service-expr)))

(defn threadpool-service
  "An ExecutorServiceService based on a ThreadPoolExecutor with core and
  maximum threadpool sizes, and a LinkedBlockingQueue of a given size. Options:

  :core-pool-size             Default 0
  :max-pool-size              Default 4
  :keep-alive-time            Default 5
  :keep-alive-unit            Default SECONDS
  :queue-size                 Default 1000"
  ([name] (threadpool-service name {}))
  ([name {:keys [core-pool-size
                 max-pool-size
                 keep-alive-time
                 keep-alive-unit
                 queue-size]
          :as opts
          :or {core-pool-size 1
               max-pool-size 128
               keep-alive-time 10
               keep-alive-unit TimeUnit/MILLISECONDS
               queue-size 1000}}]
   (executor-service
     name
     (merge opts {:type `threadpool-service})
     #(ThreadPoolExecutor.
        core-pool-size
        max-pool-size
        keep-alive-time
        keep-alive-unit
        (LinkedBlockingQueue. ^int queue-size)))))
