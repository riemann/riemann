(ns reimann.folds
  (:use [reimann.common]))

; Returns the events in s, sorted and taken at points.
(defn sorted-sample-extract [s points]
  (if (empty? s) 
    '()
    (let [sorted (sort-by :metric_f s)
          n (count sorted)
          extract (fn [point]
                    (let [idx (min (- n 1) (int (Math/floor (* n point))))]
                      (nth sorted idx)))]
      (map extract points))))

; Sample s at points, return states with service remapped to service + point.
(defn sorted-sample [s points]
  (map (fn [point, event]
         (state (assoc event :service
                       (str (event :service) " " point))))
       points
       (sorted-sample-extract s points)))
