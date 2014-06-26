(ns riemann.stackdriver-test
  (:use riemann.stackdriver
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:stackdriver ^:integration stackdriver-test
         (let [k (stackdriver {:block-start true})]
           (k {:host "riemann.local"
               :service "stackdriver test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric -2
               :time (unix-time)}))

         (let [k (stackdriver {:block-start true})]
           (k {:service "stackdriver test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [k (stackdriver {:block-start true})]
           (k {:host "no-service.riemann.local"
               :state "ok"
               :description "Missing service, not transmitted"
               :metric 4
               :time (unix-time)})))
