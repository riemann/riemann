(ns riemann.time.controlled
  "Provides controllable periodic and deferred execution. Calling (advance!
  delta-in-seconds) moves the clock forward, triggering events that would have
  occurred, in sequence."
  (:import [java.util.concurrent ConcurrentSkipListSet]))

(defprotocol Task
  (succ [task] "The successive task to this one.")

  (run [task] "Executes this task.")

  (cancel [task] "Cancel this task."))

(defprotocol Deferrable
  (defer [this new-time] "Schedule a task for a new time."))

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
  (defer [this t]
              (reset! deferred-t t)))

(def max-task-id
  (atom 0))

(def clock
  "Reference to the current time, in seconds." 
  (atom nil))

(def tasks
  "Scheduled operations."
  (ConcurrentSkipListSet. 
    (fn [a b] (compare [(:t a) (:id a)]
                       [(:t b) (:id b)]))))

(defn task-id
  "Return a new task ID."
  []
  (swap! max-task-id inc))

; Look at all these bang! methods! Mutability is SO EXCITING!

(defn reset-tasks!
  "Resets the clock to zero and clears the schedule, without triggering side
  effects."
  []
  (.clear tasks))

(defn reset-clock!
  []
  (reset! clock 0))

(defn reset-time!
  []
  (reset-clock!)
  (reset-tasks!))

(defn poll-task!
  "Removes the next task from the queue."
  []
  (.pollFirst tasks))

(defn set-time!
  "Sets the current time, without triggering callbacks."
  [t]
  (reset! clock t))

(defn schedule!
  "Schedule a task."
  [task]
  (.add tasks task)
  task)

(defn once! 
  "Run f, one time, at time t. Returns a Task."
  [f t]
  (schedule! (Once. (task-id) f t (atom false))))

(defn every! 
  "Runs f every t seconds. Returns a Deferable Task."
  [f t interval]
  (schedule! (Every. (task-id) f t interval (atom nil) (atom false))))

(defn now
  "Returns the current time."
  []
  @clock)

(defn advance! 
  "Advances the clock to t seconds, triggering side effects."
  [t]
  (when (< @clock t)
    (loop []
      (when-let [task (poll-task!)]
        (if (<= (:t task) t)
          (do
            ; Consume task
            (swap! clock max (:t task))
            (run task)
            (when-let [task' (succ task)]
              (schedule! task'))
            (recur))
          ; Quietly return task
          (schedule! task))))
    (swap! clock max t)))
