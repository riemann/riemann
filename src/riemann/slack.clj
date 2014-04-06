(ns riemann.slack
  "Post alerts to slack.com"
  (:require [clj-http.client :as client]
            [cheshire.core :as json])
  (:use [clojure.string :only [escape join upper-case]]))

  (defn slack_format [message]
    "Format message according to slack formatting spec."
    (escape message {\< "&lt;" \> "&gt;"}))

  (defn slack
    "Posts events into a slack.com channel using Incoming Webhooks.
    Takes your account name, webhook token, bot username and room name.
    Returns a function that will post a message into slack.com room.

    (by [:service]
      (let [alert (slack \"some_org\" \"....\" \"Riemann bot\" \"#monitoring\")]
                                                      alert))
    "
    [account_name token username room]
    (fn [event]
      (client/post (str "https://" account_name ".slack.com/services/hooks/incoming-webhook?token=" token)
                   {:form-params
                    {:payload (json/generate-string
                                {:text (slack_format (str "*Host:* " (:host event)
                                                          " *State:* " (:state event)
                                                          " *Description:* " (:description event)
                                                          " *Metric:* " (:metric event)
                                                          ))
                                 :channel room
                                 :username username
                                 :icon_emoji ":warning:"})}})))
