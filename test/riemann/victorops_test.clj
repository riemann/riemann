(ns riemann.victorops-test
  (:use riemann.victorops
        clojure.test)
  (:require [riemann.logging :as logging]))

(def api-key (System/getenv "VICTOROPS_API_KEY"))
(def routing-key (System/getenv "VICTOROPS_ROUTING_KEY"))

(when-not api-key
  (println "export VICTOROPS_API_KEY=\"...\" to run these tests."))

(when-not routing-key
  (println "export VICTOROPS_ROUTING_KEY=\"...\" to run these tests."))

(logging/init)

(deftest ^:victorops ^:integration test-info
  (let [vo (victorops api-key routing-key)]
    ((:info vo) {:host "localhost"
                :service "victorops info notification"
                :metric 42
                :state "info"})))

(deftest ^:victorops ^:integration test-recovery
  (let [vo (victorops api-key routing-key)]
    ((:recovery vo) {:host "localhost"
                    :service "victorops recovery notification"
                    :metric 0.5
                    :state "ok"})))

(deftest ^:victorops ^:integration test-warning
  (let [vo (victorops api-key routing-key)]
    ((:warning vo) {:host "localhost"
                    :service "victorops warning notification"
                    :metric 0.5
                    :state "warning"})))

(deftest ^:victorops ^:integration test-critical
  (let [vo (victorops api-key routing-key)]
    ((:critical vo) {:host "localhost"
                    :service "victorops critical notification"
                    :metric 0.5
                    :state "critical"})))
