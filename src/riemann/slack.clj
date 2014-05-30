(ns riemann.slack
  "Post alerts to slack.com"
  (:require [clj-http.client :as client]
            [cheshire.core :as json])
  (:use [clojure.string :only [escape join upper-case]]))

(defn slack-escape [message]
  "Escape message according to slack formatting spec."
  (escape message {\< "&lt;" \> "&gt;" \& "&amp;"}))

(defn- default-formatter [events]
  {:text (str "*Host:* " (:host events)
              " *State:* " (:state events)
              " *Description:* " (:description events)
              " *Metric:* " (:metric events))})

(defn slack
  "Posts events into a slack.com channel using Incoming Webhooks.
  Takes your account name, webhook token, bot username and channel name.
  Returns a function that will post a message into slack.com channel:

  (def credentials {:account \"some_org\", :token \"53CR3T\"}
  (def slacker (slack credentials {:username \"Riemann bot\"
                                   :channel \"#monitoring\"
                                   :icon \":smile:\"}))

  (by [:service] slacker)

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
  "
  ([account_name token username channel] (slack {:account account_name, :token token}
                                                {:username username, :channel channel}))
  ([{:keys [account token]} {:keys [username channel icon formatter] :or {formatter default-formatter}}]
   (fn [events]
     (let [{:keys [text attachments] :as result} (formatter events)
           icon (:icon result (or icon ":warning:"))
           channel (:channel result channel)
           username (:username result username)]
       (client/post (str "https://" account ".slack.com/services/hooks/incoming-webhook?token=" token)
                    {:form-params
                     {:payload (json/generate-string
                                 (merge
                                  {:channel channel, :username username, :icon_emoji icon}
                                  (when text {:text (slack-escape text)})
                                  (dissoc result :icon :text)))}})))))
