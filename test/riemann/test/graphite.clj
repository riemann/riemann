(ns riemann.test.graphite
  (:use riemann.graphite
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

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
         (is (= (graphite-path-percentiles
                  {:service "foo bar 0.999"})
                "foo.bar.999")))

(deftest ^:graphite ^:integration graphite-test
         (let [g (graphite {:block-start true})]
           (g {:host "riemann.local"
               :service "graphite test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric -2
               :time (unix-time)}))
         
         (let [g (graphite {:block-start true})]
           (g {:service "graphite test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))
         
         (let [g (graphite {:block-start true})]
           (g {:host "no-service.riemann.local"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 4
               :time (unix-time)})))
