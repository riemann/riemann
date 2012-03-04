(ns riemann.test.graphite
  (:use riemann.graphite)
  (:use riemann.common)
  (:require [riemann.logging :as logging])
  (:use clojure.test))

(logging/init)

(deftest percentiles
         (is (= (graphite-path-percentiles
                  {:service "foo bar"})
                "foo.bar"))
         (is (= (graphite-path-percentiles
                  {:service "foo bar 1"})
                "foo.bar.1"))
         (is (= (graphite-path-percentiles
                  {:service "foo bar 99"})
                "foo.bar.99"))
         (is (= (graphite-path-percentiles
                  {:service "foo bar 0.99"})
                "foo.bar.99"))
         )

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
