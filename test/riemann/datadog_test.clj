(ns riemann.datadog-test
  (:use riemann.datadog
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:datadog ^:integration datadog-test
         (let [k (datadog {:api-key "datadog-test-key"})]
           (k {:host "riemann.local"
               :service "datadog test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric -2
               :time (unix-time)}))

         (let [k (datadog {:api-key "datadog-test-key"})]
           (k {:service "datadog test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [k (datadog {:api-key "datadog-test-key"})]
           (k {:host "no-service.riemann.local"
               :state "ok"
               :description "Missing service, not transmitted"
               :metric 4
               :time (unix-time)}))

         (let [k (datadog {:api-key "datadog-test-key"})]
           (k [
               {:host "no-service.riemann.local"
                :state "ok"
                :description "Missing service, not transmitted"
                :metric 4
                :time (unix-time)},
               {:host "no-service.riemann.local"
                :state "ok"
                :description "Missing service, not transmitted"
                :metric 4
                :time (unix-time)}
              ])))
