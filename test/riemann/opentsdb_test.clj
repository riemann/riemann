(ns riemann.opentsdb-test
  (:require [riemann.logging :as logging]
            [riemann.opentsdb :refer :all]
            [riemann.time :refer [unix-time]]
            [clojure.test :refer :all]))

(logging/init)

(deftest ^:opentsdb ^:integration opentsdb-test
         (let [k (opentsdb {:block-start true})]
           (k {:host "riemann.local"
               :service "opentsdb test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric -2
               :time (unix-time)}))

         (let [k (opentsdb {:block-start true})]
           (k {:service "opentsdb test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [k (opentsdb {:block-start true})]
           (k {:host "no-service.riemann.local"
               :state "ok"
               :description "Missing service, not transmitted"
               :metric 4
               :time (unix-time)})))
