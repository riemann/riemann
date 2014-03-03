(ns riemann.kairosdb-test
  (:use riemann.kairosdb
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:kairosdb ^:integration kairosdb-test
         (let [k (kairosdb {:block-start true})]
           (k {:host "riemann.local"
               :service "kairosdb test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric -2
               :time (unix-time)}))

         (let [k (kairosdb {:block-start true})]
           (k {:service "kairosdb test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [k (kairosdb {:block-start true})]
           (k {:host "no-service.riemann.local"
               :state "ok"
               :description "Missing service, not transmitted"
               :metric 4
               :time (unix-time)})))
