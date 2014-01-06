(ns riemann.time.controlled
  "Provides controllable periodic and deferred execution. Calling (advance!
  delta-in-seconds) moves the clock forward, triggering events that would have
  occurred, in sequence."
  (:use riemann.time
        clojure.math.numeric-tower))

(def clock
  "Reference to the current time, in seconds."
  (atom nil))

(defn reset-clock!
  []
  (reset! clock 0))

(defn reset-time!
  "Resets the clock and task queue. If a function is given, calls f after
  resetting the time and task list."
  ([f] (reset-time!) (f))
  ([]
   (reset-clock!)
   (reset-tasks!)))

(defn set-time!
  "Sets the current time, without triggering callbacks."
  [t]
  (reset! clock t))

(defn unix-time-controlled
  []
  @clock)

(defn linear-time-controlled
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
              (schedule-sneaky! task'))
            (recur))
          ; Return task
          (schedule-sneaky! task))))
    (swap! clock max t)))

(defn control-time!
  "Switches riemann.time functions to time.controlled counterparts, invokes f,
  then restores them. Definitely not threadsafe. Not safe by any standard,
  come to think of it. Only for testing purposes."
  [f]
  ; Please forgive me.
  (with-redefs [riemann.time/unix-time unix-time-controlled
                riemann.time/linear-time linear-time-controlled]
    (f)))
