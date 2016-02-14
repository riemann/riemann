(ns riemann.opsgenie
  "Forwards events to OpsGenie"
  (:require [clj-http.client :as client])
  (:require [cheshire.core :as json]))

(def ^:private alerts-url
  "https://api.opsgenie.com/v1/json/alert")

(defn- post
 "Post to OpsGenie"
 [url body]
  (client/post url
                {:body body
                 :socket-timeout 5000
                 :conn-timeout 5000
                 :content-type :json
                 :accept :json
                 :throw-entire-message? true}))

(defn- message
  "Generate description based on event.
  Because service might be quite long and opsgenie limits message, it
  pulls more important info into beginning of the string"
  [event]
  (str (:host event)
       ": [" (:state event) "] "
       (:service event)))

(defn- description
  "Generate message based on event"
  [event]
  (str
   "Host: " (:host event)
   " \nService: " (:service event)
   " \nState: " (:state event)
   " \nMetric: " (:metric event)
   " \nDescription: " (:description event)))

(defn- api-alias
  "Generate OpsGenie alias based on event"
  [event]
  (hash (str (:host event) \uffff (:service event) \uffff
       (clojure.string/join \uffff (sort (:tags event))))))

(defn- create-alert
  "Create alert in OpsGenie"
  [api-key event recipients]
  (post alerts-url (json/generate-string
                    {:message (message event)
                     :description (description event)
                     :apiKey api-key
                     :alias (api-alias event)
                     :tags (clojure.string/join "," (:tags event))
                     :recipients recipients})))
(defn- close-alert
  "Close alert in OpsGenie"
  [api-key event]
  (post (str alerts-url "/close")
        (json/generate-string
          {:apiKey api-key
           :alias (api-alias event)})))

(defn opsgenie
  "Creates an OpsGenie adapter. Takes your OG service key, and returns a map of
  functions which trigger and resolve events. clojure/hash from event host, service and tags
  will be used as the alias.

  (let [og (opsgenie \"my-service-key\" \"recipient@example.com\")]
    (changed-state
      (where (state \"ok\") (:resolve og))
      (where (state \"critical\") (:trigger og))))"
  [service-key recipients]
  {:trigger     #(create-alert service-key % recipients)
   :resolve     #(close-alert service-key %)})
