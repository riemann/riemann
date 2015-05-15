(ns riemann.influxdb-9-test
  (:use riemann.influxdb-9
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:influxdb-9 ^:integration influxdb-test
  (let [k (influxdb {:host (System/getenv "INFLUXDB_HOST")
                     :database "riemann_test"})]
    (k {:host "riemann.local"
        :service "influxdb test"
        :state "ok"
        :description "all clear, uh, situation normal"
        :metric -2
        :time (unix-time)})
    (k {:service "influxdb test"
        :state "ok"
        :description "all clear, uh, situation normal"
        :metric 3.14159
        :time (unix-time)})
    (k {:host "no-service.riemann.local"
        :state "ok"
        :description "Missing service, not transmitted"
        :metric 4
        :time (unix-time)})))
