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
  "Post the riemann metrics datapoints."
  [api-key data]
  (let [url (str gateway-url "?api_key=" api-key)
        http-options {:body data
                      :content-type :json}]
    (client/post url http-options)))

(defn datadog
  "Return a function which accepts event and sends it to datadog.
  Usage:

  (datadog {:api-key \"bn14a6ac2e3b5h795085217d49cde7eb\"})

  Option:

  :api-key    Datadog's API Key for authentication."
  [opts]
  (let [opts (merge {:api-key "datadog-api-key"} opts)]
    (fn [event]
      (let [post-data {:series [{:metric (datadog-metric-name event)
                                 :type "gauge"
                                 :host (:host event)
                                 :tags (:tags event)
                                 :points (generate-datapoint event)}]}
            json-data (generate-string post-data)]
        (post-datapoint (:api-key opts) json-data)))))
