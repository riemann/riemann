(ns riemann.stackdriver
  "Forwards events to Stackdriver."
  (:require riemann.time
            [clj-http.client :as client]
            [cheshire.core :refer [generate-string]]
            [clojure.string :as str]))

(def gateway-url "https://custom-gateway.stackdriver.com/v1/custom")

(defn metric-name
  "Constructs a metric name from an event."
  [opts event]
  (let [service ((:name opts) event)]
     (str/replace service #"\s+" ".")))

(defn generate-datapoints
  "Accepts riemann event/events and converts it into equivalent stackdriver datapoint."
  [opts event-or-events]
  (->> (if (sequential? event-or-events) event-or-events (list event-or-events))
       (remove (comp nil? :metric))
       (map (fn [event]
              {:name (metric-name opts event)
               :value (:metric event)
               :collected_at (long (:time event))}))))

(defn post-datapoints
  "Post the riemann metrics datapoints."
  [api-key uri data]
  (let [http-options {:body data
                      :content-type :json
                      :headers {"x-stackdriver-apikey" api-key}}]
    (client/post uri http-options)))

(defn stackdriver
  "Returns a function which accepts an event/events and sends it to Stackdriver."
  [opts]
  (let [ts (atom 0)
        opts (merge {:api-key "stackdriver-api-key"
                     :name :service} opts)]
    (fn [event-or-events]
      (when-let [data (not-empty (generate-datapoints opts event-or-events))]
        (->>  {:timestamp (swap! ts #(max (inc %) (long (riemann.time/unix-time))))
               :proto_version 1
               :data data}
              (generate-string)
              (post-datapoints (:api-key opts) gateway-url))))))
