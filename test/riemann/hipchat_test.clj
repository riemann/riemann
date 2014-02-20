(ns riemann.hipchat-test
  (:use riemann.hipchat
        clojure.test)
  (:require [riemann.logging :as logging]))

(def api-key (System/getenv "HIPCHAT_API_KEY"))
(def room (System/getenv "HIPCHAT_ALERT_ROOM"))
(def alert_user "Riemann_HC_Test")

(when-not api-key
  (println "export HIPCHAT_API_KEY=\"...\" to run these tests."))

(when-not room
  (println "export HIPCHAT_ALERT_ROOM=\"...\" to run these tests."))

(logging/init)

(deftest ^:hipchat ^:integration good_event
  (let [hc (hipchat {:token api-key :room room :from alert_user :notify 0})]
    (hc {:host "localhost"
         :service "hipchat test good"
         :description "Testing a metric with ok state"
         :metric 42
         :state "ok"})))

(deftest ^:hipchat ^:integration error_event
  (let [hc (hipchat {:token api-key :room room :from alert_user :notify 0})]
    (hc {:host "localhost"
         :service "hipchat test error"
         :description "Testing a metric with error state"
         :metric 43
         :state "error"})))

(deftest ^:hipchat ^:integration critical_event
  (let [hc (hipchat {:token api-key :room room :from alert_user :notify 0})]
    (hc {:host "localhost"
         :service "hipchat test critical"
         :description "Testing a metric with critical state"
         :metric 44
         :state "critical"})))

(deftest ^:hipchat ^:integration yellow
  (let [hc (hipchat {:token api-key :room room :from alert_user :notify 0})]
    (hc {:host "localhost"
         :service "hipchat test yellow"
         :description "Testing a metric with unknown state"
         :metric 45
         :state "unknown"})))

(deftest ^:hipchat ^:integration multiple_events
  (let [hc (hipchat {:token api-key :room room :from alert_user :notify 0})]
    (hc [{:host "localhost"
          :service "hipchat multi 1"
          :description "Testing multiple metrics"
          :metric 46
          :state "ok"}
         {:host "localhost"
          :service "hipchat multi 2"
          :description "Still testing multiple metrics"
          :metric 47
          :state "ok"}])))
