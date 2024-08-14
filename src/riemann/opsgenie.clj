(ns riemann.opsgenie
  "Forwards events to OpsGenie"
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.string :refer [join]]))

(def alerts-url
  "https://api.opsgenie.com/v2/alerts")

(defn post
  "Post to OpsGenie"
  [url body headers]
  (client/post url
               {:body body
                :headers headers
                :socket-timeout 5000
                :conn-timeout 5000
                :content-type :json
                :accept :json
                :throw-entire-message? true}))

(defn message
  "Generate description based on event.
  Because service might be quite long and opsgenie limits message, it
  pulls more important info into beginning of the string"
  [event]
  (str (:host event)
       ": [" (:state event) "] "
       (:service event)))

(defn description
  "Generate message based on event"
  [event]
  (str
   "Host: " (:host event)
   " \nService: " (:service event)
   " \nState: " (:state event)
   " \nMetric: " (:metric event)
   " \nDescription: " (:description event)))

(defn api-alias
  "Generate OpsGenie alias based on event"
  [event]
  (hash (str (:host event) \uffff (:service event) \uffff
       (join \uffff (sort (:tags event))))))

(defn default-body
  [event]
  {:message (message event)
   :description (description event)
   :alias (api-alias event)
   :user "Riemann"
   :tags (:tags event [])})

(defn create-alert
  "Create alert in OpsGenie"
  [api-key event body-fn]
  (post alerts-url
        (json/generate-string (body-fn event))
        {"Authorization" (str "GenieKey " api-key)}))

(defn close-alert
  "Close alert in OpsGenie"
  [api-key event body-fn]
  (post (str alerts-url "/" (:alias (body-fn event)) "/close?identifierType=alias")
        (json/generate-string {:user "Riemann"})
        {"Authorization" (str "GenieKey " api-key)}))

(def default-options
  {:body-fn default-body})

(defn opsgenie
  "Creates an OpsGenie adapter. Takes options, and returns a map of
  functions which trigger and resolve events.
  By default, Clojure/hash from event host, service and tags will be used as
  the alias.

  Options:

  - :api-key    Your Opsgenie API key
  - :body-fn    A function used to generate the HTTP request body. default to
  `default-body`. This function should accept an event and return a map
  containing at least the `:message` and `:alias` keys.

  ```clojure
  (let [og (opsgenie {:api-key \"my-api-key\"})]
    (changed-state
      (where (state \"ok\") (:resolve og))
      (where (state \"critical\") (:trigger og))))
  ```"
  [options]
  (assert (:api-key options))
  (let [{:keys [api-key body-fn]} (merge default-options options)]
    {:trigger     #(create-alert api-key % body-fn)
     :resolve     #(close-alert api-key % body-fn)}))
