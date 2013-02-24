(ns riemann.streams
  "Streams are functions which accept events (or, in some cases, lists of
  events). They can filter those events, transform them, apply them to other
  streams, combine them over time, update state, forward to other services, and
  more. Most streams accept, after their initial arguments, any number of
  streams as children. When invoking children, they typically catch all
  exceptions and log them, then proceed to the next child.

  Any function accepting an event map (e.g. {:service \"foo\" :metric 3.5} can
  be a stream. prn is a stream. So is (partial log :info), or (fn [x]). The
  streams namespace aims to provide a comprehensive set of widely applicable,
  combinable tools for building up more complicated streams."
  (:use [riemann.common :exclude [match]]
        [riemann.time :only [unix-time linear-time every! once! after! next-tick defer cancel]]
        clojure.math.numeric-tower
        clojure.tools.logging)
  (:require [riemann.folds :as folds]
            [riemann.index :as index]
            riemann.client
            riemann.logging
            [clojure.set :as set]))

(def  infinity (/  1.0 0))
(def -infinity (/ -1.0 0))

(defn expired?
  "There are two ways an event can be considered expired. First, if it has state \"expired\". Second, if its :ttl and :time indicates it has expired."
  [event]
    (or (= (:state event) "expired")
        (when-let [time (:time event)]
          (let [ttl (or (:ttl event) index/default-ttl)
                age (- (unix-time) time)]
            (> age ttl)))))

(defmacro call-rescue
  "Call each child, in order, with event. Rescues and logs any failure."
  [event children]
  `(do
     (doseq [child# ~children]
       (try
         (child# ~event)
         (catch Exception e#
           (warn e# (str child# " threw")))))
     true))

(defn bit-bucket
  "Discards arguments."
  [args])

(defn dual
  "A stream which splits events into two mirror-images streams, based on (pred
  e). If (pred e) is true, calls (true-stream e) and (false-stream (expire e)).
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

(defn combine
  "Returns a function which takes a seq of events. Combines events with f, then
  forwards the result to children."
  [f & children]
  (fn [events]
    (call-rescue (f events) children)))

(defn smap
  "Streaming map. Calls children with (f event). Prefer this to (adjust f).
  Example:

  (smap :metric prn) ; prints the metric of each event.
  (smap #(assoc % :state \"ok\") index) ; Indexes each event with state \"ok\""
  [f & children]
  (fn [event]
    (call-rescue (f event) children)))

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
                                  (+ (:metric event) (:metric acc)))) ...)

  Or, using riemann.folds, a simple moving average:
  (sreduce (fn [acc event] (folds/mean [acc event])) ...)"
  [f & opts]
  (if (fn? (first opts))
    ; No value provided
    (let [children   opts
          first-time (ref true)
          acc        (ref nil)]
      (fn [event]
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
      (fn [event]
        (call-rescue (swap! acc f event) children)))))

(defn sdo
  "Takes a list of functions f1, f2, f3, and returns f such that (f event)
  calls (f1 event) (f2 event) (f3 event). Useful for binding several streams to
  a single variable.
  
  (sdo prn (rate 5 index))"
  [& children]
  (fn [event]
    (call-rescue event children)))

(defn stream
  [& args]
  (deprecated "riemann.streams/stream is now streams/sdo."
              (apply sdo args)))

(defn moving-event-window
  "A sliding window of the last few events. Every time an event arrives, calls
  children with a vector of the last n events, from oldest to newest. Ignores
  event times. Example:

  (moving-event-window 5 (combine folds/mean index))"
  [n & children]
  (let [window (atom (vec []))]
    (fn [event]
      (let [w (swap! window (fn [w]
                              (vec (take-last n (conj w event)))))]
        (call-rescue w children)))))

(defn fixed-event-window
  "Passes on fixed-size windows of n events each. Accumulates n events, then
  calls children with a vector of those events, from oldest to newest. Ignores
  event times. Example:

  (fixed-event-window 5 (combine folds/mean index))"
  [n & children]
  (let [buffer (atom [])]
    (fn [event]
      (let [events (swap! buffer (fn [events]
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
    (fn [event]
      (let [events (dosync
                     ; Compute minimum allowed time
                     (let [cutoff (alter cutoff max (- (get event :time 0) n))]
                       (when (or (nil? (:time event))
                                 (< cutoff (:time event)))
                         ; This event belongs in the buffer, and our cutoff may
                         ; have changed.
                         (alter buffer conj event)
                         (alter buffer
                                (fn [events]
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
    (fn [event]
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
        finish-eventually (fn [bin start]
          (.start (new Thread (fn []
                         (let [end (+ start interval)]
                           ; Sleep until this bin is past
                           (Thread/sleep (max 0 (* 1000 (- end (unix-time)))))
                           ; Prevent anyone else from creating or changing this
                           ; bin. Congratulations, you've invented timelocks.
                           (dosync
                             (alter bins dissoc start)
                             (alter watermark max end))
                           ; Now that we're safe from modification, finish him!
                           (finish bin start end))))))

        ; Add event to the bin for a time
        bin (fn [event t]
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

    (fn [event]
      (let [; What time did this event happen at?
            t (or (:time event) (unix-time))]
        (bin event t)))))

(defn periodically-until-expired
  "When an event arrives, begins calling f every interval seconds. Starts
  after delay. Stops calling f when an expired? event arrives."
  ([f] (periodically-until-expired 1 0 f))
  ([interval f] (periodically-until-expired interval 0 f))
  ([interval delay f]
   (let [task (atom nil)]
     (fn [event]
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
        switch (fn part-time-fast-switch []
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

(defn fold-interval
  "Applies the folder function to all event-key values of events during
  interval seconds."
  [interval event-key folder & children]
  (part-time-fast interval
      (fn [] (ref []))
      (fn [r event]
        (dosync
          (if-let [ek (event-key event)]
            (alter r conj event))))
      (fn [r start end]
        (let [stat (dosync
                    (folder (map event-key @r)))
              event (assoc (last @r) event-key stat)]
          (call-rescue event children)))))

(defn fold-interval-metric [interval folder & children] (apply fold-interval interval :metric folder children))

(defn fill-in
  "Passes on all events. Fills in gaps in event stream with copies of the given
  event, wherever interval seconds pass without an event arriving. Inserted
  events have current time. Stops inserting when expired. Uses local times."
  ([interval default-event & children]
   (let [fill (fn []
                (call-rescue (assoc default-event :time (unix-time)) children))
         new-deferrable (fn [] (every! interval
                                       interval
                                       fill))
         deferrable (ref (new-deferrable))]
    (fn [event]
      (let [d (deref deferrable)]
        (if d
          ; We have an active deferrable
          (if (expired? event)
            (do
              (cancel d)
              (dosync (ref-set deferrable nil)))
            (defer d interval))
          ; Create a deferrable
          (when-not (expired? event)
            (locking deferrable
              (when-not (deref deferrable)
                (dosync (ref-set deferrable (new-deferrable))))))))

      ; And forward
      (call-rescue event children)))))

(defn fill-in-last
  "Passes on all events. Fills in gaps in event stream with copies of the last
  event merged with the given data, wherever interval seconds pass without an
  event arriving. Inserted events have current time. Stops inserting when
  expired. Uses local times."
  ([interval update & children]
   (let [last-event (ref nil)
         fill (fn []
                (call-rescue (merge @last-event update {:time (unix-time)}) children))
         new-deferrable (fn [] (every! interval interval fill))
         deferrable (ref nil)]
     (fn [event]
       ; Record last event
       (dosync (ref-set last-event event))

       (let [d (deref deferrable)]
         (if d
           ; We have an active deferrable
           (if (expired? event)
             (do
               (cancel d)
               (dosync (ref-set deferrable nil)))
             (defer d interval))
           ; Create a deferrable
           (when-not (expired? event)
             (locking deferrable
               (when-not (deref deferrable)
                 (dosync (ref-set deferrable (new-deferrable))))))))

       ; And forward
       (call-rescue event children)))))

(defn interpolate-constant
  "Emits a constant stream of events every interval seconds, starting when an
  event is received, and ending when an expired event is received. Times are
  set to Riemann's time. The first and last events are forwarded immediately.

  Note: ignores event times currently--will change later."
  [interval & children]
    (let [state (ref nil)
          emit-dup (fn []
                     (call-rescue
                       (assoc (deref state) :time (unix-time))
                       children))
          peri (periodically-until-expired interval emit-dup)]
      (fn [event]
        (dosync
          (ref-set state event))

        (peri event)
        (when (expired? event)
          (call-rescue event children)
          ; Clean up
          (dosync
            (ref-set state nil))
          ))))

(defn ddt-real
  "(ddt) in real time."
  [n & children]
  (let [state (atom (list nil))  ; Events at t3, t2, and t1.
        swap (fn swap []
               (let [[_ e2 e1] (swap! state
                                      (fn [[e3 e2 e1 :as state]]
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
        (swap! state (fn [[most-recent & more]] (cons event more))))
      (poller event))))

(defn ddt-events
  "(ddt) between each pair of events."
  [& children]
  (let [prev (ref nil)]
    (fn stream [event]
      (when-let [m (:metric event)]
        (let [prev-event (dosync
                           (let [prev-event (deref prev)]
                             (ref-set prev event)
                             prev-event))]
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
  "Take the sum of every event over interval seconds and divide by the interval
  size. Emits one event every interval seconds. Starts as soon as an event is
  received, stops when an expired event arrives. Uses the most recently
  received event with a metric as a template. Event ttls decrease constantly if
  no new events arrive."
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

        tick (fn tick []
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
                (fn [] (ref []))
                (fn [r event] (dosync (alter r conj event)))
                (fn [r start end]
                  (let [samples (dosync
                                  (folds/sorted-sample (deref r) points))]
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
              (let [sum (ref 0)]
                (fn [event]
                  (let [s (dosync
                            (when-let [m (:metric event)]
                              (commute sum + (:metric event))))
                        event (assoc event :metric s)]
                    (call-rescue event children))))))

(defn mean-over-time
  "Emits the most recent event each time this stream is called, but with the
  average of all received metrics."
  [children]
  (let [sum (ref nil)
        total (ref 0)]
    (fn [event]
      (let [m (dosync
                (let [t (commute total inc)
                      s (commute sum + (:metric event))]
                  (/ s t)))
            event (assoc event :metric m)]
        (call-rescue event children)))))

(defn ewma-timeless
  "Exponential weighted moving average. Constant space and time overhead.
  Passes on each event received, but with metric adjusted to the moving
  average. Does not take the time between events into account."
  [r & children]
  (let [m (ref 0)
        c-existing (- 1 r)
        c-new r]
    (fn [event]
      ; Compute new ewma
      (let [m (when-let [metric-new (:metric event)]
                (dosync
                  (ref-set m (+ (* c-existing (deref m))
                                (* c-new metric-new)))))]
        (call-rescue (assoc event :metric m) children)))))

(defn- top-update
  "Helper for top atomic state updates."
  [[smallest top] k f event]
  (let [value (f event)
        ekey [(:host event) (:service event)]
        scan (fn scan [top]
               (if (empty? top)
                 nil
                 (first (apply min-key second top))))
        trim (fn trim [top smallest]
               (if (< k (count top))
                 (dissoc top smallest)
                 top))]
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
      [ekey (assoc top ekey value)]

      ; Falls outside the top set.
      (and (not (top ekey))
           (<= value (top smallest))
           (<= k (count top)))
      [smallest top]

      ; In the top set
      :else
      (let [top (trim (assoc top ekey value) smallest)]
        (if (or (nil? (top smallest))
                (< value (top smallest)))
          [(scan top) top]
          [smallest top])))))

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
    (adjust [:tags conj \"top\"]
      index)
    index)
  
  This implementation of top is lazy, in a sense. It won't proactively expire
  events which are bumped from the top-k set--you have to wait for another
  event with the same host and service to arrive before child streams will know
  it's expired. At some point I (or an enterprising committer) should fix
  that."
  ([k f top-stream]
   (top k f top-stream bit-bucket))
  ([k f top-stream bottom-stream]
   (let [state (atom [nil {}])]
     (dual (fn stream [event]
             (let [top   (second (swap! state top-update k f event))]
               (top [(:host event) (:service event)])))
           top-stream
           bottom-stream))))

(defn throttle
  "Passes on n events every m seconds. Drops events when necessary."
  [n m & children]
  (part-time-fast m
    (fn [] (ref 0))
    (fn [sent event]
      (when-not (dosync (< n (alter sent inc)))
        (call-rescue event children)))
    (fn [sent start end])))

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
  (let [anchor (unix-time)
        state (atom {:sent 0
                     :buffer []
                     :flushed nil})
        ; Flush buffer of events
        flush (fn flush [state]
                (if (empty? (:buffer state))
                  (merge state {:sent 0
                                :flushed nil})
                  (merge state {:sent 1
                                :buffer []
                                :flushed (:buffer state)})))

        ; Tick function; flushes buffer and invokes children.
        tick (fn tick []
               (let [state (swap! state flush)]
                 ; Reschedule another tick if necessary
                 (when (= 1 (:sent state))
                   (once! (next-tick anchor dt) tick))
                 ; Flush any previously buffered events.
                 (when (:flushed state)
                   (call-rescue (:flushed state) children))))

        ; Enqueue an event. Increments :sent until a critical threshold, then
        ; starts storing events in buffer.
        enqueue (fn enqueue [state event]
                  (merge state {:sent (inc (:sent state))
                                :flushed nil
                                :buffer (if (< (:sent state) n)
                                          (:buffer state)
                                          (conj (:buffer state) event))}))]
    
    ; Stream: accept events and enqueue into state.
    (fn stream [event]
      (let [state (swap! state enqueue event)]
        (when (= 1 (:sent state))
          ; We claimed the right to set up the next tick.
          (once! (next-tick anchor dt) tick))
        (when (<= (:sent state) n)
          ; We're clear to send immediately.
          (call-rescue [event] children))))))

(defn coalesce
  "Combines events over time. Coalesce remembers the most recent event for each
  service that passes through it (limited by :ttl). Every time it receives an
  event, it passes on *all* events it remembers. When events expire, they are
  included in the emitted sequence of events *once*, and removed from the state
  table thereafter.

  Use coalesce to combine states that arrive at different times--for instance,
  to average the CPU use over several hosts."
  [& children]
  ; Past is [{keys -> events}, expired-events]
  (let [past (atom [{} []])]
    (fn stream [{:keys [host service] :as event}]
      (let [ekey  [host service]
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
        (call-rescue (concat expired (vals ok)) children)))))
    
(defn append
  "Conj events onto the given reference"
  [reference]
  (fn [event]
    (dosync
      (alter reference conj event))))

(defn register
  "Set reference to the most recent event that passes through."
  [reference]
  (fn [event]
    (dosync (ref-set reference event))))

(defn forward
  "Sends an event through a client"
  [client]
  (fn [event]
    (riemann.client/send-event client event)))

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
  (fn [event]
    (when (riemann.common/match value (f event))
      (call-rescue event children)
      true)))

(defn tagged-all
  "Passes on events where all tags are present. This stream returns true if an
  event it receives matches those tags, nil otherwise.

  (tagged-all \"foo\" prn)
  (tagged-all [\"foo\" \"bar\"] prn)"
  [tags & children]
  (if (coll? tags)
    (fn [event]
      (when (set/subset? (set tags) (set (:tags event)))
        (call-rescue event children)
        true))

    (fn [event]
      (when (member? tags (:tags event))
        (call-rescue event children)
        true))))

(defn tagged-any
  "Passes on events where any of tags are present. This stream returns true if
  an event it receives matches those tags, nil otherwise.

  (tagged-any \"foo\" prn)
  (tagged-all [\"foo\" \"bar\"] prn)"
  [tags & children]
  (if (coll? tags)
    (let [required (set tags)]
      (fn [event]
        (when (some required (:tags event))
          (call-rescue event children)
          true)))

    (fn [event]
      (when (member? tags (:tags event))
        (call-rescue event children)
        true))))

(def tagged "Alias for tagged-all" tagged-all)

(defn expired
  "Passes on events with :state \"expired\"."
  [& children]
  (apply match :state "expired" children))

(defn with
  "Transforms an event by associng a set of new k:v pairs, and passes the
  result to children. Use:

  (with :service \"foo\" prn)
  (with {:service \"foo\" :state \"broken\"} prn)"
  [& args]
  (if (map? (first args))
    ; Merge in a map of new values.
    (let [[m & children] args]
      (fn [event]
        ;    Merge on protobufs is broken; nil values aren't applied.
        ;    (let [e (merge event m)]
        (let [e (reduce (fn [m, [k, v]]
                          (if (nil? v) (dissoc m k) (assoc m k v)))
                        event m)]
          (call-rescue e children))))

    ; Change a particular key.
    (let [[k v & children] args]
      (fn [event]
        ;    (let [e (assoc event k v)]
        (let [e (if (nil? v) (dissoc event k) (assoc event k v))]
          (call-rescue e children))))))

(defn default
  "Transforms an event by associng a set of new key:value pairs, wherever the
  event has a nil value for that key. Passes the result on to children. Use:

  (default :service \"foo\" prn)
  (default {:service \"jrecursive\" :state \"chicken\"} prn)"
  [& args]
  (if (map? (first args))
    ; Merge in a map of new values.
    (let [[defaults & children] args]
      (fn [event]
        ;    Merge on protobufs is broken; nil values aren't applied.
        (let [e (reduce (fn [m [k v]]
                          (if (nil? (get m k)) (assoc m k v) m))
                        event defaults)]
          (call-rescue e children))))

    ; Change a particular key.
    (let [[k v & children] args]
      (fn [event]
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
      (fn [event]
        (let [value (apply f (field event) args)
              event (assoc event field value)]
          (call-rescue event children))))
    (apply smap (first args) (rest args))))

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
        table (ref {})]
     (fn [event]
       (let [fork-name (f event)
             fork (dosync
                    (or ((deref table) fork-name)
                        ((alter table assoc fork-name (new-fork))
                           fork-name)))]
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
  (let [options (first children)
        previous (ref
                   (when (map? options)
                     (:init options)))
        children (if (map? options)
                   (rest children)
                   children)]
    (fn [event]
      (when
        (dosync
          (let [cur (pred event)
                old (deref previous)]
            (when-not (= cur old)
              (ref-set previous cur)
              true)))
        (call-rescue event children)))))

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
  (fn [event]
    (when-let [m (:metric event)]
      (when (<= (first r) m (last r))
        (call-rescue event children)))))

(defn without
  "Passes on events only when their metric falls outside the given (inclusive)
  range."
  [r & children]
  (fn [event]
    (when-let [m (:metric event)]
      (when-not (<= (first r) m (last r))
        (call-rescue event children)))))

(defn over
  "Passes on events only when their metric is greater than x"
  [x & children]
  (fn [event]
    (when-let [m (:metric event)]
      (when (< x m)
        (call-rescue event children)))))

(defn under
  "Passes on events only when their metric is smaller than x"
  [x & children]
  (fn [event]
    (when-let [m (:metric event)]
      (when (> x m)
        (call-rescue event children)))))

(defn- where-test [k v]
  (case k
    ; Tagged checks that v is a member of tags.
    'tagged (list 'when (list :tags 'event)
                  (list 'riemann.common/member? v (list :tags 'event)))
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
               'tagged}]
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
    `(fn [event#]
       (let [value# (~f event#)]
         (if value#
           (call-rescue event# ~true-kids)
           (call-rescue event# ~else-kids))
         value#))))

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
