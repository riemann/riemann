(ns ^{:doc "Send events to Telegram"}
  riemann.telegram
  (:require [clj-http.client :as client]
            [clojure.string :refer [escape join]]))

(def ^:private api-url "https://api.telegram.org/bot%s/%s")

(defn html-parse-mode
  "Formats html message."
  [e]
  (str
   "<strong>Host:</strong> " (or (:host e) "-") "\n"
   "<strong>Service:</strong> " (or (:service e) "-") "\n"
   "<strong>State:</strong> " (or (:state e) "-") "\n"
   "<strong>Metric:</strong> " (or (:metric e) "-") "\n"
   "<strong>Description:</strong> " (or (:description e) "-")))

(defn markdown-parse-mode
  "Formats markdown message."
  [e]
  (str
   "*Host:* " (or (:host e) "-") "\n"
   "*Service:* " (or (:service e) "-") "\n"
   "*State:* " (or (:state e) "-") "\n"
   "*Metric:* " (or (:metric e) "-") "\n"
   "*Description:* " (or (:description e) "-")))

(defn- format-message
  "Formats a message."
  [parse-mode message-formatter event]
  (cond
    message-formatter (message-formatter event)
    (= "HTML" parse-mode) (html-parse-mode event)
    :default (markdown-parse-mode event)))

(defn- post
  "POST to the Telegram API."
  [event {:keys [token http-options telegram-options message-formatter]
          :or {http-options {}}}]
  (let [parse-mode (:parse_mode telegram-options "Markdown")
        text (format-message parse-mode message-formatter event)
        form-params (assoc telegram-options
                           :parse_mode parse-mode
                           :text text)]
    (client/post (format api-url token "sendMessage")
                 (merge {:form-params form-params
                         :throw-entire-message? true}
                        http-options))))

(defn telegram
  "Send events to Telegram chat. Uses your bot token and returns a function,
  which send message through API to specified chat.

  Format event (or events) to string with markdown syntax by default.

  Telegram bots API documentation: https://core.telegram.org/bots/api

  Options:

  `:token`              The telegram token
  `:http-options`       clj-http extra options (optional)
  `:telegram-options`   These options are merged with the `:form-params` key of
  the request. The `:chat_id` key is mandatory.
  By default, the `:parse_mode` key is \"Markdown\".
  `:message-formatter`  A function accepting an event and returning a string. (optional).
  If not specified, `html-parse-mode` or `markdown-parse-mode` will be used,
  depending on the `:parse_mode` value.

  Usage:

  (def token \"define_your_token\")
  (def chat-id \"0123456\")

  (streams
    (telegram {:token token
               :telegram-options {:chat_id chat-id
                                  :parse_mode \"HTML\"}}))"
  [opts]
  (fn [event]
    (let [events (if (sequential? event) event [event])]
      (doseq [event events]
        (post event opts)))))
