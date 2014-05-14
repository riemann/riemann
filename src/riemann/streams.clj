(ns riemann.streams
  "The streams namespace aims to provide a comprehensive set of widely
  applicable, combinable tools for building more complex streams.

  Streams are functions which accept events or, in some cases, lists of events.

  Streams typically do one or more of the following.

  * Filter events.
  * Transform events.
  * Combine events over time.
  * Apply events to other streams.
  * Forward events to other services.

  Most streams accept, after their initial arguments, any number of streams as
  children. These are known as children or \"child streams\" of the stream.
  The children are typically invoked sequentially, any exceptions thrown are
  caught, logged and optionally forwarded to *exception-stream*.
  Return values of children are ignored.

  Events are backed by a map (e.g. {:service \"foo\" :metric 3.5}), so any
  function that accepts maps will work with events.
  Common functions like prn can be used as a child stream.

  Some common patterns for defining child streams are (fn [e] (println e))
  and (partial log :info)."
  (:use [riemann.common :exclude [match]]
        [riemann.time :only [unix-time
                             linear-time
                             every!
                             once!
                             after!
                             next-tick
                             defer
                             cancel]]
        clojure.math.numeric-tower
        clojure.tools.logging)
  (:require [riemann.folds :as folds]
            [riemann.index :as index]
            riemann.client
            riemann.logging
            [clojure.set :as set])
  (:import (java.util.concurrent Executor)))

(def  infinity (/  1.0 0))
(def -infinity (/ -1.0 0))

(def ^:dynamic *exception-stream*
  "When an exception is caught, it's converted to an event and sent here."
  nil)

(defn expired?
  "There are two ways an event can be considered expired.
  First, if it has state \"expired\".
  Second, if its :ttl and :time indicates it has expired."
  [event]
    (or (= (:state event) "expired")
        (when-let [time (:time event)]
          (let [ttl (or (:ttl event) index/default-ttl)
                age (- (unix-time) time)]
            (> age ttl)))))

(defmacro call-rescue
  "Call each child (children), in order, with event.
  Rescues and logs any failure."
  [event children]
  `(do
     (doseq [child# ~children]
       (try
         (child# ~event)
         (catch Throwable e#
           (warn e# (str child# " threw"))
           (if-let [ex-stream# *exception-stream*]
             (ex-stream# (exception->event e#))))))
     ; TODO: Why return true?
     true))

(defmacro exception-stream
  "Catches exceptions, converts them to events, and sends those events to a
  special exception stream.

  (exception-stream (email \"polito@vonbraun.com\")
    (async-queue! :graphite {:core-pool-size 128}
      graph))

  Streams often take multiple children and send an event to each using
  call-rescue. Call-rescue will rescue any exception thrown by a child stream,
  log it, and move on to the next child stream, so that a failure in one child
  won't prevent others from executing.

  Exceptions binds a dynamically scoped thread-local variable
  *exception-stream*. When call-rescue encounters an exception, it will *also*
  route the error to this exception stream. When switching threads (e.g. when
  using an executor or Thread), you
  must use (bound-fn) to preserve this binding.

  This is a little more complex than you might think, because we *not only*
  need to bind this variable during the runtime execution of child streams, but
  *also* during the evaluation of the child streams themselves, e.g. at the
  invocation time of exceptions itself. If we write

  (exception-stream (email ...)
    (rate 5 index))

  then (rate), when invoked, might need access to this variable immediately.
  Therefore, this macro binds *exception-stream* twice: one when evaluating
  children, and again, every time the returned stream is invoked."
  [exception-stream & children]
  `(let [ex-stream# ~exception-stream
         children#  (binding [*exception-stream* ex-stream#]
                      (list ~@children))]
     (fn stream# [event#]
       (binding [*exception-stream* ex-stream#]
         (call-rescue event# children#)))))

(defn bit-bucket
  "Discards arguments."
  [args])

(defn dual
  "A stream which splits events into two mirror-images streams, based on
  (pred e).
  If (pred e) is true, calls (true-stream e) and (false-stream (expire e)).
  If (pred e) is false, does the opposite. Expired events are forwarded to both
  streams.

  (pred e) is always called once per incoming event."
  [pred true-stream false-stream]
  (fn stream [event]
    (let [value (pred event)]
      (cond
        (expired? event)
        (call-rescue event [true-stream false-stream])

        value
        (do
          (call-rescue (expire event) [false-stream])
          (call-rescue event [true-stream]))

        :else
        (do
          (call-rescue (expire event) [true-stream])
          (call-rescue event [false-stream]))))))

(defn smap*
  "Streaming map: less magic. Calls children with (f event).
  Unlike smap, passes on nil results to children. Example:

  (smap folds/maximum prn) ; Prints the maximum of lists of events."
  [f & children]
  (fn stream [event]
    (call-rescue (f event) children)))

(defn smap
  "Streaming map. Calls children with (f event), whenever (f event) is non-nil.
  Prefer this to (adjust f) and (combine f). Example:

  (smap :metric prn) ; prints the metric of each event.
  (smap #(assoc % :state \"ok\") index) ; Indexes each event with state \"ok\""
  [f & children]
  (fn stream [event]
    (let [value (f event)]
      (when-not (nil? value)
        (call-rescue value children)))))

(defn combine
  "Returns a function which takes a seq of events.
  Combines events with f, then forwards the result to children."
  [f & children]
  (deprecated "combine is deprecated in favor of smap or smap*"
              (apply smap* f children)))

(defn smapcat
  "Streaming mapcat. Calls children with each event in (f event), which should
  return a sequence. For instance, to set the state of any services with
  metrics deviating from the mode to \"warning\", one might use coalesce to
  aggregate all services, and smapcat to find the mode and assoc the proper
  states; emitting a series of individual events to the index.

  (coalesce
    (smapcat (fn [events]
               (let [freqs (frequencies (map :metric events))
                     mode  (apply max-key freqs (keys freqs))]
                 (map #(assoc % :state (if (= mode (:metric %))
                                         \"ok\" \"warning\"))
                      events)))
      index))"
  [f & children]
  (fn stream [event]
    (doseq [e (f event)]
      (call-rescue e children))))

(defn sreduce
  "Streaming reduce. Two forms:

  (sreduce f child1 child2 ...)
  (sreduce f val child1 child2 ...)

  Maintains an internal value, which defaults to the first event received or,
  if provided, val. When the stream receives an event, calls (f val event) to
  produce a new value, which is sent to each child. f *must* be free of side
  effects. Examples:

  Passes on events, but with the *maximum* of all received metrics:
  (sreduce (fn [acc event] (assoc event :metric
                                  (max (:metric event) (:metric acc)))) ...)

  Or, using riemann.folds, a simple moving average:
  (sreduce (fn [acc event] (folds/mean [acc event])) ...)"
  [f & opts]
  (if (fn? (first opts))
    ; No value provided
    (let [children   opts
          first-time (ref true)
          acc        (ref nil)]
      (fn stream [event]
        (let [[first-time value] (dosync
                                   (if @first-time
                                     (do
                                       (ref-set first-time false)
                                       (ref-set acc event)
                                       [true nil])
                                     [false (alter acc f event)]))]
          (when-not first-time
            (call-rescue value children)))))

    ; Value provided
    (let [acc      (atom (first opts))
          children (rest opts)]
      (fn stream [event]
        (call-rescue (swap! acc f event) children)))))

(defn sdo
  "Takes a list of functions f1, f2, f3, and returns f such that (f event)
  calls (f1 event) (f2 event) (f3 event). Useful for binding several streams to
  a single variable.

  (sdo prn (rate 5 index))"
  [& children]
  (fn stream [event]
    (call-rescue event children)))

(defn stream
  [& args]
  (deprecated "riemann.streams/stream is now streams/sdo."
              (apply sdo args)))

(defn execute-on
  "Returns a stream which accepts events and executes them using a
  java.util.concurrent.Executor. Returns immediately. May throw
  RejectedExecutionException if the underlying executor will not accept the
  event; e.g. if its queue is full. Use together with
  riemann.service/executor-service for reloadable asynchronous execution of
  streams. See also: async-queue!, which may be simpler.

  (let [io-pool (service!
                  (executor-service
                    #(ThreadPoolExecutor. 1 10 ...)))
        graph (execute-on io-pool (graphite {:host ...}))]
    ...
    (tagged \"graph\"
      graph))"
  [^Executor executor & children]
  (fn stream [event]
    (.execute executor
             (bound-fn runner []
               (call-rescue event children)))))

(defn moving-event-window
  "A sliding window of the last few events. Every time an event arrives, calls
  children with a vector of the last n events, from oldest to newest. Ignores
  event times. Example:

  (moving-event-window 5 (smap folds/mean index))"
  [n & children]
  (let [window (atom (vec []))]
    (fn stream [event]
      (let [w (swap! window (fn swap [w]
                              (vec (take-last n (conj w event)))))]
        (call-rescue w children)))))

(defn fixed-event-window
  "Passes on fixed-size windows of n events each. Accumulates n events, then
  calls children with a vector of those events, from oldest to newest. Ignores
  event times. Example:

  (fixed-event-window 5 (smap folds/mean index))"
  [n & children]
  (let [buffer (atom [])]
    (fn stream [event]
      (let [events (swap! buffer (fn swap [events]
                              (let [events (conj events event)]
                                (if (< n (count events))
                                  [event]
                                  events))))]
        (when (= n (count events))
          (call-rescue events children))))))

(defn moving-time-window
  "A sliding window of all events with times within the last n seconds. Uses
  the maximum event time as the present-time horizon. Every time a new event
  arrives within the window, emits a vector of events in the window to
  children.

  Events without times accrue in the current window."
  [n & children]
  (let [cutoff (ref 0)
        buffer (ref [])]
    (fn stream [event]
      (let [events (dosync
                     ; Compute minimum allowed time
                     (let [cutoff (alter cutoff max (- (get event :time 0) n))]
                       (when (or (nil? (:time event))
                                 (< cutoff (:time event)))
                         ; This event belongs in the buffer, and our cutoff may
                         ; have changed.
                         (alter buffer conj event)
                         (alter buffer
                                (fn alter [events]
                                  (vec (filter
                                         (fn [e] (or (nil? (:time e))
                                                     (< cutoff (:time e))))
                                         events)))))))]
        (when events
          (call-rescue events children))))))

(defn fixed-time-window
  "A fixed window over the event stream in time. Emits vectors of events, such
  that each vector has events from a distinct n-second interval. Windows do
  *not* overlap; each event appears at most once in the output stream. Once an
  event is emitted, all events *older or equal* to that emitted event are
  silently dropped.

  Events without times accrue in the current window."
  [n & children]
  ; This is not a particularly inspired or clear implementation. :-(

  (when (zero? n)
    (throw (IllegalArgumentException. "Can't have a zero-width time window.")))

  (let [start-time (ref nil)
        buffer     (ref [])]
    (fn stream [event]
      (let [windows (dosync
                      (cond
                        ; No time
                        (nil? (:time event))
                        (do
                          (alter buffer conj event)
                          nil)

                        ; No start time
                        (nil? @start-time)
                        (do
                          (ref-set start-time (:time event))
                          (ref-set buffer [event])
                          nil)

                        ; Too old
                        (< (:time event) @start-time)
                        nil

                        ; Within window
                        (< (:time event) (+ @start-time n))
                        (do
                          (alter buffer conj event)
                          nil)

                        ; Above window
                        true
                        (let [delta (- (:time event) @start-time)
                              dstart (- delta (mod delta n))
                              empties (dec (/ dstart n))
                              windows (conj (repeat empties []) @buffer)]
                          (alter start-time + dstart)
                          (ref-set buffer [event])
                          windows)))]
        (when windows
          (doseq [w windows]
            (call-rescue w children)))))))


(defn window
  "Alias for moving-event-window."
  [n & children]
  (apply moving-event-window n children))

; On my MBP tops out at around 300K
; events/sec. Experimental benchmarks suggest that:
(comment (time
             (doseq [f (map (fn [t] (future
               (let [c (ref 0)]
                 (dotimes [i (/ total threads)]
                         (let [e {:metric 1 :time (unix-time)}]
                           (dosync (commute c + (:metric e))))))))
                            (range threads))]
               (deref f))))
; can do something like 1.9 million events/sec over 4 threads.  That suggests
; there's a space for a faster (but less correct) version which uses either
; agents or involves fewer STM ops. Assuming all events have local time might
; actually be more useful than correctly binning/expiring multiple times.
; Also: broken?
(defn- part-time-fn [interval create add finish]
  ; All intervals are [start, end)
  (let [; The oldest time we are allowed to flush rates for.
        watermark (ref 0)
        ; A list of all bins we're tracking.
        bins (ref {})
        ; Eventually finish a bin.
        finish-eventually (fn finish-eventually [bin start]
                            (let [f (bound-fn thread []
                                      (let [end (+ start interval)]
                                        ; Sleep until this bin is past
                                        (Thread/sleep
                                          (max 0 (* 1000 (- end (unix-time)))))
                                        ; Prevent anyone else from creating or
                                        ; changing this bin. Congratulations,
                                        ; you've invented timelocks.
                                        (dosync
                                          (alter bins dissoc start)
                                          (alter watermark max end))
                                        ; Now that we're safe from
                                        ; modification, finish him!
                                        (finish bin start end)))]
                              (.start (Thread. ^Runnable f))))

        ; Add event to the bin for a time
        bin (fn bin [event t]
              (let [start (quot t interval)]
                (dosync
                  (when (<= (deref watermark) start)
                    ; We are accepting this event.
                    ; Return an existing table
                    ; or create and store a new one
                    (let [current ((deref bins) start)]
                      (if current
                        ; Use current
                        (add current event)
                        ; Create new
                        (let [bin (create event)]
                          (alter bins assoc start bin)
                          (finish-eventually bin start))))))))]

    (fn stream [event]
      (let [; What time did this event happen at?
            t (or (:time event) (unix-time))]
        (bin event t)))))

(defn periodically-until-expired
  "When an event arrives, begins calling f every interval seconds. Starts after
  delay. Stops calling f when an expired? event arrives, or the most recent
  event expires."
  ([f] (periodically-until-expired 1 0 f))
  ([interval f] (periodically-until-expired interval 0 f))
  ([interval delay f]
   (let [task (atom nil)
         expires-at (atom infinity)
         f (fn wrapper []
             (if (< @expires-at (unix-time))
               ; Expired
               (when-let [t @task]
                 (cancel t)
                 (reset! task nil))
               ; We're still valid; keep going
               (f)))]

     (fn stream [event]
       ; Bump the time we're allowed to keep running for.
       (if (and (:ttl event) (:time event))
         ; We have a fixed TTL
         (reset! expires-at (+ (:time event) (:ttl event)))
         ; Run forever
         (reset! expires-at infinity))

       (if (expired? event)
         ; Stop periodic.
         (when-let [t @task]
           (cancel t)
           (reset! task nil))

         ; Start if necessary
         (when-not @task
           ; Note that we lock the periodic atom to prevent the STM from
           ; retrying our thread-creating transaction more than once. Double
           ; nil? check allows us to avoid synchronizing *every* event at the
           ; cost of a race condition over extremely short timescales. As those
           ; timescales are likely to have undefined ordering *anyway*, I don't
           ; really care about getting this particular part right.
           (locking task
             (when-not @task
               (reset! task (every! interval delay f))))))))))

(defn part-time-fast
  "Partitions events by time (fast variant). Each <interval> seconds, creates a
  new bin by calling (create). Applies each received event to the current bin
  with (add bin event). When the time interval is over, calls (finish bin
  start-time elapsed-time).

  Concurrency guarantees:

  (create) may be called multiple times for a given time slice.
  (add)    when called, will receive exactly one distinct bucket in each time
           slice.
  (finish) will be called *exactly once* for each time slice."
  [interval create add finish]
  (let [state (atom nil)
        ; Set up an initial bin and start time.
        setup (fn part-time-fast-setup []
                (swap! state #(or % {:start (unix-time)
                                    :current (create)})))

        ; Switch to the next bin, finishing the current one.
        switch (bound-fn switch []
                 (apply finish
                        (locking state
                          (when-let [s @state]
                            (let [bin (:current s)
                                  old-start (:start s)
                                  boundary (unix-time)]
                              (reset! state {:start boundary
                                             :current (create)})
                              [bin old-start boundary])))))

        ; Switch bins every interval
        p (periodically-until-expired interval interval switch)]

    (fn part-time-fast' [event]
      (p event)
      (cond
        ; The event's expired
        (expired? event)
        (locking state
          (reset! state nil))

        ; We have a current bin
        @state
        (add (:current @state) event)

        ; Create an initial bin
        :else
        (do
          (setup)
          (recur event))))))

(defn part-time-simple
  "Divides wall clock time into discrete windows. Returns a stream, composed
  of four functions:

  (reset previous-state) Given the state for the previous window, returns a
  fresh state for a new window. Reset must be a pure function, as it will be
  invoked in a compare-and-set loop. Reset may be invoked at *any* time. Reset
  will be invoked with nil when no previous state exists.

  (add state event) is called every time an event arrives to *combine* the
  event and the state together, returning some new state. Merge must be a pure
  function.

  (side-effects state event) is called with the *resulting* state and the event
  which just arrived, but will be called only once per event, and can be
  impure. Its return value is used for the return value of the stream.

  (finish state start-time end-time) is called once at the end of each time
  window, and receives the final state for that window, and also the start
  and end times for that window. Finish will be called exactly once per window,
  and may be impure.

  When no events arrive in a given time window, no functions are called."
  ([dt reset add finish]
   (part-time-simple dt reset add (fn [state event]) finish))
  ([dt reset add side-effects finish]
   (let [anchor (unix-time)
         state (atom {:window (reset nil)})

         ; Called every dt seconds to flush the window.
         tick (fn tick []
                (let [last-state (atom nil)
                      ; Swap out the current state
                      _ (swap! state (fn [state]
                                       (reset! last-state state)
                                       {:window (reset (:window state))}))
                      s @last-state]
                  ; And finalize the last window
                  (finish (:window s) (:start s) (:end s))))]

     (fn stream [event]
       ; Race to claim the first write to this window
       (let [state (swap! state (fn [state]
                                  ; Add the event to our window.
                                  (let [window (:window state)
                                        state (assoc state :window
                                                     (add window event))]
                                    (case (:scheduled state)
                                      ; We're the first ones here.
                                      nil (let [end (next-tick anchor dt)]
                                            (merge state
                                                   {:scheduled :first
                                                    :start (- end dt)
                                                    :end end}))

                                      ; Someone else just claimed
                                      :first (assoc state :scheduled :done)

                                      ; No change
                                      :done state))))]

         (when (= :first (:scheduled state))
           ; We were the first thread to update this window.
           (once! (:end state) tick))

         ; Side effects
         (side-effects (:window state) event))))))

(defn fold-interval
  "Applies the folder function to all event-key values of events during
  interval seconds."
  [interval event-key folder & children]
  (part-time-fast interval
                  (fn create [] (atom []))
                  (fn add [r event]
                    (when-let [ek (event-key event)]
                      (swap! r conj event)))
                  (fn finish [r start end]
                    (let [events @r
                          stat  (folder (map event-key events))
                          event (assoc (last events) event-key stat)]
                      (call-rescue event children)))))

(defn fold-interval-metric
  "Wrapping for fold-interval that assumes :metric as event-key."
  [interval folder & children]
  (apply fold-interval interval :metric folder children))

(defn fill-in
  "Passes on all events. Fills in gaps in event stream with copies of the given
  event, wherever interval seconds pass without an event arriving. Inserted
  events have current time. Stops inserting when expired. Uses local times."
  ([interval default-event & children]
   (let [fill (bound-fn fill []
                (call-rescue (assoc default-event :time (unix-time)) children))
         new-deferrable (fn new-deferrable [] (every! interval
                                                      interval
                                                      fill))
         deferrable (atom (new-deferrable))]
    (fn stream [event]
      (let [d (deref deferrable)]
        (if d
          ; We have an active deferrable
          (if (expired? event)
            (do
              (cancel d)
              (reset! deferrable nil))
            (defer d interval))
          ; Create a deferrable
          (when-not (expired? event)
            (locking deferrable
              (when-not (deref deferrable)
                (reset! deferrable (new-deferrable)))))))

      ; And forward
      (call-rescue event children)))))

(defn fill-in-last
  "Passes on all events. Fills in gaps in event stream with copies of the last
  event merged with the given data, wherever interval seconds pass without an
  event arriving. Inserted events have current time. Stops inserting when
  expired. Uses local times."
  ([interval update & children]
   (let [last-event (atom nil)
         fill (bound-fn fill []
                (call-rescue (merge @last-event update {:time (unix-time)}) children))
         new-deferrable (fn new-deferrable []
                          (every! interval interval fill))
         deferrable (atom nil)]
     (fn stream [event]
       ; Record last event
       (reset! last-event event)

       (let [d (deref deferrable)]
         (if d
           ; We have an active deferrable
           (if (expired? event)
             (do
               (cancel d)
               (reset! deferrable nil))
             (defer d interval))
           ; Create a deferrable
           (when-not (expired? event)
             (locking deferrable
               (when-not (deref deferrable)
                 (reset! deferrable (new-deferrable)))))))

       ; And forward
       (call-rescue event children)))))

(defn interpolate-constant
  "Emits a constant stream of events every interval seconds, starting when an
  event is received, and ending when an expired event is received. Times are
  set to Riemann's time. The first and last events are forwarded immediately.

  Note: ignores event times currently--will change later."
  [interval & children]
    (let [state (atom nil)
          emit-dup (bound-fn emit-dup []
                     (call-rescue
                       (assoc (deref state) :time (unix-time))
                       children))
          peri (periodically-until-expired interval emit-dup)]
      (fn stream [event]
        (reset! state event)

        (peri event)
        (when (expired? event)
          (call-rescue event children)
          ; Clean up
          (reset! state nil)
          ))))

(defn ddt-real
  "(ddt) in real time."
  [n & children]
  (let [state (atom (list nil))  ; Events at t3, t2, and t1.
        swap (bound-fn swap []
               (let [[_ e2 e1] (swap! state
                                      (fn swap [[e3 e2 e1 :as state]]
                                        ; If no events have come in this
                                        ; interval, we preserve the last event
                                        ; in both places, which means we emit
                                        ; zeroes.
                                        (if e3
                                          (list nil e3 e2)
                                          (list nil e2 e2))))]
                 (when (and e1 e2)
                   (let [dt (- (:time e2) (:time e1))
                         out (merge e2
                                    (if (zero? dt)
                                      {:time (unix-time)
                                       :metric 0}
                                      (let [diff (/ (- (:metric e2)
                                                       (:metric e1))
                                                    dt)]
                                        {:time (unix-time)
                                         :metric diff})))]
                     (call-rescue out children)))))
        poller (periodically-until-expired n swap)]

    (fn stream [event]
      (when (:metric event)
        (swap! state (fn swap [[most-recent & more]] (cons event more))))
      (poller event))))

(defn ddt-events
  "(ddt) between each pair of events."
  [& children]
  (let [prev (atom nil)]
    (fn stream [event]
      (when-let [m (:metric event)]
        (let [prev-event (let [prev-event @prev]
                           (reset! prev event)
                           prev-event)]
          (when prev-event
            (let [dt (- (:time event) (:time prev-event))]
              (when-not (zero? dt)
                (let [diff (/ (- m (:metric prev-event)) dt)]
                  (call-rescue (assoc event :metric diff) children))))))))))

(defn ddt
  "Differentiate metrics with respect to time. With no args, emits an event for
  each one received, but with metric equal to the difference between the
  current event and the previous one, divided by the difference in their times.
  If the first argument is a number n, emits a rate-of-change event every n
  seconds instead, until expired. Skips events without metrics."
  [& args]
  (if (number? (first args))
    (apply ddt-real args)
    (apply ddt-events args)))

(defn rate
  "Take the sum of every event's metric over interval seconds and divide by the
  interval size. Emits one event every interval seconds. Starts as soon as an
  event is received, stops when the most recent event expires. Uses the most
  recently received event with a metric as a template. Event ttls decrease
  constantly if no new events arrive."
  [interval & children]
  (assert (< 0 interval))
  (let [last-event (atom nil)
        sum (atom '(0 0))

        add-sum  (fn add-sum [[current previous] addend]
                              (list (+ current addend) previous))
        swap-sum (fn swap-sum [[current previous]]
                               (list 0 current))

        swap-event (fn swap-event [e sum]
                     (let [e (merge e {:metric (/ sum interval)
                                       :time   (unix-time)})]
                       (if-let [ttl (:ttl e)]
                         (assoc e :ttl (- ttl interval))
                         e)))

        tick (bound-fn tick []
               ; Get last metric
               (let [sum (second (swap! sum swap-sum))
                     event (swap! last-event swap-event sum)]
                 ; Forward event to children.
                 (call-rescue event children)))

        poller (periodically-until-expired interval interval tick)]

    (fn rate' [event]
      (when-let [m (:metric event)]
        ; TTLs decay by interval when emitted, so we add interval once.
        ; That way, incoming and outgoing TTLs, under constant event flow, are
        ; the same.
        (reset! last-event
                (if-let [ttl (:ttl event)]
                  (assoc event :ttl (+ ttl interval))
                  event))
        (swap! sum add-sum m))
      (poller event))))

(defn percentiles
  "Over each period of interval seconds, aggregates events and selects one
  event from that period for each point. If point is 0, takes the lowest metric
  event.  If point is 1, takes the highest metric event. 0.5 is the median
  event, and so forth. Forwards each of these events to children. The service
  name has the point appended to it; e.g. 'response time' becomes 'response
  time .95'."
  [interval points & children]
  (part-time-fast interval
                (fn setup [] (atom []))
                (fn add [r event] (swap! r conj event))
                (fn finish [r start end]
                  (let [samples (folds/sorted-sample @r points)]
                    (doseq [event samples] (call-rescue event children))))))

(defn counter
  "Counts things. The first argument may be an initial counter value, which
  defaults to zero.

  ; Starts at zero
  (counter index)

  ; Starts at 500
  (counter 500 index)

  Events without metrics are passed through unchanged. Events with metrics
  increment the counter, and are passed on with their metric set to the current
  count.

  You can reset the counter by passing it an event with a metric, tagged
  \"reset\"; the count will be reset to that metric."
  [& children]
  (let [counter (atom (if (number? (first children))
                        (first children)
                        0))
        children (if (number? (first children))
                   (rest children)
                   children)]
    (fn stream [event]
      (if-let [metric (:metric event)]
        (do
          (if (member? "reset" (:tags event))
            (reset! counter metric)
            (swap! counter + metric))
          (call-rescue (assoc event :metric @counter) children))
        (call-rescue event children)))))

(defn sum-over-time
  "Sums all metrics together. Emits the most recent event each time this
  stream is called, but with summed metric."
  [& children]
  (deprecated "Use streams/counter"
              (let [sum (atom 0)]
                (fn stream [event]
                  (let [s (when-let [m (:metric event)]
                            (swap! sum + (:metric event)))
                        event (assoc event :metric s)]
                    (call-rescue event children))))))

(defn mean-over-time
  "Emits the most recent event each time this stream is called, but with the
  average of all received metrics."
  [children]
  (deprecated "Use streams/ewma-timeless"
              (let [sum (ref nil)
                    total (ref 0)]
                (fn stream [event]
                  (let [m (dosync
                            (let [t (commute total inc)
                                  s (commute sum + (:metric event))]
                              (/ s t)))
                        event (assoc event :metric m)]
                    (call-rescue event children))))))

(defn ewma-timeless
  "Exponential weighted moving average. Constant space and time overhead.
  Passes on each event received, but with metric adjusted to the moving
  average. Does not take the time between events into account. R is the ratio
  between successive events: r=1 means always return the most recent metric;
  r=1/2 means the current event counts for half, the previous event for 1/4,
  the previous event for 1/8, and so on."
  [r & children]
  (let [m (atom 0)
        c-existing (- 1 r)
        c-new r]
    (fn stream [event]
      ; Compute new ewma
      (let [m (when-let [metric-new (:metric event)]
                (swap! m (comp (partial + (* c-new metric-new))
                               (partial * c-existing))))]
        (call-rescue (assoc event :metric m) children)))))

(defn ewma
  "Exponential weighted moving average. Constant space and time overhead.
  Passes on each event received, but with metric adjusted to the moving
  average. Takes into account the time between events."
  [halflife & children]
  (let [m (atom {:metric 0})
        r (expt Math/E (/ (Math/log 1/2) halflife))
        c-existing r
        c-new (- 1 r)]
    (fn stream [event]
      ; Compute new ewma
      (swap! m (fn [x]
        (let [time-new (or (:time event) 0)
              time-old (or (:time x) time-new)
              time-diff (- time-new time-old)
              metric-old (:metric x)
              m-new (when-let [metric-new (:metric event)]
                (cond
                  (pos? time-diff)
                    (merge x {:time time-new
                              :metric (+ (* c-new metric-new)
                                         (* metric-old
                                            (expt c-existing time-diff)))})
                  (neg? time-diff)
                    (merge x {:time time-old
                              :metric (+ metric-old
                                         (* (* c-new metric-new)
                                            (expt c-existing
                                                  (Math/abs time-diff))))})
                  (zero? time-diff)
                    (merge x {:time time-old
                              :metric (+ metric-old
                                         (* c-new metric-new))})))]
              (call-rescue (merge event m-new) children)
              (or m-new x)))))))

(defn- top-update
  "Helper for top atomic state updates."
  [[smallest top] k f event]
  (let [value (f event)
        ekey [(:host event) (:service event)]
        scan (fn scan [top]
               (if (empty? top)
                 nil
                 (first (first (sort-by (comp f second) top)))))
        trim (fn trim [top smallest]
               (if (< k (count top))
                 [(dissoc top smallest) (top smallest)]
                 [top]))]
    (cond
      ; Expired or irrelevant event
      (or (expired? event)
          (nil? value))
      (let [top (dissoc top ekey)]
        (if (top smallest)
          [smallest top]
          [(scan top) top]))

      ; Empty set
      (nil? smallest)
      [ekey (assoc top ekey event)]

      ; Falls outside the top set.
      (and (not (top ekey))
           ((complement pos?) (compare value (f (top smallest))))
           (<= k (count top)))
      [smallest top]

      ; In the top set
      :else
      (let [[top out] (trim (assoc top ekey event) smallest)]
        (if (or (nil? (top smallest))
                (neg? (compare value (f (top smallest)))))
          [(scan top) top out]
          [smallest top out])))))

(defn top
  "Bifurcates a stream into a dual pair of streams: one for the top k events,
  and one for the bottom k events.

  f is a function which maps events to comparable values, e.g. numbers. If an
  incoming event e falls in the top k, the top stream receives e and the bottom
  stream receives (expire e). If the event is *not* in the top k, calls (top
  (expire e)) and (bottom e).

  If an inbound event is already expired, it is forwarded directly to both
  streams. In this way, both top- and bottom-stream have a consistent, dual
  view of the event space.

  Index the top 10 events, by metric:

  (top 10 :metric index)

  Index everything, but tag the top k events with \"top\":

  (top 10 :metric
    (tag \"top\" index)
    index)

  This implementation of top is lazy, in the sense that it won't proactively
  expire events which are bumped from the top-k set--you have to wait for
  another event with the same host and service to arrive before child streams
  will know it's expired."
  ([k f top-stream]
     (top k f top-stream bit-bucket nil))
  ([k f top-stream bottom-stream]
     (top k f top-stream bottom-stream true))
  ([k f top-stream bottom-stream demote?]
   (let [state (atom [nil {}])]
     (dual (fn stream [event]
             (let [[_ top out] (swap! state top-update k f event)]
               (when (top [(:host event) (:service event)])
                 (when (and out demote?)
                   (call-rescue (expire out) [top-stream])
                   (call-rescue out [bottom-stream]))
                 true)))
           top-stream
           bottom-stream))))

(defn throttle
  "Passes on n events every dt seconds. Drops events when necessary."
  [n dt & children]
  (part-time-simple
    dt
    (fn reset [_] 0)

    (fn add [sent event] (inc sent))

    (fn side-effects [sent event]
      (when-not (< n sent)
        (call-rescue event children)))

    (fn finish [sent start end])))

(defn rollup
  "Invokes children with events at most n times per dt second interval. Passes
  *vectors* of events to children, not a single event at a time. For instance,
  (rollup 3 1 f) receives five events and forwards three times per second:

  1 -> (f [1])
  2 -> (f [2])
  3 -> (f [3])
  4 ->
  5 ->

  ... and events 4 and 5 are rolled over into the next period:

    -> (f [4 5])"
  [n dt & children]
  (part-time-simple
    dt

    (fn reset [[sent buffer]]
      (if (empty? buffer)
        ; We didn't carry over any events from the last window
        [0 []]
        ; We did carry over events.
        [1 []]))

    (fn add [[sent buffer] event]
      (if (< sent n)
        [(inc sent) buffer]
        [(inc sent) (conj buffer event)]))

    (fn side-effects [[sent buffer] event]
      (when (<= sent n)
        (call-rescue [event] children)))

    (fn finish [[sent buffer] _ _]
      (when-not (empty? buffer)
        (call-rescue buffer children)))))

(defn batch
  "Batches up events into vectors, bounded both by size and by time. Once
  either n events have accumulated, or dt seconds passed, flushes the current
  batch to all child streams. Child streams should accept a sequence of
  events."
  [n dt & children]
  (part-time-simple dt
    ; First, the batch to conj onto. Second, a full batch, if ready
    (constantly [[] nil])

    ; Conj new elements into the batch, and spill over if necessary
    ; when we overflow.
    (fn add [[batch done] event]
      (let [batch (conj batch event)]
        (if (<= n (count batch))
          ; Full!
          [[] batch]
          ; Not yet
          [batch nil])))

    ; If we're full, flush the buffer early.
    (fn side-effects [[_ done] event]
      (when done (call-rescue done children)))

    ; And flush incomplete buffers once the time interval has elapsed
    (fn flush [[batch _] start-time end-time]
      (when (seq batch)
        (call-rescue batch children)))))

(defn coalesce-with-event
  "Helper for coalesce: calls (f current-event all-events) every time an event
  is received."
  [keyfn child]
  ; Past is [{keys -> events}, expired-events]
  (let [past (atom [{} []])]
    (fn stream [event]
      (let [ekey   (keyfn event)
            ; Updates the state map and delivers expired events to an atom.
            update (fn update [[ok _]]
                     (doall
                       (map persistent!
                            (reduce (fn part [[ok expired] [k v]]
                                      (if (expired? v)
                                        [ok (conj! expired v)]
                                        [(assoc! ok k v) expired]))
                                    [(transient {})
                                     (transient [])]
                                    (assoc ok ekey event)))))
            [ok expired] (swap! past update)]
        (child event (concat expired (vals ok)))))))

(defn coalesce
  "Combines events over time. Coalesce remembers the most recent event for each
  service/host combination that passes through it (limited by :ttl). Every dt
  seconds (default to 1 second), it passes on *all* events it remembers. When
  events expire, they are included in the emitted sequence of events *once*,
  and removed from the state table thereafter.

  Use coalesce to combine states that arrive at different times--for instance,
  to average the CPU use over several hosts.

  Every 10 seconds, print a sequence of events including all the events which
  share the same :foo and :bar attributes:

  (by [:foo :bar]
    (coalesce 10 prn))"
  [& [dt & children]]
  (let [children (if (number? dt) children (cons dt children))
        dt (if (number? dt) dt 1)
        chm (java.util.concurrent.ConcurrentHashMap.)
        callback (fn callback []
                   (let [es (vec (.values chm))
                         expired (filter expired? es)]
                     (doseq [e expired
                             :let [s (:service e)
                                   h (:host e)]]
                       (.remove chm [s h] e))
                     (call-rescue es children)))
        period-manager (periodically-until-expired dt callback)]
    (fn [e]
      (.put chm [(:service e) (:host e)] e)
      (period-manager e))))

(defn append
  "Conj events onto the given reference"
  [reference]
  (fn stream [event]
    (swap! reference conj event)))

(defn register
  "Set reference to the most recent event that passes through."
  [reference]
  (fn stream [event]
    (reset! reference event)))

(defn forward
  "Sends an event or a collection of events through a Riemann client."
  [client]
  (fn stream [es]
    (if (map? es)
      (riemann.client/send-event client es)
      (riemann.client/send-events client es))))

(defn match
  "Passes events on to children only when (f event) matches value, using
  riemann.common/match. For instance:

  (match :service nil prn)
  (match :state #{\"warning\" \"critical\"} prn)
  (match :description #\"error\" prn)
  (match :metric 5 prn)
  (match expired? true prn)
  (match (fn [e] (/ (:metric e) 1000)) 5 prn)

  For cases where you only care about whether (f event) is truthy, use (where
  some-fn) instead of (match some-fn true)."
  [f value & children]
  (fn stream [event]
    (when (riemann.common/match value (f event))
      (call-rescue event children)
      true)))

(defn tagged-all?
  "Predicate function to check if a collection of tags is
  present in the tags of event."
  [tags event]
  (set/subset? (set tags) (set (:tags event))))

(defn tagged-all
  "Passes on events where all tags are present. This stream returns true if an
  event it receives matches those tags, nil otherwise.

  Can be used as a predicate in a where form.

  (tagged-all \"foo\" prn)
  (tagged-all [\"foo\" \"bar\"] prn)"
  [tags & children]
  (let [tag-coll (flatten [tags])]
    (fn stream [event]
      (when (tagged-all? tag-coll event)
        (call-rescue event children)
        true))))

(defn tagged-any?
  "Predicate function to check if any of a collection of tags
  are present in the tags of event."
  [tags event]
  (not= nil (some (set tags) (:tags event))))

(defn tagged-any
  "Passes on events where any of tags are present. This stream returns true if
  an event it receives matches those tags, nil otherwise.

  Can be used as a predicate in a where form.

  (tagged-any \"foo\" prn)
  (tagged-all [\"foo\" \"bar\"] prn)"
  [tags & children]
  (let [tag-coll (flatten [tags])]
    (fn stream [event]
      (when (tagged-any? tag-coll event)
        (call-rescue event children)
        true))))

(def tagged "Alias for tagged-all" tagged-all)

(defn expired
  "Passes on events with :state \"expired\"."
  [& children]
  (apply match :state "expired" children))

(defn with
  "Constructs a copy of each incoming event with new values for the given keys,
  and passes the resulting event on to each child stream. As everywhere in
  Riemann, events are immutable; only this stream's children will see this
  version of the event.

  If you only want to set *default* values, use `default`. If you want to
  update values for a key based on the *current value* of that field in each
  event, use `adjust`. If you want to update events using arbitrary functions,
  use `smap`.

  ; Print each event, but with service \"foo\"
  (with :service \"foo\" prn)

  ; Print each event, but with no host and state \"broken\".
  (with {:host nil :state \"broken\"} prn)"
  [& args]
  (if (map? (first args))
    ; Merge in a map of new values.
    (let [[m & children] args]
      (fn stream [event]
        ;    Merge on protobufs is broken; nil values aren't applied.
        ;    (let [e (merge event m)]
        (let [e (reduce (fn [m, [k, v]]
                          (if (nil? v) (dissoc m k) (assoc m k v)))
                        event m)]
          (call-rescue e children))))

    ; Change a particular key.
    (let [[k v & children] args]
      (fn stream [event]
        ;    (let [e (assoc event k v)]
        (let [e (if (nil? v) (dissoc event k) (assoc event k v))]
          (call-rescue e children))))))

(defn default
  "Like `with`, but does not override existing (i.e. non-nil) values. Useful
  when you want to fill in default values for events that might come in without
  them.

  (default :ttl 300 index)
  (default {:service \"jrecursive\" :state \"chicken\"} index)"
  [& args]
  (if (map? (first args))
    ; Merge in a map of new values.
    (let [[defaults & children] args]
      (fn stream [event]
        ;    Merge on protobufs is broken; nil values aren't applied.
        (let [e (reduce (fn [m [k v]]
                          (if (nil? (get m k)) (assoc m k v) m))
                        event defaults)]
          (call-rescue e children))))

    ; Change a particular key.
    (let [[k v & children] args]
      (fn stream [event]
        (let [e (if (nil? (k event)) (assoc event k v) event)]
          (call-rescue e children))))))

(defn adjust
  "Passes on a changed version of each event by applying a function to a
  particular field or to the event as a whole.

  Passing a vector of [field function & args] to adjust will modify the given
  field in incoming events by applying the function to it along with the given
  arguments.  For example:

  (adjust [:service str \" rate\"] ...)

  takes {:service \"foo\"} and emits {:service \"foo rate\"}.

  If a function is passed to adjust instead of a vector, adjust behaves like
  smap: the entire event will be given to the function and the result will be
  passed along to the children. For example:

  (adjust #(assoc % :metric (count (:tags %))) ...)

  takes {:tags [\"foo\" \"bar\"]} and emits {:tags [\"foo\" \"bar\"] :metric 2}.

  Prefer (smap f & children) to (adjust f & children) where possible."
  [& args]
  (if (vector? (first args))
    ; Adjust a particular field in the event.
    (let [[[field f & args] & children] args]
      (fn stream [event]
        (let [value (apply f (field event) args)
              event (assoc event field value)]
          (call-rescue event children))))
    (apply smap (first args) (rest args))))

(defn scale
  "Passes on a changed version of each event by multiplying each
   metric with the given scale factor.

  ; Convert bytes to kilobytes
  (scale 1/1024 index)"
  [factor & children]
  (let [scale-event (fn [{:keys [metric] :as event}]
                      (assoc event :metric
                             (if metric (* metric factor))))]
    (apply smap scale-event children)))

(defn tag
  "Adds a new tag, or set of tags, to events which flow through.

  (tag \"foo\" index)
  (tag [\"foo\" \"bar\"] index)"
  [tags & children]
  (let [tags (flatten [tags])]
    (apply smap (fn stream [event]
                  (assoc event :tags
                         (distinct (concat tags (:tags event)))))
           children)))

(defmacro pipe
  "Sometimes, you want to have a stream split into several paths, then
  recombine those paths after some transformation. Pipe lets you write
  these topologies easily.

  We might express a linear stream in Riemann, in which a -> b -> c -> d, as

  (a (b (c d)))

  With pipe, we write

  (pipe ↧ (a ↧)
          (b ↧)
          (c ↧)
          d)

  The first argument ↧ is a *marker* for points where events should flow down
  into the next stage. A delightful list of marker symbols you might enjoy is
  available at http://www.alanwood.net/unicode/arrows.html.

  What makes pipe more powerful than the standard Riemann composition rules is
  that the marker may appear *multiple times* in a stage, and *at any depth in
  the expression*. For instance, we might want to categorize events based on
  their metric, and send all those events into the same throttled email stream.

  (let [throttled-emailer (throttle 100 1 (email \"ops@rickenbacker.mil\"))]
    (splitp < metric
      0.9 (with :state :critical throttled-emailer)
      0.5 (with :state :warning  throttled-emailer)
          (with :state :ok       throttled-emailer)))

  But with pipe, we can write:

  (pipe - (splitp < metric
                  0.9 (with :state :critical -)
                  0.5 (with :state :warning  -)
                      (with :state :ok       -))
          (throttle 100 1 (email \"ops@rickenbacker.mil\")))

  So pipe lets us do three things:

  0. *Flatten* a deeply nested expression, like Clojure's -> and ->>.

  1. *Omit or simplify* the names for each stage, when we care more about the
  *structure* of the streams than giving them full descriptions.

  2. Write the stream in the *order in which events flow*.

  Pipe rewrites its stages as a let binding in reverse order; binding each
  stage to the placeholder in turn. The placeholder must be a compile-time
  symbol, and obeys the usual let-binding rules about variable shadowing; you
  can rebind the marker lexically within any stage using let, etc. Yep, this is
  a swiss arrow in disguise; ssshhhhhhh. ;-)"
  [marker & stages]
  `(let [~@(->> stages
                reverse
                (interpose marker)
                (cons marker))]
         ~marker))

(defmacro by
  "Splits stream by field.
  Every time an event arrives with a new value of field, this macro invokes
  its child forms to return a *new*, distinct set of streams for that
  particular value.

  (rate 5 prn) prints a single rate for all events, once every five seconds.

  (by :host (rate 5) tracks a separate rate for *each host*, and prints each
  one every five seconds.

  You can pass multiple fields too

  (by [:host :service])

  Note that field can be a keyword like :host or :state, but you can *also* use
  any unary function for more complex sharding.

  Be aware that (by) over unbounded values can result in
  *many* substreams being created, so you wouldn't want to write
  (by metric prn): you'd get a separate prn for *every* unique metric that
  came in."
  [fields & children]
  ; new-fork is a function which gives us a new copy of our children.
  ; table is a reference which maps (field event) to a fork (or list of
  ; children).
  `(let [new-fork# (fn [] [~@children])]
     (by-fn ~fields new-fork#)))

(defn by-fn [fields new-fork]
  (let [fields (flatten [fields])
        f (if (= 1 (count fields))
            ; Just use the first function given applied to the event
            (first fields)
            ; Return a vec of *each* function given, applied to the event
            (apply juxt fields))
        table (atom {})]
     (fn stream [event]
       (let [fork-name (f event)
             fork (if-let [fork (@table fork-name)]
                    fork
                    ((swap! table assoc fork-name (new-fork)) fork-name))]
         (call-rescue event fork)))))

(defn changed
  "Passes on events only when (f event) differs from that of the previous
  event. Options:

  :init   The initial value to assume for (pred event).

  ; Print all state changes
  (changed :state prn)

  ; Assume states *were* ok the first time we see them.
  (changed :state {:init \"ok\"} prn)

  Note that f can be an arbitrary function:

  (changed (fn [e] (> (:metric e) 2)) ...)"
  [pred & children]
  (let [options  (first children)
        previous (atom (list (when (map? options)
                               (:init options))))
        children (if (map? options)
                   (rest children)
                   children)]
    (fn stream [event]
      (let [cur  (pred event)
            kept (swap! previous (comp (partial take 2)
                                       #(conj % cur)))]
        (when-not (every? (partial = cur) kept)
          (call-rescue event children))))))

(defmacro changed-state
  "Passes on changes in state for each distinct host and service."
  [& children]
  `(by [:host :service]
       (changed :state ~@children)))

(defn within
  "Passes on events only when their metric falls within the given inclusive
  range.

  (within [0 1] (fn [event] do-something))"
  [r & children]
  (deprecated "streams/within is deprecated; use (where (< x metric y))"
              (fn stream [event]
                (when-let [m (:metric event)]
                  (when (<= (first r) m (last r))
                    (call-rescue event children))))))

(defn without
  "Passes on events only when their metric falls outside the given (inclusive)
  range."
  [r & children]
  (deprecated "streams/without is deprecated; use (where (not (< x metric y)))"
              (fn stream [event]
                (when-let [m (:metric event)]
                  (when-not (<= (first r) m (last r))
                    (call-rescue event children))))))

(defn over
  "Passes on events only when their metric is greater than x"
  [x & children]
  (deprecated "streams/over is deprecated in favor of (where (< x metric))"
              (fn stream [event]
                (when-let [m (:metric event)]
                  (when (< x m)
                    (call-rescue event children))))))

(defn under
  "Passes on events only when their metric is smaller than x"
  [x & children]
  (deprecated "streams/under is deprecated in favor of (where (< metric x))"
              (fn stream [event]
                (when-let [m (:metric event)]
                  (when (> x m)
                    (call-rescue event children))))))

(defn- where-test [k v]
  (condp some [k]
    ; Tagged checks that v is a member of tags.
    #{'tagged 'tagged-all} (list 'when (list :tags 'event)
                             (list 'tagged-all? (list 'flatten [v]) 'event))
    #{'tagged-any} (list 'when (list :tags 'event)
                     (list 'tagged-any? (list 'flatten [v]) 'event))
    ; Otherwise, match.
    (list 'riemann.common/match v (list (keyword k) 'event))))

; Hack hack hack hack
(defn where-rewrite
  "Rewrites lists recursively. Replaces (metric x y z) with a test matching
  (:metric event) to any of x, y, or z, either by = or re-find. Replaces any
  other instance of metric with (:metric event). Does the same for host,
  service, event, state, time, ttl, tags (which performs an exact match of the
  tag vector), tagged (which checks to see if the given tag is present at all),
  metric_f, and description."
  [expr]
  (let [syms #{'host
               'service
               'state
               'metric
               'metric_f
               'time
               'ttl
               'description
               'tags
               'tagged
               'tagged-all
               'tagged-any}]
    (if (list? expr)
      ; This is a list.
      (if (syms (first expr))
        ; list starting with a magic symbol
        (let [[field & values] expr]
          (if (= 1 (count values))
            ; Match one value
            (where-test field (first values))
            ; Any of the values
            (concat '(or) (map (fn [value] (where-test field value)) values))))

        ; Other list
        (map where-rewrite expr))

      ; Not a list
      (if (syms expr)
        ; Expr *is* a magic sym
        (list (keyword expr) 'event)
        expr))))

(defn where-partition-clauses
  "Given expressions like (a (else b) c (else d)), returns [[a c] [b d]]"
  [exprs]
  (map vec
       ((juxt remove
              (comp (partial mapcat rest) filter))
          (fn [expr]
            (when (list? expr)
              (= 'else (first expr))))
          exprs)))

(defmacro where*
  "A simpler, less magical variant of (where). Instead of binding symbols in
  the context of an expression, where* takes a function which takes an event.
  When (f event) is truthy, passes event to children--and otherwise, passes
  event to (else ...) children. For example:

  (where* (fn [e] (< 2 (:metric e))) prn)

  (where* expired?
    (partial prn \"Expired\")
    (else
      (partial prn \"Not expired!\")))"
  [f & children]
  (let [[true-kids else-kids] (where-partition-clauses children)]
    `(let [true-kids# ~true-kids
           else-kids# ~else-kids]
      (fn stream# [event#]
         (let [value# (~f event#)]
           (if value#
             (call-rescue event# true-kids#)
             (call-rescue event# else-kids#))
           value#)))))

(defmacro where
  "Passes on events where expr is true. Expr is rewritten using where-rewrite.
  'event is bound to the event under consideration. Examples:

  ; Match any event where metric is either 1, 2, 3, or 4.
  (where (metric 1 2 3 4) ...)

  ; Match a event where the metric is negative AND the state is ok.
  (where (and (> 0 metric)
              (state \"ok\")) ...)

  ; Match a event where the host begins with web
  (where (host #\"^web\") ...)


  ; Match an event where the service is in a set of services
  (where (service #{\"service-foo\" \"service-bar\"}) ...)
  ; which is equivalent to
  (where (service \"service-foo\" \"service-bar\") ...)

  If a child begins with (else ...), the else's body is executed when expr is
  false. For instance:

  (where (service \"www\")
    (notify-www-team)
    (else
      (notify-misc-team)))

  The streams generated by (where) return the value of expr: truthy if expr
  matched the given event, and falsey otherwise. This means (where (metric 5))
  tests events and returns true if their metric is five."
  [expr & children]
  (let [p (where-rewrite expr)
        [true-kids else-kids] (where-partition-clauses children)]
    `(let [true-kids# ~true-kids
           else-kids# ~else-kids]
       (fn stream [event#]
         (let [value# (let [~'event event#] ~p)]
           (if value#
             (call-rescue event# true-kids#)
             (call-rescue event# else-kids#))
           value#)))))

(defn split*-match
  [event [pred stream]]
  (let [stream (or stream pred)]
    (if (or (= stream pred) (pred event))
      stream)))

(defn split*
  "Given a list of function and stream pairs, passes the current event onto the
  stream associated with the first passing condition.

   Conditions are functions as for where*.  An odd number of forms will make
  the last form the default stream. For example:

   (split*
     (fn [e] (< 0.9  (:metric e))) (with :state \"critical\" index)
     (fn [e] (< 0.75 (:metric e))) (with :state \"warning\" index)
     (with :state \"ok\" index))"
  [& clauses]
  (let [clauses (partition-all 2 clauses)]
    (fn stream [event]
      (when-let [stream (some (partial split*-match event) clauses)]
        (call-rescue event [stream])))))

(defmacro split
  "Behave as for split*, expecting predicates to be (where) expressions instead
  of functions. Example:

  (split
    (< 0.9  metric) (with :state \"critical\" index)
    (< 0.75 metric) (with :state \"warning\" index)
    (with :state \"ok\" index))"
  [& clauses]
  (let [clauses (mapcat (fn [clause]
                          (if (nil? (second clause))
                            clause
                            [`(where ~(first clause)) (second clause)]))
                            (partition-all 2 clauses))]
    `(split* ~@clauses)))

(defmacro splitp
  "Takes a binary predicate, an expression and a set of clauses. Each clause
  takes the form

  test-expr stream

  splitp returns a stream which accepts an event. Expr is a (where) expression,
  which will be evaluated against the event to obtain a value for selecting a
  clause. For each clause, evaluates (pred test-expr value). If the result is
  logical true, evaluates (stream event) and returns that value.

  A single default stream can follow the clauses, and its value will be
  returned if no clause matches. If no default stream is provided and no clause
  matches, an IllegalArgumentException is thrown.

  Splitp evaluates streams once at invocation time.

  Example:

  (splitp < metric
    0.9  (with :state \"critical\" index)
    0.75 (with :state \"warning\" index)
         (with :state \"ok\" index))"
  [pred expr & clauses]
  (let [; Split up clauses into [stream test-expr] pairs
        clauses (map (fn [[a b :as clause]]
                       (case (count clause)
                         1 [a]
                         2 [b a]))
                     (partition-all 2 clauses))

        ; Symbol for an event
        event-sym (gensym "event__")

        ; Symbols to store streams
        stream-syms (repeatedly (count clauses) #(gensym "stream__"))

        ; Binding symbols to stream expressions
        stream-bindings (mapcat (fn [stream-sym [stream test-expr]]
                                  [stream-sym stream])
                                stream-syms
                                clauses)

        ; Transform clauses into a form for condp
        condp-clauses (mapcat (fn [stream-sym [stream test-expr :as clause]]
                                (if (= 1 (count clause))
                                  [`(~stream-sym ~event-sym)]
                                  [test-expr `(~stream-sym ~event-sym)]))
                              stream-syms
                              clauses)]

    `(let [; A function which extracts a value from an event
           valuefn# (where ~expr)
           ; Bind streams to symbols
           ~@stream-bindings]
       ; Return a stream function
       (fn splitp-stream# [~event-sym]
         ; Which extracts a value from the event and calls condp
         (condp ~pred (valuefn# ~event-sym)
           ~@condp-clauses)))))

(defn runs
  "Usable to perform flap detection, runs examines a moving-event-window of
  n events and determines if :field is the same across all them. If it is,
  runs passes on the last (newest) event of the window. In practice, this can
  be used with (changed-state ...) as a child to reduce 'flappiness' for state
  changes.

  (runs 3 :state prn) ; Print events where there are 3-in-a-row of a state."
  [len-run field & children]
  (moving-event-window
    len-run
    (smap
      (fn [events]
        (if (>= (count events) len-run 1)
          (if (apply = (map field events))
              (last events))))
      (apply sdo children))))

(defn stable
  "A stream which detects stable groups of events over time. Takes a time
  period in seconds, and a function of events. Passes on all events for which
  (f event1) is equal to (f event2), for each successive pair of events, for at
  least dt seconds. Use (stable) to filter out transient spikes and flapping
  states.

  In these plots, stable events are shown as =, and unstable events are shown
  as -. = events are passed to children, and - events are ignored.

       A spike           Flapping           Stable changes
  |                 |                    |
  |       -         |    -- -   ======   |      =====
  |                 |        -           |           ========
  |======= ======   |====  -  --         |======
  +------------->   +---------------->   +------------------>
        time              time                  time

  May buffer events for up to dt seconds when the value of (f event) changes,
  in order to determine if the new value is stable or not.

  ; Passes on events where the state remains the same for at least five
  ; seconds.
  (stable 5 :state prn)"
  [dt f & children]
  (let [state (atom {:prev   ::unknown
                     :task   nil
                     :out    (list)
                     :buffer []})

        ; Called by our timeout task dt seconds after a state transition;
        ; ensures that we flush the buffer if no new events have arrived.
        timeout (fn timeout []
                  (let [es (-> state
                             (swap!
                               (fn [state]
                                 (let [e (first (:buffer state))]
                                   (if (and e
                                            (<= dt (- (unix-time)
                                                      (:time e))))
                                     ; Flush!
                                     (merge state {:out (:buffer state)
                                                   :buffer []})
                                     ; Either the buffer was flushed already,
                                     ; or the event we were meant to flush was
                                     ; replaced by another.
                                     state))))
                             :out)]
                    (doseq [e es]
                      (call-rescue e children))))

        update (fn update [state event]
                 (let [value (f event)
                       buffer (:buffer state)]
;                   (prn :event event)
;                   (prn :state state)
                   (if (= value (:prev state))
                     ; This event is the same as before.
                     (if (empty? buffer)
                       ; We're stable; flush this event immediately.
                       (assoc state :out (list event))

                       ; We're buffering.
                       (let [buffer (conj buffer event)]
                         (if (<= dt (- (:time event) (:time (first buffer))))
                           ; We're now stable. Flush buffer.
                           (merge state {:out buffer
                                         :buffer []})

                           ; Still buffering.
                           (merge state {:out (list)
                                         :buffer buffer}))))

                     ; This event is different than the one before it; skip
                     {:prev value
                      :out (list)
                      :buffer [event]})))]

    (fn stream [event]
      (let [state (swap! state update event)]
        (when (= 1 (count (:buffer state)))
          ; We just replaced the buffer with a single event, which means the
          ; value *just* changed. In N seconds we may need to flush the buffer,
          ; if the value doesn't change. We *could* track the task to do this,
          ; but it's simpler to just add N tasks during a flapping state and
          ; let them all fight it out.
          (once! (+ dt (:time (first (:buffer state))))
                 timeout))

        (doseq [e (:out state)]
          (call-rescue e children))))))

(defn project*
  "Like project, but takes predicate *functions* instead of where expressions."
  [predicates & children]
  (let [n          (count predicates)
                   ; A vector of the current state and the *returned* state
                   ; which allows us to submit expired events exactly once.
        state      (atom [(vec (repeat n nil)) nil])
        ; Returns a vector of two states: one with expired events replaced
        ; by nil, and one with expired events replaced with (expire event).
        clean-expire (fn clean-expire [state]
                       (loop [i 0
                              clean state
                              expired state]
                         (if (<= n i)
                           [clean expired]
                           (if (expired? (state i))
                             (recur (inc i)
                                    (assoc clean i nil)
                                    (assoc expired i (expire (state i))))
                             (recur (inc i) clean expired)))))
        ; Updates the state vector with a new event at specific indices
        update (fn update [[state _] indices event]
                 (clean-expire (reduce (fn r [state index]
                                   (assoc state index event))
                                 state
                                 indices)))]
    (fn stream [event]
      ; Find matching predicates
      (let [indices (loop [i       0
                           indices (list)]
                      (if (<= n i)
                        indices
                        (recur (inc i)
                               (if ((predicates i) event)
                                 (conj indices i)
                                 indices))))]
        ; Apply changes atomically and call
        (when (not (empty? indices))
          (let [[_ events] (swap! state update indices event)]
            (call-rescue events children)))))))

(defmacro project
  "Projects an event stream into a specific basis--like (coalesce), but where
  you only want to compare two or three specific events. Takes a vector of
  predicate expressions, like those used in (where). Project maintains a vector
  of the most recent event for each predicate. An incoming event is compared
  against each predicate; if it matches, the event replaces any previous event
  in that position and the entire vector of events is forwarded to all child
  streams. Expired events are included in the emitted vector of events *once*,
  and removed from the state vector thereafter.

  Use project when you want to compare a small number of distinct states over
  time. For instance, to find the ratio of enqueues to dequeues:

  (project [(service \"enqueues\")
            (service \"dequeues\")]
    (fn [[enq deq]]
      (prn (/ (:metric enq)
              (:metric deq)))))"
  [basis & children]
  (let [wrapped (mapv (fn [expr] `(where ~expr)) basis)]
    `(project* ~wrapped ~@children)))

(defrecord ApdexState [event ^long satisfied ^long tolerated ^long other])

(defn apdex*
  "Like apdex, but takes functions of events rather than where-predicates.

  A stream which computes Apdex metrics every dt seconds for a stream of
  events. If (satisfied? event) is truthy, increments the satisfied count for
  that time window by 1. If (tolerated? event) is truthy, increments the
  tolerated count for that time window by 1.  Any other states are ignored.
  Every dt seconds (as long as events are arriving), emits an event with a
  metric between 0 and 1, derived by:

  (satisfied count + (tolerating count / 2) / total count of received events

  Ignores expired events.

  See http://en.wikipedia.org/wiki/Apdex for details."
  [dt satisfied? tolerated? & children]
  (part-time-simple
    dt
    (fn reset [_] (ApdexState. {} 0 0 0))
    (fn add [^ApdexState state event]
      (if (expired? event)
        state
        (let [k (cond (satisfied? event) :satisfied
                      (tolerated? event) :tolerated
                      :else              :other)]
          (-> state
            (assoc :last-event event)
            (assoc k (inc (get state k)))))))
    (fn finish [{:keys [last-event satisfied tolerated other]} _ _]
      (let [total (+ satisfied tolerated other)]
        (when-not (zero? total)
          (call-rescue (assoc last-event :metric
                              (/ (+ satisfied
                                    (/ tolerated 2))
                                 (+ satisfied tolerated other)))
                       children))))))

(defmacro apdex
  "A stream which computes Apdex metrics every dt seconds for a stream of
  events. Satisfied? and tolerated? are predicates as for (where). If satisfied
  is truthy, increments the satisfied count for that time window by 1. If
  (tolerated? event) is truthy, increments the tolerated count for that time
  window by 1. Any other states are ignored. Every dt seconds (as long as
  events are arriving), emits an event with a metric between 0 and 1, derived
  by:

  (satisfied count + (tolerating count / 2) / total count of received events

  Ignores expired events.

  See http://en.wikipedia.org/wiki/Apdex for details."
  [dt satisfied? tolerated? & children]
  `(apdex* ~dt (where ~satisfied?) (where ~tolerated?) ~@children))

(defn clock-skew
  "Detects clock skew between hosts. Keeps track of what time each host thinks
  it is, based on their most recent event time. Compares the time of each event
  to the median clock, and passes on that event with metric equal to the time
  difference: events ahead of the clock have positive metrics, and events
  behind the clock have negative metrics."
  [& children]
  (smap (fn preprocess [event]
          (assoc event ::clock-skew-timestamp (unix-time)))
        (coalesce-with-event
          ; We only care about the last event on each host
          (fn keyfn [event] (:host event))

          ; Now, given a specific event and a list of the current state...
          (fn order [event events]
            (if (expired? event)
              (call-rescue event children)
              (let [now (unix-time)
                    clock (->> events
                            ; Figure out what time is is *now*
                            (map (fn [event]
                                   (when (:time event)
                                     (+ (:time event)
                                        (- now
                                           (::clock-skew-timestamp event))))))
                            ; Drop expired events (or any others without times)
                            (remove nil?)
                            ; Pick median
                            sort
                            middle)
                    delta (if clock
                            (- (:time event) clock)
                            0)
                    event (-> event
                            (dissoc ::clock-skew-timestamp)
                            (assoc :metric delta))]
                (call-rescue event children)))))))
