(ns riemann.service
  "Lifecycle protocol for stateful services bound to a core."
  (:require wall.hack)
  (:import (java.util.concurrent TimeUnit
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
         "Stops a service. Must be idempotent."))

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
  (reload! [this new-core]
           (reset! core new-core))

  (start! [this]
          (locking this
            (when-not @running
              (reset! running true)
              (let [t (Thread. (fn thread-service-runner []
                                 (while @running
                                   (f @core))))]
                (reset! thread t)
                (.start t)))))

  (stop! [this]
         (locking this
           (when @running
             (reset! running false)
             ; Wait for exit
             (while (.isAlive ^Thread @thread)
               (Thread/sleep 5))))))

(defn thread-service 
  "Returns a ThreadService which will call (f core) repeatedly when started.
  Will only stop between calls to f. Start and stop are blocking operations.
  Equivalent to other ThreadServices with the same name and equivalence key--
  if not provided, defaults nil."
  ([name f]
   (thread-service name nil f))
  ([name equiv-key f]
   (ThreadService. name equiv-key f (atom nil) (atom false) (atom nil))))

(defmacro all-equiv?
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
                  `(equiv? (~fun ~asym ~@args) (~fun ~bsym ~@args)))
                forms)))))

; I'm doing my best to cover the reasonable equivalence classes required to
; compare ThreadPoolExecutor dynamics with common classes, but I've taken some
; shortcuts. If you mess around with ThreadFactories or deep internals, these
; may not compare correctly.
(extend-protocol ServiceEquiv
  nil
  (equiv? [a b] (nil? b))

  Object
  (equiv? [a b] (= a b))

  ThreadFactory
  (equiv? [a b] (= (class a) (class b))) ; punt!

  BlockingQueue
  (equiv? [a b] false) ; punt!

  SynchronousQueue
  (equiv? [a b] (= (class a) (class b)))

  LinkedBlockingQueue
  (equiv? [a b] 
          (all-equiv? a b
                      (class)
                      (->> (wall.hack/field LinkedBlockingQueue :capacity))))

  ArrayBlockingQueue
  (equiv? [a b] (all-equiv? a b
                            (class)
                            (->> 
                              ^objects (wall.hack/field 
                                         ArrayBlockingQueue :items)
                              (alength))))

  ExecutorService
  (equiv? [a b] false) ; punt!

  ThreadPoolExecutor
  (equiv? [a ^ThreadPoolExecutor b]
          (and
            (all-equiv? a b
                          (class)
                          (.getCorePoolSize)
                          (.getMaximumPoolSize)
                          (.getKeepAliveTime TimeUnit/NANOSECONDS)
                          (.getRejectedExecutionHandler)
                          (.getThreadFactory))
               (equiv? (.getQueue a)
                       (.getQueue b)))))

; Wraps an ExecutorService with a start/stop lifecycle
(defprotocol IExecutorServiceService
  (getExecutor [this]))

(deftype ExecutorServiceService
  [name f ^:volatile-mutable ^ExecutorService executor]

  IExecutorServiceService
  (getExecutor [this] executor)
  
  ServiceEquiv
  (equiv? [a b]
          (and 
            (all-equiv? a ^ExecutorServiceService b
                        (class)
                        (.name))
            (equiv? (getExecutor a)
                    (getExecutor b))))

  Service
  (reload! [this new-core])

  (start! [this]
          (locking this
            (set! executor (f))))

  (stop! [this]
         (locking this
           (when executor
             (.shutdown executor)
             (set! executor nil))))

  Executor
  (execute [this runnable]
           (if-let [x executor]
             (.execute x runnable)
             (throw (RejectedExecutionException.
                      (str "ExecutorServiceService "
                           " isn't running."))))))
             

(defn threadpool-executor-service
  "Creates a new threadpool executor service ... service! Takes a function
  which generates an ExecutorService. Returns an ExecutorServiceService which
  provides start/stop/reload/equiv? lifecycle management of that service."
  [name f]
  (ExecutorServiceService. name f nil))
