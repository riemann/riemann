(ns riemann.influxdb-test
  (:require
    [clojure.test :refer :all]
    [riemann.influxdb :refer [influxdb]]
    [riemann.logging :as logging]
    [riemann.time :refer [unix-time]]))

(logging/init)


(deftest ^:influxdb-8 ^:integration influxdb-test-8
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


(deftest ^:influxdb-9 ^:integration influxdb-test-9
  (let [k (influxdb {:version :0.9
                     :host (System/getenv "INFLUXDB_HOST")
                     :db "riemann_test"})]
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
