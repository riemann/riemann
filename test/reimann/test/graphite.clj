(ns reimann.test.graphite
  (:use reimann.graphite)
  (:use reimann.common)
  (:use clojure.test))

(deftest ^:integration graphite-test
         (let [g (graphite {})]
           (g {:host "reimann.local"
               :service "graphite test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric_f 3.14159
               :time (unix-time)}))
         
         (let [g (graphite {})]
           (g {:service "graphite test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric_f 3.14159
               :time (unix-time)}))
         
         (let [g (graphite {})]
           (g {:host "reimann.local"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric_f 3.14159
               :time (unix-time)})))
