(ns riemann.influxdb2-test
  (:use riemann.influxdb2
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:influxdb2 ^:integration influxdb2-test
         (let [k (influxdb2 {:block-start true})]
           (k {:host "riemann.local"
               :service "influxdb test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric -2
               :time (unix-time)}))

         (let [k (influxdb2 {:block-start true})]
           (k {:service "influxdb test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [k (influxdb2 {:block-start true})]
           (k {:host "no-service.riemann.local"
               :state "ok"
               :description "Missing service, not transmitted"
               :metric 4
               :time (unix-time)})))
