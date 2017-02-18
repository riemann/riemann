(ns riemann.netuitive
  "Forward events to Netuitive."
  (:use [clojure.string :only [join split]])
  (:require [clj-http.client :as client]
            [cheshire.core :refer [generate-string]]))

(def ^:private gateway-url "https://api.app.netuitive.com/ingest/")

(defn parsetime
   "Converts ratio time in seconds to epoch time in millis"
   [time]
   (long (* 1000 time)))

(defn netuitive-metric-name
  "Constructs a metric-name from an event."
  [event]
  (let [service (:service event)
        split-service (if service (split service #" ") [])]
    (join "." split-service)))

(defn post-datapoint
   "Post the Riemann metrics to Netuitive."
   [api-key url data]
   (let [url (str url api-key)
         http-options {:body data :content-type :json}]
      (client/post url http-options {:save-request? true :debug true :debug-body true})))

(defn generate-tag
   "Create Netuitive tag in the form name:<tag> value:true"
   [tag]
   {:name tag :value :true})

(defn generate-event
   "Structure for ingest to Netuitive as JSON"
   [event opts]
   (let [type (:type opts "Riemann")
         metric-id (netuitive-metric-name event)]
       {:id (str type ":" (:host event))
        :name (:host event)
        :type type
        :metrics [{:id metric-id}]
        :samples [{:metricId metric-id
                   :timestamp (parsetime (:time event))
                   :val (:metric event)}]
        :tags (mapv generate-tag (:tags event))}))

(defn netuitive
  "Return a function which accepts either single events or batches of
   events in a vector and sends them to Netuitive.
  Usage:
  (netuitive {:api-key \"0123456789abcdef01234567890abcde\"})
  Option:
  :api-key    Required - Netuitive's API Key for authentication.
  :url        Optional - URL to post to Netuitive ingest - defaults to Production
  :type       Optional - Arbitrary String to use as Element Type - defaults to \"Riemann\"
  Example:
  (def netuitive-forwarder
    (batch 100 1/10
      (async-queue!
        :netuitive-forwarder  ; A name for the forwarder
        {:queue-size     1e4  ; 10,000 events max
         :core-pool-size 5    ; Minimum 5 threads
         :max-pools-size 100} ; Maximum 100 threads
        (netuitive {:api-key \"0123456789abcdef01234567890abcde\" :url \"https://api.app.netuitive.com/ingest/\" :type \"Riemann\"}))))"
  [opts]
  (let [opts (merge {:api-key "netuitive-api-key"} opts)]
    (fn [event]
      (let [events (if (sequential? event) event [event])
            post-data (mapv #(generate-event % opts) events)
            json-data (generate-string post-data)]
        (post-datapoint (:api-key opts) (:url opts gateway-url) json-data)))))
