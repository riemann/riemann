(ns riemann.telegram-test
  (:require [riemann.logging :as logging]
            [riemann.telegram :refer :all]
            [riemann.test-utils :refer [with-mock]]
            [clojure.test :refer :all]))

(def api-token (System/getenv "TELEGRAM_API_TOKEN"))
(def chat-id (System/getenv "TELEGRAM_CHAT_ID"))

(when-not api-token
  (println "export TELEGRAM_API_TOKEN=\"...\" to run these tests."))

(when-not chat-id
  (println "export TELEGRAM_CHAT_ID=\"...\" to run these tests."))

(logging/init)

(deftest ^:telegram ^:integration single-event
  (let [tg (telegram {:token api-token :chat-id chat-id})]
    (tg {:host "localhost"
         :service "telegram single"
         :description "Testing single event"
         :metric 42
         :state "ok"})))

(deftest ^:telegram ^:integration multiple-events
  (let [tg (telegram {:token api-token :chat-id chat-id})]
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

(deftest ^:telegram ^:integration html-event
  (let [tg (telegram {:token api-token :chat-id chat-id :parse-mode "HTML"})]
    (tg {:host "localhost"
         :service "telegram html parse mode test"
         :description "Testing <b>html</b> formatted <code>event</code>"
         :metric 45
         :state "ok"})))

(deftest ^:telegram telegram-test
  (with-mock [calls clj-http.client/post]
    (testing "default options"
      (let [s (telegram {:token "1234"
                         :telegram-options {:chat_id "4321"}})
            e {:host "localhost"
               :service "bar"
               :description "desc"
               :metric 45
               :state "ok"}]
        (s e)
        (is (= 1 (count @calls)))
        (let [[url {:keys [form-params]}]
              (last @calls)]
          (is (= url "https://api.telegram.org/bot1234/sendMessage"))
          (is (= form-params {:chat_id "4321"
                              :parse_mode "Markdown"
                              :text (markdown-parse-mode e)})))))
    (testing "http options and more telegram options"
      (let [s (telegram {:token "1234"
                         :http-options {:proxy-host "127.0.0.1"
                                        :proxy-port 8080}
                         :telegram-options {:chat_id "4321"
                                            :disable_notification true
                                            :parse_mode "HTML"}})
            e {:host "localhost"
               :service "bar"
               :description "desc"
               :metric 45
               :state "ok"}]
        (s e)
        (is (= 2 (count @calls)))
        (let [[url {:keys [form-params] :as req}]
              (last @calls)]
          (is (= url "https://api.telegram.org/bot1234/sendMessage"))
          (is (= (:proxy-host req) "127.0.0.1"))
          (is (= (:proxy-port req) 8080))
          (is (= form-params {:chat_id "4321"
                              :disable_notification true
                              :parse_mode "HTML"
                              :text (html-parse-mode e)})))))
    (testing "custom formatter"
      (let [formatter #(:host %)
            s (telegram {:token "1234"
                         :message-formatter formatter
                         :telegram-options {:chat_id "4321"}})
            e {:host "localhost"
               :service "bar"
               :description "desc"
               :metric 45
               :state "ok"}]
        (s e)
        (is (= 3 (count @calls)))
        (let [[url {:keys [form-params]}]
              (last @calls)]
          (is (= url "https://api.telegram.org/bot1234/sendMessage"))
          (is (= form-params {:chat_id "4321"
                              :parse_mode "Markdown"
                              :text "localhost"})))))))

