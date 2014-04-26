(ns riemann.folds
  "Functions for combining states.
  
  Folds usually come in two variants: a friendly version like sum, and a strict
  version like sum*. Strict variants will throw when one of their events is
  nil, missing a metric, or otherwise invalid. In typical use, however, you
  won't *have* all the necessary information to pass on an event. Friendly
  variants will do their best to ignore these error conditions where sensible,
  returning partially complete events or nil instead of throwing.
  
  Called with an empty list, folds which would return a single event return
  nil."
  (:use [riemann.common])
  (:refer-clojure :exclude [count]))

(defn sorted-sample-extract
  "Returns the events in seqable s, sorted and taken at each point p of points,
  where p ranges from 0 (smallest metric) to 1 (largest metric). 0.5 is the
  median event, 0.95 is the 95th' percentile event, and so forth. Ignores
  events without a metric."
  [s points]
  (let [sorted (sort-by :metric (filter :metric s))]
    (if (empty? sorted)
      '()
      (let [n (clojure.core/count sorted)
            extract (fn [point]
                      (let [idx (min (dec n) (int (Math/floor (* n point))))]
                        (nth sorted idx)))]
        (map extract points)))))

(defn sorted-sample
  "Sample a sequence of events at points. Returns events with service remapped
  to (str service \" \" point). For instance, (sorted-sample events [0 1])
  returns a 2-element seq of the smallest event and the biggest event, by
  metric. The first has a service which ends in \" 0\" and the second one ends
  in \" 1\".  Useful for extracting histograms and percentiles.
  
  When s is empty, returns an empty list."
  [s points]
  (map (fn [point event]
         (assoc event :service
                (str (:service event) " " point)))
       points
       (sorted-sample-extract s points)))

(defn non-nil-metrics
  "Given a sequence of events, returns a compact sequence of their
  metrics--that is, omits any events which are nil, or have nil metrics."
  [events]
  (keep (fn [event]
          (when-not (nil? event)
            (:metric event)))
        events))

(defn fold*
  "Fold with a reduction function over metrics. Throws if any event or metric
  is nil."
  [f events]
  (assoc (first events) :metric (reduce f (map :metric events))))

(defn fold
  "Fold with a reduction function over metrics. Ignores nil events and events
  with nil metrics.
  
  If there are *no* non-nil events, returns nil."
  [f events]
  (when-let [e (some identity events)]
    (assoc e :metric (reduce f (non-nil-metrics events)))))

(defn fold-all
  "Fold with a reduction function over metrics.

  If the first event has a nil :metric, or if any remaining event is nil, or
  has a nil metric, returns the first event, but with :metric nil and a
  :description of the error.
 
  If the first event is nil, returns nil."
  [f events]
  (when-let [e (first events)]
    (try
      (assoc e :metric (reduce f (map :metric events)))
      (catch NullPointerException ex
        (merge e
               {:metric nil
                :description "An event or metric was nil."})))))

(defn sum*
  "Adds events together. Sums metrics, merges into first of events."
  [events]
  (fold* + events))

(defn sum
  "Adds events together. Sums metrics, merges into first event with a metric,
  ignores nil events and nil metrics."
  [events]
  (fold + events))

(defn product*
  "Multiplies events. Returns the first event, with its metric multiplied by
  the metrics of all other events."
  [events]
  (fold* * events))

(defn product
  "Multiplies events. Returns the first event with a metric, with its metric
  being the product of all events with metrics."
  [events]
  (fold * events))

(defn difference*
  "Subtracts events. Returns the first event, with its metric reduced by the
  metrics of all subsequent events."
  [events]
  (fold* - events))

(defn difference
  "Subtracts events. Returns the first event, with its metric reduced by the
  metrics of all subsequent events with metrics. Returns nil if the first event
  is nil, or its metric is nil."
  [events]
  (fold-all - events))

(defn quotient*
  "Divides events. Returns the first event, with its metric divided by the
  product of the metrics of all subsequent events. Like quotient, but throws
  when any metric is nil or a divisor is zero."
  [events]
  (fold* / events))

(defn quotient
  "Divides events. Returns the first event, with its metric divided by the
  product of the metrics of all subsequent events."
  [events]
  (when-let [event (first events)]
    (try
      (fold-all / events)
      (catch ArithmeticException e
        (merge event
               {:metric nil
                :description "Can't divide by zero"})))))

(defn quotient-sloppy
  "Like quotient, but considers 0/0 = 0. Useful for relative rates, when you
  want the ratio of two constant values to be zero."
  [events]
  (if (and (first events)
           (zero? (:metric (first events))))
    (first events)
    (quotient events)))

(defn mean
  "Averages events together. Mean metric, merged into first of events. Ignores
  nil events and nil metrics."
  [events]
  (when-let [e (some identity events)]
    (let [metrics (non-nil-metrics events)]
      (when (seq metrics)
        (assoc e :metric (/ (reduce + metrics)
                            (clojure.core/count metrics)))))))

(defn median
  "Returns the median event from events, by metric."
  [events]
  (first (sorted-sample-extract events [0.5])))

(defn extremum
  "Returns an extreme event, by a comparison function over the metric."
  [comparison events]
  (reduce (fn [smallest event]
            (cond (nil? (:metric event))                           smallest
                  (nil? smallest)                                  event
                  (comparison (:metric event) (:metric smallest))  event
                  :else                                            smallest))
          nil
          events))

(defn minimum
  "Returns the minimum event, by metric."
  [events]
  (extremum <= events))

(defn maximum
  "Returns the maximum event, by metric."
  [events]
  (extremum >= events))

(defn std-dev
  "calculates standard deviation across a seq of events"
  [events]
  (when-let [e (some identity events)]
    (let [
      samples (non-nil-metrics events)
      n (clojure.core/count samples)
      mean (/ (reduce + samples) n)
      intermediate (map #(Math/pow (- %1 mean) 2) samples)]
      (assoc e :metric (Math/sqrt (/ (reduce + intermediate) n))))))

(defn count
  "Returns the number of events."
  [events]
  (let [events (remove nil? events)]
    (if-let [e (first events)]
      (assoc e :metric (clojure.core/count events))
      (event {:metric 0}))))
