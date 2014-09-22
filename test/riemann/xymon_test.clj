(ns riemann.xymon-test
  (:use riemann.xymon
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:xymon ^:integration xymon-test
         (let [k (xymon nil)]
           (k {:host "riemann.local"
               :service "xymon test"
               :state "green"
               :description "all clear, uh, situation normal"
               :metric -2
               :time (unix-time)}))

         (let [k (xymon nil)]
           (k {:service "xymon test"
               :state "green"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [k (xymon nil)]
           (k {:host "riemann.local"
	       :service "xymon test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [k (xymon nil)]
           (k {:host "no-service.riemann.local"
               :state "ok"
               :description "Missing service, not transmitted"
               :metric 4
               :time (unix-time)})))
