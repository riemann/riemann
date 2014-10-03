(ns riemann.cloudwatch-test
  (:use riemann.cloudwatch
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:cloudwatch ^:integration cloudwatch-test
         (let [k (cloudwatch {:access-key "aws-access-key"
                              :secret-key "aws-secret-key"})]
           (k {:host "riemann.local"
               :service "cloudwatch test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric -2
               :time (unix-time)}))

         (let [k (cloudwatch {:access-key "aws-access-key"
                              :secret-key "aws-secret-key"})]
           (k {:service "cloudwatch test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [k (cloudwatch {:access-key "aws-access-key"
                              :secret-key "aws-secret-key"})]
           (k {:host "no-service.riemann.local"
               :state "ok"
               :description "Missing service, not transmitted"
               :metric 4
               :time (unix-time)}))

         (let [k (cloudwatch {:access-key "aws-access-key"
                              :secret-key "aws-secret-key"})]
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
