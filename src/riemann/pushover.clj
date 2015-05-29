(ns riemann.pushover
  "Forwards events to Pushover."
  (:require [clj-http.client :as client]))

(def ^:private event-url
  "https://api.pushover.net/1/messages.json")

(defn- post
  "POST to Pushover."
  [token user event]
  (client/post event-url
               {:form-params
                {:token token
                 :user user
                 :title (:title event)
                 :message (:message event)}}))

(defn- format-event
  "Formats an event for Pushover"
  [event]
  {:title (str (:host event) " " (:service event))
   :message (str (:host event) " "
                 (:service event) " is "
                 (:state event) " ("
                 (:metric event) ")")})

(defn pushover
  "Returns a function which accepts an event and sends it to Pushover.
  Usage:

  (pushover \"APPLICATION_TOKEN\" \"USER_KEY\")"
  [token user]
  (fn [event]
    (post token user (format-event event))))
