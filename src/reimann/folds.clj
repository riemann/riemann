(ns reimann.folds
  (:use [reimann.common]))

(defn sorted-sample-extract [s points]
  "Returns the events in s, sorted and taken at points."
  (if (empty? s) 
    '()
    (let [sorted (sort-by :metric s)
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
  "Adds events together. Sums metric, merges into last of events."
  (assoc (last events)
         :metric
         (reduce + (map :metric events))))

(defn mean [events]
  "Averages events together. Mean metric, merged into last of events."
  (assoc (last events)
         :metric
         (/ (reduce + (map :metric events)) (count events))))

(defn median [events]
  "Returns the median event from events, by metric."
  (first (sorted-sample-extract events [0.5])))

(defn minimum [events]
  "Returns the minimum event, by metric."
  (first (sorted-sample-extract events [0])))

(defn maximum [events]
  "Returns the maximum event, by metric."
  (first (sorted-sample-extract events [1])))
