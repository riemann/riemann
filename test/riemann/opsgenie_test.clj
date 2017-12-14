(ns riemann.opsgenie-test
  (:require [riemann.logging :as logging]
            [riemann.opsgenie :refer :all]
            [clojure.test :refer :all]))

(def service-key (System/getenv "OPSGENIE_SERVICE_KEY"))
(def recipients (System/getenv "OPSGENIE_RECIPIENTS"))

(when-not service-key
  (println "export OPSGENIE_SERVICE_KEY=\"...\" to run these tests."))

(when-not recipients
  (println "export OPSGENIE_RECIPIENTS=\"...\" to run these tests."))

(logging/init)

(deftest ^:opsgenie ^:integration test-resolve
  (let [og (opsgenie service-key recipients)]
    ((:resolve og) {:host "localhost"
                    :service "opsgenie notification"
                    :description "Testing resolving event"
                    :metric 42
                    :state "ok"})))

(deftest ^:opsgenie ^:integration test-trigger
  (let [og (opsgenie service-key recipients)]
    ((:trigger og) {:host "localhost"
                    :service "opsgenie notification"
                    :description "Testing triggering event"
                    :metric 20
                    :state "error"})))
