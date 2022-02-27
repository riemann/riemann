(ns riemann.clickhouse-test
  (:use riemann.clickhouse
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:clickhouse ^:integration clickhouse-test
  (let [k (clickhouse {:block-start true})]
    (k {:host "riemann.local"
        :service "clickhouse test"
        :state "ok"
        :description "all clear, uh, situation normal"
        :metric -2
        :time (unix-time)}))

  (let [k (clickhouse {:block-start true})]
    (k {:service "clickhouse test"
        :state "ok"
        :description "all clear, uh, situation normal"
        :metric 3.14159
        :time (unix-time)}))

  (let [k (clickhouse {:block-start true})]
    (k {:host "no-service.riemann.local"
        :state "ok"
        :description "missing service, not transmitted"
        :metric 4
        :time (unix-time)})))