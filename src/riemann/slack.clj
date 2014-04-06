(ns riemann.slack
  "Post alerts to slack.com"
  (:require [clj-http.client :as client]
            [cheshire.core :as json])
  (:use [clojure.string :only [escape join upper-case]]))

  (defn slack_format [message]
    "Format message according to slack formatting spec."
    (escape message {\< "&lt;" \> "&gt;"}))

  ;  (client/post hook_url {:form-params
  ;     {:payload (json/write-str {:text "OMG" :channel "#alerts" :username "alerter" :icon_emoji ":warning:"})}})


  (defn slack_message
    ""
    [username channel]
    (fn [event]
      (json/generate-string
        (slack_format
          {:text (str event)
           :channel channel
           :username username
           :icon_emoji ":warning:"}))))

  (defn slack_url
    ""
    [ac_name token]
    (str "https://" ac_name ".slack.com/services/hooks/incoming-webhook?token=" token))


  (defn slack
    ""
    [account_name token username channel]
    (prn (slack_message username channel)))
  (client/post (slack_url account_name token)
               {:form-params {:payload (str (slack_message username channel ))}}))


