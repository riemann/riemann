(ns riemann.time
  "Clocks and scheduled tasks. Provides functions for getting the current time
  and running functions (Tasks) at specific times and periods. Includes a
  threadpool for task execution, controlled by (start!) and (stop!)."
  (:import [java.util.concurrent ConcurrentSkipListSet]
           [java.util.concurrent.locks LockSupport])
  (:use [clojure.math.numeric-tower :only [ceil]]
        [clojure.stacktrace         :only [print-stack-trace]]
        [clojure.tools.logging      :only [warn]]))

(defprotocol Task
  (succ [task]
    "The successive task to this one.")
  (run [task]
    "Executes this task.")
  (cancel [task]
    "Cancel this task."))

(defprotocol Deferrable
  (defer [this new-time]
    "Schedule a task for a new time."))

(defn unix-time-real
  "The current unix epoch time in seconds, taken from
  System/currentTimeMillis."
  []
  (/ (System/currentTimeMillis) 1000))

(defn linear-time-real
  "A current time on a linear scale with no fixed epoch; counts in seconds.
  Unlike unix-time, which can pause, skip, or flow backwards, advances
  consistently at (close) to wall clock time."
  []
  (/ (System/nanoTime) 1000000000))

(def unix-time unix-time-real)
(def linear-time linear-time-real)

(defrecord Once [id f t cancelled]
  Task
  (succ [this] nil)
  (run [this] (when-not @cancelled (f)))
  (cancel [this]
          (reset! cancelled true)))

(defrecord Every [id f t interval deferred-t cancelled]
  Task
  (succ [this]
        (when-not @cancelled
          (let [next-time (or @deferred-t (+ t interval))]
            (reset! deferred-t nil)
            (assoc this :t next-time))))

  (run [this]
       (when-not (or @deferred-t @cancelled) (f)))

  (cancel [this]
          (reset! cancelled true))

  Deferrable
  (defer [this delay]
              (reset! deferred-t (+ (unix-time) delay))))

(def max-task-id
  (atom 0))

(def ^ConcurrentSkipListSet tasks
  "Scheduled operations."
  (ConcurrentSkipListSet.
    (fn [a b] (compare [(:t a) (:id a)]
                       [(:t b) (:id b)]))))

; Processor-dependent threadpool sizing will happen. It's on the list. ;-)
(def thread-count 4)
(def park-interval 0.1)
(def threadpool (atom []))
(def running (atom false))

(defn task-id
  "Return a new task ID."
  []
  (swap! max-task-id inc))

(defn next-tick
  "Given a period dt, beginning at some point in time anchor, finds the next
  tick after time now, such that the next tick is separate from anchor by an
  exact multiple of dt. If now is omitted, defaults to (unix-time)."
  ([anchor dt]
   (next-tick anchor dt (unix-time)))
  ([anchor dt now]
   (+ now (- dt (mod (- now anchor) dt)))))

; Look at all these bang! methods! Mutability is SO EXCITING!

(defn reset-tasks!
  "Resets the task queue to empty, without triggering side effects."
  []
  (.clear tasks))

(defn poll-task!
  "Removes the next task from the queue."
  []
  (.pollFirst tasks))

(defn schedule-sneaky!
  "Schedules a task. Does *not* awaken any threads."
  [task]
  (.add tasks task)
  task)

(defn schedule!
  "Schedule a task. May awaken a thread from the threadpool to investigate."
  [task]
  (schedule-sneaky! task)
  (when @running
    (LockSupport/unpark (rand-nth @threadpool)))
  task)

(defn once!
  "Calls f at t seconds."
  [t f]
  (schedule! (Once. (task-id) f t (atom false))))

(defn after!
  "Calls f after delay seconds"
  [delay f]
  (once! (+ (unix-time) delay) f))

(defn every!
  "Calls f every interval seconds, after delay."
  ([interval f]
   (every! interval 0 f))
  ([interval delay f]
   (schedule! (Every. (task-id)
                      f
                      (+ (unix-time) delay)
                      interval
                      (atom nil)
                      (atom false)))))

(defn run-tasks!
  "While running, takes tasks from the queue and executes them when ready. Will
  park the current thread when no tasks are available."
  [i]
  (while @running
    (try
      (if-let [task (poll-task!)]
        ; We've acquired a task.
        (if (<= (:t task) (unix-time-real))
          (do
            ; Run task
            (try
              (run task)
              (catch Throwable t
                (warn t "running task threw")))
            (when-let [task' (succ task)]
              ; Schedule the next task.
              (schedule-sneaky! task')))
          (do
            ; Return task.
            (schedule-sneaky! task)
            ; Park until that task comes up next.
            (LockSupport/parkUntil (ceil (* 1000 (:t task))))))
        (do
          ; No task available; park for a bit and try again.
          (LockSupport/parkNanos (ceil (* 1000000000 park-interval)))))
      (catch Throwable t
        (warn t "riemann.time task threw")))))

(defn stop!
  "Stops the task threadpool. Waits for threads to exit."
  []
  (locking threadpool
    (reset! running false)
    (while (some #(.isAlive ^Thread %) @threadpool)
      ; Allow at most 1/10th park-interval to pass after all threads exit.
      (Thread/sleep (* park-interval 100)))
    (reset! threadpool [])))

(defn start!
  "Starts the threadpool to execute tasks on the queue automatically."
  []
  (locking threadpool
    (stop!)
    (reset! running true)
    (reset! threadpool
            (map (fn [i]
                   (let [^Runnable f (bound-fn [] (run-tasks! i))]
                     (doto (Thread. f (str "riemann task " i))
                       (.start))))
                 (range thread-count)))))
