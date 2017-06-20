(ns riemann.msteams
  "Post alerts to Microsoft Teams"
  (:require [clj-http.client :as client]
            [cheshire.core :as json])
  (:use [clojure.string :only [join]]))

(defn- default-formatter
  "Default formatter for rendering events as a message card."
  [events]
  {(keyword "@type") "MessageCard"
   (keyword "@context") "http://schema.org/extensions"
   :title "Riemann Alerting"
   :summary "Received alerts from Riemann"
   :sections 
   (map 
     (fn [event]
       {:title (str (:service event) " is " (:state event))
        :facts [
                {:name "Service"
                 :value (:service event)}
                {:name "Host"
                 :value (:host event)}
                {:name "Metric"
                 :value (:metric event)}
                {:name "State"
                 :value (:state event)}
                {:name "Description"
                 :value (:description event)}
                {:name "Tags"
                 :value (join ", " (:tags event))}
                ]})
     events)})

(defn msteams
  "Posts events to Microsoft Teams using Incoming Webhook.
  Returns a function which is invoked with a URL and an optional formatter function 
  and returns a stream. That stream is a function which takes an event or a 
  sequence of events and sends them to Microsoft Teams.
  
  (def msteams-output (msteams {:url \"https://outlook.office.com/webhook/abc/IncomingWebhook/xyz\")

  (rollup 5 60 msteams-output)

  Options:

  :url Microsoft Teams Incoming Webhook URL
  :formatter Optional message card formatting function, defaults to default-formatter

  The arity-2 version of the function accepts a map as the second argument which is 
  passed directly to clj-http and can be used to set e.g. socket and connection timeouts.
  "
  ([opts]
   (msteams opts {}))
  ([opts http-params]
   (fn [event]
     (let [events (if (sequential? event) event (list event))
           {url :url
            formatter :formatter, :or {formatter default-formatter}} opts 
           request (json/generate-string (formatter events))]
       (client/post url
                    (merge http-params
                           {:body request
                            :socket-timeout 5000
                            :conn-timeout   5000
                            :content-type :json
                            :accept :json}))))))

