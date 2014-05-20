(ns riemann.slack
  "Post alerts to slack.com"
  (:require [clj-http.client :as client]
            [cheshire.core :as json])
  (:use [clojure.string :only [escape join upper-case]]))

  (defn slack_format [message]
    "Format message according to slack formatting spec."
    (escape message {\< "&lt;" \> "&gt;" \& "&amp;"}))

  (defn slack
    "Posts events into a slack.com channel using Incoming Webhooks.
    Takes your account name, webhook token, bot username and room name.
    Optionally, the name of the emoji to use as a user avatar.
    Returns a function that will post a message into slack.com room.

    (by [:service] 
      (let [alert (slack \"some_org\" \"....\" \"Riemann bot\" \"#monitoring\" \":emoji:\")]
                                                      alert))
    "
    [account_name token username room & [emoji]]
    (fn [event]
      (client/post (str "https://" account_name ".slack.com/services/hooks/incoming-webhook?token=" token)
                   {:form-params
                    {:payload (json/generate-string
                                {:text "This event requires your attention!"
                                 :attachments [{:fallback (slack_format (str "*Description:* " (:description event)
                                                                             " *Host:* " (:host event)
                                                                             " *Metric:* " (:metric event)
                                                                             " *State:* " (:state event)))
                                                :text (slack_format (or (:description event) ""))
                                                :pretext "Event properties:"
                                                :color (case (:state event)
                                                         "ok" "good"
                                                         "critical" "danger"
                                                         "warning")
                                                :fields [{:title "Host"
                                                          :value (slack_format (or (:host event) "-"))
                                                          :short true}
                                                          {:title "Service"
                                                          :value (slack_format (or (:service event) "-"))
                                                          :short true}
                                                          {:title "Metric"
                                                           :value (or (:metric event) "-")
                                                           :short true}
                                                          {:title "State"
                                                           :value (slack_format (or (:state event) "-"))
                                                           :short true}]}]
                                 :channel room
                                 :username username
                                 :icon_emoji (or emoji ":warning:")})}})))
