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

(deftest good_event
  (let [hc (hipchat {:token api-key :room room :from alert_user :notify 0})]
    (hc {:host "localhost"
         :service "hipchat test good"
         :metric 42
         :state "ok"})))

(deftest error_event
  (let [hc (hipchat {:token api-key :room room :from alert_user :notify 0})]
    (hc {:host "localhost"
         :service "hipchat test error"
         :metric 43
         :state "error"})))

(deftest critical_event
  (let [hc (hipchat {:token api-key :room room :from alert_user :notify 0})]
    (hc {:host "localhost"
         :service "hipchat test critical"
         :metric 44
         :state "critical"})))

(deftest yellow
  (let [hc (hipchat {:token api-key :room room :from alert_user :notify 0})]
    (hc {:host "localhost"
         :service "hipchat test yellow"
         :metric 45
         :state "unknown"})))

(deftest multiple_events
  (let [hc (hipchat {:token api-key :room room :from alert_user :notify 0})]
    (hc [{:host "localhost"
          :service "hipchat multi 1"
          :metric 46
          :state "ok"}
         {:host "localhost"
          :service "hipchat multi 2"
          :metric 47
          :state "ok"}])))
