(ns riemann.stackdriver
  "Forwards events to Stackdriver."
  (:require riemann.time
            [clj-http.client :as client]
            [cheshire.core :refer [generate-string]])
  (:use [clojure.string :only [replace]]))

(def gateway-url "https://custom-gateway.stackdriver.com/v1/custom")

(defn metric-name
  "Constructs a metric-name for an event."
  [opts event]
  (let [service ((:name opts) event)]
     (replace service #"\s+" ".")))

(defmulti generate-datapoint (fn [opts event] (sequential? event)))

(defmethod generate-datapoint false
  [opts event]
  (let [value (:metric event)
        service (metric-name opts event)]
    {:name service
     :value (if (nil? value) 0 value)
     :collected_at (long (:time event))}))

(defmethod generate-datapoint true
  [opts event]
  (mapv
   #(let [value (:metric %)
          service (metric-name opts %)]
     {:name service
      :value (if (nil? value) 0 value)
      :collected_at (long (:time %))}) event))

(defn post-datapoint
  "Post the riemann metrics datapoints."
  [api-key uri data]
  (let [http-options {:body data
                      :content-type :json
                      :headers {"x-stackdriver-apikey" api-key}}]
    (client/post uri http-options)))

(defn stackdriver
  "Returns a function which accepts an event and sends it to Stackdriver."
  [opts]
  (let [ts (atom 0)
        opts (merge {:api-key "stackdriver-api-key"
                     :name :service} opts)]
    (fn [event]
      (let [post-data {:timestamp (swap! ts #(max (inc %) (long (riemann.time/unix-time))))
                       :proto_version 1
                       :data (generate-datapoint opts event)}
            json-data (generate-string post-data)]
        (post-datapoint (:api-key opts) gateway-url json-data)))))
