(ns riemann.test.graphite
  (:use riemann.graphite)
  (:use riemann.common)
  (:use clojure.test))

(deftest ^:integration graphite-test
         (let [g (graphite {})]
           (g {:host "riemann.local"
               :service "graphite test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))
         
         (let [g (graphite {})]
           (g {:service "graphite test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))
         
         (let [g (graphite {})]
           (g {:host "riemann.local"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)})))
