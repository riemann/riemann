(ns riemann.slack
  "Post alerts to slack.com"
  (:require [clj-http.client :as client]
            [cheshire.core :as json])
  (:use [clojure.string :only [escape join upper-case]]))


(defn slack-escape
  "Escape message according to slack formatting spec."
  [message]
  (escape message {\< "&lt;" \> "&gt;" \& "&amp;"}))

(defn slack-fallback
  "Minimal, plain-text event formatting. Used for dumb clients such as IRC."
  [events]
  (slack-escape
    (str
      "*Host:* " (or (:host events) "-")
      " *Service:* " (or (:service events) "-")
      " *State:* " (or (:state events) "-")
      " *Description:* " (or (:description events) "-")
      " *Metric:* " (or (:metric events) "-"))))


(defn default-formatter
  "Simple formatter for rendering an event as a Slack attachment."
  [events]
  {:attachments
   [{:fallback (slack-fallback events)
     :fields
     [{:title "Riemann Event"
       :value (slack-escape
                (str "Host:   " (or (:host events) "-") "\n"
                     "Service:   " (or (:service events) "-") "\n"
                     "State:   " (or (:state events) "-") "\n"
                     "Description:   " (or (:description events) "-") "\n"
                     "Metric:   " (or (:metric events) "-") "\n"
                     "Tags:   " (into [] (:tags events)) "\n"))
       :short true}]}]})


(defn extended-formatter
  "Format an event as a Slack attachment with a series of fields."
  [events]
  {:text "This event requires your attention!",
   :attachments
   [{:fallback (slack-fallback events)
     :text (slack-escape (or (:description events) "")),
     :pretext "Event Details:",
     :color
     (case (:state events) "ok" "good" "critical" "danger" "warning"),
     :fields
     [{:title "Host",
       :value (slack-escape (or (:host events) "-")),
       :short true}
      {:title "Service",
       :value (slack-escape (or (:service events) "-")),
       :short true}
      {:title "Metric", :value (or (:metric events) "-"), :short true}
      {:title "State",
       :value (slack-escape (or (:state events) "-")),
       :short true}
      {:title "Description",
       :value (slack-escape (or (:description events) "-"))
       :short true}
      {:title "Tags",
       :value (slack-escape (str (into [] (:tags events))))
       :short true}]}]})


(defn slack
  "Posts events into a slack.com channel using Incoming Webhooks.
  Takes your account name, webhook token, bot username and channel name.
  Returns a function that will post a message into slack.com channel:

  (def credentials {:account \"some_org\", :token \"53CR3T\"})
  (def slacker (slack credentials {:username \"Riemann bot\"
                                   :channel \"#monitoring\"
                                   :icon \":smile:\"}))

  (by [:service] slacker)

  Hint: token is the last part of the webhook URL that Slack gives you.
  https://hooks.slack.com/services/QWERSAFG0/AFOIUYTQ48/120984SAFJSFR
  Token in this case would be 120984SAFJSFR

  You can also supply a custom formatter for formatting events into Slack
  messages. Formatter result may contain:

    * `username` - overrides the username provided upon construction
    * `channel` - overrides the channel provided upon construction
    * `icon` - overrides the icon provided upon construction
    * `text` - main text formatted using Slack markup
    * `attachments` - array of attachments according to https://api.slack.com/docs/attachments

  (def slacker (slack credentials {:username \"Riemann bot\", :channel \"#monitoring\"
                                   :formatter (fn [e] {:text (:state e)
                                                       :icon \":happy:\"})))

  You can use `slack` inside of a grouping function which produces a seq of
  events, like `rollup`:

  (def slacker (slack credentials {:username \"Riemann bot\", :channel \"#monitoring\"
                                   :formatter (fn [es] {:text (apply str (map :state es))})))

  (rollup 5 60 slacker)

  The last parameter of the arity-3 version of the function is a map
  of parameters passed directly to clj-http. For example, you can set
  socket and connection timeouts like so:

  (slack {...} {...} {:socket-timeout 1000 :conn-timeout 1000})
  "
  ([account_name token username channel] (slack {:account account_name, :token token}
                                                {:username username, :channel channel}))
  ([account-info message-info]
   (slack account-info message-info {}))
  ([{:keys [webhook_uri account token]}
    {:keys [username channel icon formatter] :or {formatter default-formatter}}
    http-params]
   (fn [events]
     (let [{:keys [text attachments] :as result} (formatter events)
           icon (:icon result (or icon ":warning:"))
           channel (:channel result channel)
           username (:username result username)]
       (client/post (if webhook_uri
                      webhook_uri
                      (str "https://" account ".slack.com/services/hooks/incoming-webhook?token=" token))
                    (merge http-params
                           {:form-params
                            {:payload (json/generate-string
                                       (merge
                                        {:channel channel, :username username, :icon_emoji icon}
                                        (when text {:text text})
                                        (dissoc result :icon :text)))}}))))))
