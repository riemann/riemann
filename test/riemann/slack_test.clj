(ns riemann.slack-test
  (:use riemann.slack
        clojure.test)
  (:require [riemann.logging :as logging]))


(def api-key (System/getenv "SLACK_API_KEY"))
(def room (System/getenv "SLACK_ALERT_ROOM"))
(def account (System/getenv "SLACK_ALERT_ACCOUNT"))
(def user "Riemann_Slack_Test")

(when-not api-key
  (println "export SLACK_API_KEY=\"...\" to run these tests."))

(when-not room
  (println "export SLACK_ALERT_ROOM=\"...\" to run these tests."))

(when-not account
  (println "export SLACK_ALERT_ACCOUNT=\"...\" to run these tests."))

(logging/init)

(deftest ^:slack ^:integration test_event
  (let [slack_connect (slack account api-key user room)]
    (slack_connect {:host "localhost"
                    :service "good event test"
                    :description "Testing slack.com alerts from riemann"
                    :metric 42
                    :state "ok"})))
