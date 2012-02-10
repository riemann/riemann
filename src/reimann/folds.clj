(ns reimann.folds
  (:use [reimann.common]))

(defn sorted-sample-extract [s points]
  "Returns the events in s, sorted and taken at points."
  (if (empty? s) 
    '()
    (let [sorted (sort-by :metric_f s)
          n (count sorted)
          extract (fn [point]
                    (let [idx (min (- n 1) (int (Math/floor (* n point))))]
                      (nth sorted idx)))]
      (map extract points))))

(defn sorted-sample [s points]
  "Sample s at points, return states with service remapped to service + point."
  (map (fn [point, event]
         (assoc event :service
                (str (event :service) " " point)))
       points
       (sorted-sample-extract s points)))

(defn sum [events]
  "Adds events together. Sums metric_f, merges into last of events."
  (assoc (last events)
         :metric_f
         (reduce + (map :metric_f events))))

(defn mean [events]
  "Averages events together. Mean metric_f, merged into last of events."
  (assoc (last events)
         :metric_f
         (/ (reduce + (map :metric_f events)) (count events))))

(defn median [events]
  "Returns the median event from events, by metric_f."
  (first (sorted-sample-extract events [0.5])))

(defn minimum [events]
  "Returns the minimum event, by metric_f."
  (first (sorted-sample-extract events [0])))

(defn maximum [events]
  "Returns the maximum event, by metric_f."
  (first (sorted-sample-extract events [1])))
