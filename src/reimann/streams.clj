(ns reimann.streams
  (:use reimann.common)
  (:use reimann.folds)
  (:require [reimann.index :as index])
  (:require [reimann.client])
  (:use [clojure.contrib.math])
  (:use [clojure.contrib.logging]))

; Call each fn, in order, with event. Rescues and logs any failure.
(defmacro call-rescue [event children]
  `(do
     (doseq [child# ~children]
       (try
         (child# ~event)
         (catch Exception e#
           (log :warn (str child# " threw") e#))))
     true))

; On my MBP tops out at around 300K
; events/sec. Experimental benchmarks suggest that:
(comment (time
             (doseq [f (map (fn [t] (future
               (let [c (ref 0)]
                 (dotimes [i (/ total threads)]
                         (let [e {:metric_f 1 :time (unix-time)}]
                           (dosync (commute c + (:metric_f e))))))))
                            (range threads))]
               (deref f))))
; can do something like 1.9 million events/sec over 4 threads.  That suggests
; there's a space for a faster (but less correct) version which uses either
; agents or involves fewer STM ops. Assuming all events have local time might
; actually be more useful than correctly binning/expiring multiple times.
; Also: broken?
(defn part-time-fn [interval create add finish]
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
            t (or (event :time) (unix-time))]
        (bin event t)))))

; Partitions events by time (fast variant). Over interval seconds, adds events
; to a bin, created with (create). When the interval is complete, calls (finish
; bin start-time end-time)
;
; This leaks a thread. Need to think about dynamic scheduling/expiry.
(defn part-time-fast [interval create add finish]
  (let [current (ref (create))
        start (ref (unix-time))
        switcher (.start (new Thread (bound-fn []
          ; Switch between bins
          (loop [] 
            ; Wait for interval
            (Thread/sleep (* interval 1000))
            ; Switch out old bin, create new one, call finish on old bin.
            (apply finish
                   (dosync
                     (let [bin (deref current)
                           old-start (deref start)
                           boundary (unix-time)]
                       (ref-set start boundary)
                       (ref-set current (create))
                       [bin old-start boundary])))
            (recur)))))]
    (fn [event]
      (dosync
        (add (deref current) event)))))

; Take the sum of every event over interval seconds and divide by the interval
; size.
(defn rate [interval & children]
  (part-time-fast interval
      (fn [] {:count (ref 0)
              :state (ref {})})
      (fn [r event] (dosync
                      (ref-set (:state r) event)
                      (alter (:count r) + (:metric_f event))))
      (fn [r start end]
        (let [event (dosync
                (let [count (deref (r :count))
                      rate (/ count (- end start))]
                  (merge (deref (:state r)) 
                         {:metric_f rate :time (round end)})))]
          (call-rescue event children)))))

(defn percentiles [interval points & children]
  "Over each period of interval seconds, aggregates events and selects one
  event from that period for each point. If point is 0, takes the lowest metric
  event.  If point is 1, takes the highest metric event. 0.5 is the median
  event, and so forth. Forwards each of these events to children. The service
  name has the point appended to it; e.g. 'response time' becomes 'response
  time .95'."
  (part-time-fast interval
                (fn [] (ref []))
                (fn [r event] (dosync (alter r conj event)))
                (fn [r start end]
                  (let [samples (dosync
                                  (sorted-sample (deref r) points))]
                    (doseq [event samples] (call-rescue event children))))))

; Sums all metric_fs together. Emits the most recent event each time this stream
; is called, but with summed metric_f.
(defn sum [& children]
  (let [sum (ref 0)]
    (fn [event]
      (let [s (dosync (commute sum + (:metric_f event)))
            event (assoc event :metric_f s)]
        (call-rescue event children)))))

; Emits the most recent event each time this stream is called, but with the
; average of all received metric_fs.
(defn mean [children]
  (let [sum (ref nil)
        total (ref 0)]
    (fn [event]
      (let [m (dosync 
                (let [t (commute total inc)
                      s (commute sum + (:metric_f event))]
                  (/ s t)))
            event (assoc event :metric_f m)]
        (call-rescue event children)))))

(defn throttle [n m & children]
  "Passes on n events every m seconds. Drops events when necessary."
  (part-time-fast m
    (fn [] (ref 0))
    (fn [sent event]
      (when-not (dosync (< n (alter sent inc)))
        (call-rescue event children)))
    (fn [sent start end])))

(defn rollup [n m & children]
  "Invokes children with events at most n times per m second interval. Passes 
  *vectors* of events to children, not a single event at a time. For instance, 
  (rollup 3 1 f) receives five events and forwards three times per second:

  1 -> (f [1])
  2 -> (f [2])
  3 -> (f [3])
  4 ->
  5 -> 

  ... and events 4 and 5 are rolled over into the next period:

    -> (f [4 5])"

  (let [carry (ref [])]
    ; This implementation relies on stable, one-after-the-other creation of
    ; buckets from part-time. This is NOT always the case, so consider
    ; carefully which part-time implementation is used.
    (part-time-fast m
      (fn [] (dosync
               (if (empty? (deref carry))
                           ; We haven't send any events yet.
                           (ref 0)
                           ; We already sent (or will shortly send) 1 event 
                           ; for the carry.
                           (ref 1))))

      (fn [sent event]
        (if (dosync (< n (alter sent inc)))
          ; Overtime!
          (dosync (alter carry conj event))
          ; Send right away
          (call-rescue [event] children)))

      (fn [sent start end]
        ; Dispatch carried events if present.
        (let [events (dosync
                       (let [x (deref carry)]
                         (ref-set carry [])
                         x))]
          (when-not (empty? events)
            (call-rescue events children)))))))


; Conj events onto the given reference
(defn append [reference]
  (fn [event]
    (dosync
      (alter reference conj event))))

; Set reference to the most recent event that passes through.
(defn register [reference]
  (fn [event]
    (dosync (ref-set reference event))))

; Prints an event to stdout
(defn stdout [event]
  (fn [event]
    (prn event)))

; Sends a map to a client, coerced to state
(defn fwd [client]
  (fn [statelike]
    (reimann.client/send-state client statelike)))

(defn match [f value & children]
  "Passes events on to children only when (f event) is equal to value. If f is a regex, uses re-find to match."
    (fn [event]
      (let [x (f event)]
        (when (if (= (class value) java.util.regex.Pattern)
                (re-find value x)
                (= value x))
          (call-rescue event children)
          true))))

; Shortcuts for match
(defn description? [value & children] (apply match :description value children))
(defn host? [value & children] (apply match :host value children))
(defn metric? [value & children] (apply match :metric_f value children))
(defn service? [value & children] (apply match :service value children))
(defn state? [value & children] (apply match :state value children))
(defn time? [value & children] (apply match :time value children))

(defn expired? [& children]
  "Passes on events with state expired"
  (apply match :state "expired" children))

; Transforms an event by associng a set of new k:v pairs
(defmulti with (fn [& args] (map? (first args))))
(defmethod with true [m & children]
  (fn [event]
;    Merge on protobufs is broken; nil values aren't applied.
;    (let [e (merge event m)]
    (let [e (reduce (fn [m, [k, v]]
                      (if (nil? v) (dissoc m k) (assoc m k v)))
                    event m)]
      (call-rescue e children))))
(defmethod with false [k v & children]
  (fn [event]
;    (let [e (assoc event k v)]
    (let [e (if (nil? v) (dissoc event k) (assoc event k v))]
      (call-rescue e children))))

; Splits stream by field.
; Every time an event arrives with a new value of field, this macro invokes
; its enclosed form to return a *new*, distinct stream for that particular
; value.
(defmacro by [field & children]
  ; new-fork is a function which gives us a new copy of our children.
  ; table is a reference which maps (field event) to a fork (or list of
  ; children).
  `(let [new-fork# (fn [] [~@children])]
     (by-fn ~field new-fork#)))

(defn by-fn [field new-fork]
  (let [table (ref {})]
     (fn [event]
       (let [fork-name (field event)
             fork (dosync
                    (or ((deref table) fork-name)
                        ((alter table assoc fork-name (new-fork)) 
                           fork-name)))]
         (call-rescue event fork)))))

; Passes on events only when (f event) differs from that of the previous event.
(defn changed [pred & children]
  (let [previous (ref nil)]
    (fn [event]
      (when
        (dosync
          (let [cur (pred event) 
                old (deref previous)]
            (when-not (= cur old)
              (ref-set previous cur)
              true)))
        (call-rescue event children)))))

; Passes on events only when their metric falls within the given inclusive
; range. (within [0 1] (fn [event] do-something))
(defn within [r & children]
  (fn [event]
    (when (<= (first r) (:metric_f event) (last r))
      (call-rescue event children))))

(defn without [r & children]
  "Passes on events only when their metric falls outside the given (inclusive) range."
  (fn [event]
    (when (not (<= (first r) (:metric_f event) (last r)))
      (call-rescue event children))))

(defn over [x & children]
  "Passes on events only when their metric is greater than x"
  (fn [event]
    (when (< x (:metric_f event))
      (call-rescue event children))))

(defn under [x & children]
  "Passes on events only when their metric is smaller than x"
  (fn [event]
    (when (> x (:metric_f event))
      (call-rescue event children))))

(defn where-test [k v]
  (if (= (class v) java.util.regex.Pattern)
    (list 're-find v (list (keyword k) 'event))
    (list '= v (list (keyword k) 'event))))

; Hack hack hack hack
(defn where-rewrite [expr]
  "Rewrites lists recursively. Replaces (metric_f x y z) with a test matching
  (:metric_f event) to any of x, y, or z, either by = or re-find. Replaces any
  other instance of metric_f with (:metric_f event). Does the same for host,
  service, event, state, time, and description."
  (let [syms #{'host 'service 'state 'metric_f 'time 'description}]
    (if (list? expr)
      ; This is a list.
      (if (syms (first expr))
        ; list starting with a magic symbol
        (let [[field & values] expr]
          (if (= 1 (count values))
            ; Match one value
            (where-test field (first values))
            ; Any of the values
            (concat '(or)
                       (map (fn [value] (where-test field value)) values))))

        ; Other list
        (map where-rewrite expr))

      ; Not a list
      (if (syms expr)
        ; Expr *is* a magic sym
        (list (keyword expr) 'event)
        expr))))

(defmacro where [expr & children]
  "Passes on events where expr is true. Expr is rewritten using where-rewrite.
  'event is bound to the event under consideration. Examples:

  ; Match any state where metric is either 1, 2, 3, or 4.
  (where (metric_f 1 2 3 4) ...)
  
  ; Match a state where the metric is negative AND the state is ok.
  (where (and (> 0 metric_f)
              (state \"ok\")) ...)

  ; Match a state where an arbitrary function f applied to the event is truthy.
  (where (f event) ...)

  ; Match a state where the host begins with web
  (where (host #\"^web\") ...)"
  (let [p (where-rewrite expr)]
    `(let [kids# [~@children]]
      (fn [event#]
       (when (let [~'event event#] ~p)
         (call-rescue event# kids#))))))

(defn update [index]
  "Updates the given index with all states received."
  (fn [state]
    (index/update index state)))

(defn delete-from [index]
  "Deletes any events that pass through from the index"
  (fn [state]
    (index/delete index state)))
