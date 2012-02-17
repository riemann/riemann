(ns reimann.streams
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
  (:use reimann.common)
  (:require [reimann.folds :as folds])
  (:require [reimann.index :as index])
  (:require reimann.client)
  (:require reimann.logging) 
  (:require [clojure.set])
  (:use [clojure.contrib.math])
  (:use [clojure.tools.logging]))

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

(defn combine
  "Returns a function which takes a seq of events. Combines events with f, then
  forwards the result to children."
  [f & children]
  (fn [events]
    (call-rescue (f events) children)))

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
            t (or (event :time) (unix-time))]
        (bin event t)))))

; This leaks a thread. Need to think about dynamic scheduling/expiry.
(defn part-time-fast
  "Partitions events by time (fast variant). Each <interval> seconds, creates a
  new bin by calling (create). Applies each received event to the current bin
  with (add bin event). When the time interval is over, calls (finish bin
  start-time end-time)." 
  [interval create add finish]
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

(defn rate
  "Take the sum of every event over interval seconds and divide by the interval
  size."
  [interval & children]
  (part-time-fast interval
      (fn [] {:count (ref 0)
              :state (ref {})})
      (fn [r event] (dosync
                      (ref-set (:state r) event)
                      (when-let [m (:metric event)]
                        (alter (:count r) + m))))
      (fn [r start end]
        (let [event (dosync
                (let [count (deref (r :count))
                      rate (/ count (- end start))]
                  (merge (deref (:state r)) 
                         {:metric rate :time (round end)})))]
          (call-rescue event children)))))

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
  "Counts things. All metrics are summed together; passes on each event with
  the summed metric. When an event has tag \"reset\", resets the counter to
  zero and continues summing."
  [& children]
  (let [counter (ref 0)]
    (fn [event]
      (when (member? "reset" (:tags event))
        (dosync (ref-set counter 0)))
      (when-let [m (:metric event)]
        (let [c (dosync (alter counter + m))]
          (call-rescue (assoc event :metric c) children))))))

(defn sum-over-time 
  "Sums all metrics together. Emits the most recent event each time this
  stream is called, but with summed metric."
  [& children]
  (let [sum (ref 0)]
    (fn [event]
      (let [s (dosync
                (when-let [m (:metric event)]
                  (commute sum + (:metric event))))
            event (assoc event :metric s)]
        (call-rescue event children)))))

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
  [n m & children]

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

(defn expired?
  "Returns true if an event's :ttl and :time indicates it has expired."
  [event]
      (let [ttl (or (:ttl event) index/default-ttl)
            age (- (unix-time) (:time event))]
        (> age ttl)))

(defn coalesce
  "Combines events over time. Coalesce remembers the most recent event for each
  service that passes through it (limited by :ttl). Every time it receives an
  event, it passes on *all* events it remembers.
  
  Use coalesce to combine states that arrive at different times--for instance,
  to average the CPU use over several hosts."
  [& children]
  (let [past (ref {})]
    (fn [event]
      ; Expire old events
      (dosync
        (ref-set past
                 (let [p (deref past)]
                   (reduce (fn [res [k v]]
                             (if (expired? v)
                               (dissoc res k)
                               res))
                           p p))))
      
      ; Add event to memory
      (let [events (vals
        (dosync (alter past assoc [(:host event) (:service event)] event)))]
        
        ; Pass on
        (call-rescue events children)))))

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

(defn fwd
  "Sends a map to a client, coerced to state"
  [client]
  (fn [statelike]
    (reimann.client/send-state client statelike)))

(defn match
  "Passes events on to children only when (f event) is equal to value. If f is
  a regex, uses re-find to match."
  [f value & children]
  (fn [event]
    (let [x (f event)]
      (when (if (= (class value) java.util.regex.Pattern)
              (re-find value x)
              (= value x))
        (call-rescue event children)
        true))))

; Shortcuts for match
;(defn description [value & children] (apply match :description value children))
;(defn host [value & children] (apply match :host value children))
;(defn metric [value & children] (apply match :metric value children))
;(defn service [value & children] (apply match :service value children))
;(defn state [value & children] (apply match :state value children))
;(defn time [value & children] (apply match :time value children))

