(ns riemann.datadog
  "Forward events to Datadog."
  (:use [clojure.string :only [join split]])
  (:require [clj-http.client :as client]
            [cheshire.core :refer [generate-string]]))

(def ^:private gateway-url "https://app.datadoghq.com/api/v1/series")

(defn datadog-metric-name
  "Constructs a metric-name from an event."
  [event]
  (let [service (:service event)
        split-service (if service (split service #" ") [])]
    (join "." split-service)))

(defn generate-datapoint
  "Creates a vector from riemann event."
  [event]
  [(vector (:time event) (:metric event))])

(defn post-datapoint
  "Post the riemann metrics as datapoints."
  [api-key data]
  (let [url (str gateway-url "?api_key=" api-key)
        http-options {:body data
                      :content-type :json}]
    (client/post url http-options)))

(defn generate-event [event]
  {:metric (datadog-metric-name event)
   :type "gauge"
   :host (:host event)
   :tags (:tags event)
   :points (generate-datapoint event)})

(defn datadog
  "Return a function which accepts either single events or batches of
   events in a vector and sends them to datadog. Batching reduces latency
   by at least one order of magnitude and is highly recommended.
  Usage:
  (datadog {:api-key \"bn14a6ac2e3b5h795085217d49cde7eb\"})
  Option:
  :api-key    Datadog's API Key for authentication.
  Example:
  (def datadog-forwarder
    (batch 100 1/10
      (async-queue!
        :datadog-forwarder    ; A name for the forwarder
        {:queue-size     1e4  ; 10,000 events max
         :core-pool-size 5    ; Minimum 5 threads
         :max-pools-size 100} ; Maxium 100 threads
        (datadog {:api-key \"bn14a6ac2e3b5h795085217d49cde7eb\"}))))"
  [opts]
  (let [opts (merge {:api-key "datadog-api-key"} opts)]
    (fn [event]
      (let [events (if (sequential? event)
                     event
                     [event])
            post-data {:series (mapv generate-event events)}
            json-data (generate-string post-data)]
        (post-datapoint (:api-key opts) json-data)))))
