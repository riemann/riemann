(ns riemann.campfire
  "Forwards events to Campfire"
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

(defn campfire
  "Creates an adaptor to forward events to campfire.
  TODO: write more here once the event formatting is better.
  TODO: tests
  TODO: fix event formatting - see pagerduty for ideas?
  Currently doesn't format events properly, but expired events are ok.
  Tested with:
  (streams
    (by [:host, :service]
      (let [camp (campfire \"token\", true, \"sub-domain\", \"room\")]
        camp)))"
  [token ssl sub-domain room-name]
  (fn [e] (cf/message (room (cf-settings token ssl sub-domain) room-name) (str e))))
