(ns riemann.folds
  "Functions for combining states."
  (:use [riemann.common]))

(defn sorted-sample-extract
  "Returns the events in seqable s, sorted and taken at each point p of points,
  where p ranges from 0 (smallest metric) to 1 (largest metric). 0.5 is the
  median event, 0.95 is the 95th' percentile event, and so forth."
  [s points]
  (if (empty? s)
    '()
    (let [sorted (sort-by :metric s)
          n (count sorted)
          extract (fn [point]
                    (let [idx (min (dec n) (int (Math/floor (* n point))))]
                      (nth sorted idx)))]
      (map extract points))))

(defn sorted-sample
  "Sample a sequence of events at points, return states with service remapped
  to service + point. For instance, (sorted-sample events [0 1]) returns a
  2-element seq of the smallest event and the biggest event, by metric. The
  first has a service which ends in \" 0\" and the second one ends in \" 1\".
  Useful for extracting histograms and percentiles."
  [s points]
  (map (fn [point, event]
         (assoc event :service
                (str (:service event) " " point)))
       points
       (sorted-sample-extract s points)))

(defn sum
  "Adds events together. Sums metric, merges into first of events."
  [events]
  (assoc (first events)
         :metric
         (reduce + (map :metric events))))

(defn difference
  "Subtracts events. Returns the first event, with its metric reduced by the
  metrics of all subsequent events."
  [events]
  (assoc (first events)
         :metric
         (reduce - (map :metric events))))

(defn product
  "Multiplies events. Returns the first event, with its metric multiplied by
  the metrics of all other events."
  [events]
  (assoc (first events)
         :metric
         (reduce * (map :metric events))))

(defn quotient
  "Divides events. Returns the first event, with its metric divided by the
  product of the metrics of all subsequent events."
  [events]
  (assoc (first events)
         :metric
         (reduce / (map :metric events))))

(defn mean
  "Averages events together. Mean metric, merged into first of events."
  [events]
  (assoc (first events)
         :metric
         (/ (reduce + (map :metric events)) (count events))))

(defn median
  "Returns the median event from events, by metric."
  [events]
  (first (sorted-sample-extract events [0.5])))

(defn minimum
  "Returns the minimum event, by metric."
  [events]
  (apply min-key :metric events))

(defn maximum
  "Returns the maximum event, by metric."
  [events]
  (apply max-key :metric events))
