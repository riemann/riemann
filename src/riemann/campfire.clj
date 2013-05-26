(ns riemann.campfire
  "Forwards events to Campfire"
  (:use [clojure.string :only [join upper-case]])
  (:require [clj-campfire.core :as cf]))

(defn cf-settings
  "Setup settings for campfire. Required information is your api-token, ssl connection
  true or false, and your campfire sub-domain."
  [token ssl sub-domain]
  {:api-token token, :ssl ssl, :sub-domain sub-domain})

(defn room
  "Sets up the room to send events too. Pass in the settings created with cf-settings
  and the room name"
  [settings room-name]
  (cf/room-by-name settings room-name))

(defn campfire_message
  "Formats an event into a string"
  [e]
  (str (join " " ["Riemann alert on" (str (:host e)) "-" (str (:service e)) "is" (upper-case (str (:state e))) "- Description:" (str (:description e))])))

(defn campfire
  "Creates an adaptor to forward events to campfire. The campfire event will
  contain the host, state, service, metric and description.

  Tested with:
  (streams
    (by [:host, :service]
      (let [camp (campfire \"token\", true, \"sub-domain\", \"room\")]
        camp)))"
  [token ssl sub-domain room-name]
  (fn [e]
    (let [string (campfire_message e)
          settings (cf-settings token ssl sub-domain)]
      (cf/message (room settings room-name) string))))
