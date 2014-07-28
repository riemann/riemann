(ns riemann.campfire
  "Forwards events to Campfire"
  (:use [clojure.string :only [join upper-case]])
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]))

(defn url-for
  [settings action]
  (format "http%s://%s.campfirenow.com/%s"
          (if (:ssl settings) "s" "")
          (:subdomain settings)
          action))

(defn json-parse
  [s]
  (json/parse-string s true))

(defn get-room
  [settings room-name]
  (->> @(http/get (url-for settings "rooms.json")
                  {:basic-auth [(:token settings) "X"]})
       :body
       json-parse
       (filter #(= (:name %) room-name))
       first
       :name))

(defn post-message
  [settings room s]
  (->> @(http/post (url-for settings (format "room/%s/speak.json" room))
                   {:body (json/generate-string
                           {:message {:body s :type "TextMessage"}})
                    :basic-auth [(:token settings) "X"]
                    :headers {"Content-Type" "application/json"}})))

(defn format-message
  "Formats an event into a string"
  [{:keys [host service state description]}]
  (format "Riemann alert on %s - %s is %s - Description: %s"
          host service (upper-case (name state)) description))

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
    (let [s        (format-message e)
          settings {:api-token token :ssl ssl :sub-domain sub-domain}
          room     (get-room settings room-name)]
      (post-message settings room s))))
