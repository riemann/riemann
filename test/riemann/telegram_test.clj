(ns riemann.telegram-test
  (:use riemann.telegram
        clojure.test)
  (:require [riemann.logging :as logging]))

(def api-token (System/getenv "TELEGRAM_API_TOKEN"))
(def chat-id (System/getenv "TELEGRAM_CHAT_ID"))

(when-not api-token
  (println "export TELEGRAM_API_TOKEN=\"...\" to run these tests."))

(when-not chat-id
  (println "export TELEGRAM_CHAT_ID=\"...\" to run these tests."))

(logging/init)

(deftest ^:telegram ^:integration single_event
  (let [tg (telegram {:token api-token :chat_id chat-id})]
    (tg {:host "localhost"
         :service "telegram single"
         :description "Testing single event"
         :metric 42
         :state "ok"})))

(deftest ^:telegram ^:integration multiple_events
  (let [tg (telegram {:token api-token :chat_id chat-id})]
    (tg [{:host "localhost"
          :service "telegram multiple"
          :description "Testing multiple events"
          :metric 43
          :state "ok"}
         {:host "localhost"
          :service "telegram multiple"
          :description "Still testing multiple events"
          :metric 44
          :state "ok"}])))

(deftest ^:telegram ^:integration html_event
  (let [tg (telegram {:token api-token :chat_id chat-id :parse_mode "html"})]
    (tg {:host "localhost"
         :service "telegram html parse mode test"
         :description "Testing <b>html</b> formatted <code>event</code>"
         :metric 45
         :state "ok"})))
