(ns ^{:doc "Send events to Telegram"}
  riemann.telegram
  (:require [clj-http.client :as client]
            [clojure.string :refer [escape join]]))

(def ^:private api-url "https://api.telegram.org/bot%s/%s")

(defn- html-parse-mode []
  "Formats html message."
  (fn [e]
    (str
      "<strong>Host:</strong> " (or (:host e) "-") "\n"
      "<strong>Service:</strong> " (or (:service e) "-") "\n"
      "<strong>State:</strong> " (or (:state e) "-") "\n"
      "<strong>Metric:</strong> " (or (:metric e) "-") "\n"
      "<strong>Description:</strong> " (or (:description e) "-"))))

(defn- markdown-parse-mode []
  "Formats markdown message."
  (fn [e]
    (str
      "*Host:* " (or (:host e) "-") "\n"
      "*Service:* " (or (:service e) "-") "\n"
      "*State:* " (or (:state e) "-") "\n"
      "*Metric:* " (or (:metric e) "-") "\n"
      "*Description:* " (or (:description e) "-"))))

(defn- format-message [parse-mode event]
  "Formats a message, accepts a single
  event or a sequence of events."
  (join "\n\n"
        (map
          (if (re-matches #"(?i)html" parse-mode)
            (html-parse-mode)
            (markdown-parse-mode))
            (flatten [event]))))

(defn- post
  "POST to the Telegram API."
  [token chat_id event parse_mode]
  (client/post (format api-url token "sendMessage")
               {:form-params {:chat_id chat_id
                              :parse_mode (or parse_mode "markdown")
                              :text (format-message parse_mode event)}
                :throw-entire-message? true}))

(defn telegram
  "Send events to Telegram chat. Uses your bot token and returns a function,
  which send message through API to specified chat.

  Format event (or events) to string with markdown syntax.

  Telegram bots API documentation: https://core.telegram.org/bots/api

  Usage:

  (def token \"define_your_token\")
  (def chat_id \"0123456\")

  (streams
    (rollup 5 3600 (telegram {:token token :chat_id chat_id})))

  Example:

  (def telegram-async
    (batch 10 1
      (async-queue!
        :telegram-async                         ; A name for the forwarder
          {:queue-size     1e4                  ; 10,000 events max
           :core-pool-size 5                    ; Minimum 5 threads
           :max-pools-size 100}                 ; Maximum 100 threads
          (telegram {:token \"275347130:AAEdWBudgeQCV87O0ag9luwwFGcN2Efeqk4\"
                     :chat_id \"261032559\" }))))
  "
  [opts]
  (fn [event]
    (let [events (if (sequential? event)
                   event
                   [event])]
      (doseq [event events]
        (post (:token opts) (:chat_id opts) event (or (:parse_mode opts) "markdown"))))))