(defn tagged-all 
  "Passes on events where all tags are present.

  (tagged-all \"foo\" prn)
  (tagged-all [\"foo\" \"bar\"] prn)"
  [tags & children]
  (fn [event]
    (when (subset? tags (:tags event))
      (call-rescue event children))))
(def tagged tagged-all)

(defn tagged-any
  "Passes on events where any of tags are present.

  (tagged-any \"foo\" prn)
  (tagged-all [\"foo\" \"bar\"] prn)"
  [tags & children]
  (let [required (set tags)]
    (fn [event]
      (when (some required (:tags event))
        (call-rescue event children)))))

(defn expired
  "Passes on events with :state \"expired\"."
  [& children]
  (apply match :state "expired" children))

(defmulti with
  "Transforms an event by associng a set of new k:v pairs, and passes the
  result to children. Use:
  
  (with :service \"foo\" prn)
  (with {:service \"foo\" :state \"broken\"} prn)"
  (fn [& args] (map? (first args))))
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

(defmulti default 
  "Transforms an event by associng a set of new key:value pairs, wherever the
  event has a nil value for that key. Passes the result on to children. Use:
  
  (default :service \"foo\" prn)
  (default :service \"jrecursive\" :state \"chicken\"} prn)"
  (fn [& args] (map? (first args))))
(defmethod default true [defaults & children]
  (fn [event]
;    Merge on protobufs is broken; nil values aren't applied.
    (let [e (reduce (fn [m, [k, v]]
                      (if (nil? (m k)) (assoc m k v) m))
                    event defaults)]
      (call-rescue e children))))
(defmethod default false [k v & children]
  (fn [event]
    (let [e (if (nil? (event k)) (assoc event k v) event)]
      (call-rescue e children))))

(defn adjust 
  "Passes on a changed version of each event by applying f to (field event) &
  args.

  (adjust [:service str \" rate\"] ...)
 
  takes {:service \"foo\"} and emits {:service \"foo rate\"}"
  [[field f & args] & children]
  (fn [event]
    (let [value (apply f (field event) args)
          event (assoc event field value)]
      (call-rescue event children))))

(defmacro by 
  "Splits stream by field.
  Every time an event arrives with a new value of field, this macro invokes
  its child forms to return a *new*, distinct set of streams for that
  particular value.
  
  (rate 5 prn) prints a single rate for all events, once every five seconds.

  (by :host (rate 5) tracks a separate rate for *each host*, and prints each 
  one every five seconds.

  Be aware that (by) over unbounded values can result in
  *many* substreams being created, so you wouldn't want to write
  (by metric prn): you'd get a separate prn for *every* unique metric that 
  came in.

  Note that field can be a keyword like :host or :state, but you can *also* use
  any unary function for more complex sharding."
  [field & children]
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
      (when (not (<= (first r) m (last r)))
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
    'tagged (list 'when (list :tags 'event)
                  (list 'reimann.common/member? v (list :tags 'event)))
    ; Otherwise, match literal value.
    (if (= (class v) java.util.regex.Pattern)
      (list 're-find v (list (keyword k) 'event))
      (list '= v (list (keyword k) 'event)))))

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
            (concat '(or)
                       (map (fn [value] (where-test field value)) values))))

        ; Other list
        (map where-rewrite expr))

      ; Not a list
      (if (syms expr)
        ; Expr *is* a magic sym
        (list (keyword expr) 'event)
        expr))))

(defmacro where
  "Passes on events where expr is true. Expr is rewritten using where-rewrite.
  'event is bound to the event under consideration. Examples:

  ; Match any state where metric is either 1, 2, 3, or 4.
  (where (metric 1 2 3 4) ...)
  
  ; Match a state where the metric is negative AND the state is ok.
  (where (and (> 0 metric)
              (state \"ok\")) ...)

  ; Match a state which has expired.
  (where (expired? event) ...)

  ; Match a state where the host begins with web
  (where (host #\"^web\") ...)"
  [expr & children]
  (let [p (where-rewrite expr)]
    `(let [kids# [~@children]]
      (fn [event#]
       (when (let [~'event event#] ~p)
         (call-rescue event# kids#))))))

(defn update
  "Updates the given index with all states received."
  [index]
  (fn [state]
    (index/update index state)))

(defn delete-from
  "Deletes any events that pass through from the index"
  [index]
  (fn [state]
    (index/delete index state)))
