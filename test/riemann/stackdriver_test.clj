(ns riemann.stackdriver-test
  (:use riemann.stackdriver
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:stackdriver ^:integration stackdriver-test
         (let [k (stackdriver {:api-key "stackdriver-test-key"})]
           (k {:host "riemann.local"
               :service "stackdriver test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric -2
               :time (unix-time)}))

         (let [k (stackdriver {:api-key "stackdriver-test-key"})]
           (k {:service "stackdriver test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [k (stackdriver {:api-key "stackdriver-test-key"})]
           (k {:host "no-service.riemann.local"
               :state "ok"
               :description "Missing service, not transmitted"
               :metric 4
               :time (unix-time)}))

         (let [k (stackdriver {:api-key "stackdriver-test-key"})]
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
