(ns riemann.influxdb-test
  (:use riemann.influxdb
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:influxdb ^:integration influxdb-test
         (let [k (influxdb {:block-start true})]
           (k {:host "riemann.local"
               :service "influxdb test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric -2
               :time (unix-time)}))

         (let [k (influxdb {:block-start true})]
           (k {:service "influxdb test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [k (influxdb {:block-start true})]
           (k {:host "no-service.riemann.local"
               :state "ok"
               :description "Missing service, not transmitted"
               :metric 4
               :time (unix-time)})))