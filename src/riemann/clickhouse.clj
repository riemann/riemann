(ns riemann.clickhouse
  "Forwards riemann events to ClickHouse."
  (:require [clojure.string :as str]
            [clj-http.client :as http]))

(defn generate-tags
  [tags]
  (str/replace tags #"\"" "'"))

(defn generate-datapoint
  "Accepts riemann event and converts it into clickhouse datapoint."
  [event]
  (let [timestamp (long (:time event))
        host      (:host event)
        metric    (:metric event)
        service   (:service event)
        tags      (generate-tags (:tags event))]
    (when (and host metric service)
      (str timestamp "," host "," service "," metric "," tags \newline))))

(defn generate-datapoint-batch
  "Accepts riemann events and converts it into clickhouse datapoint batch."
  [events]
  (let [processed-events (map generate-datapoint events)]
    (str/join "" processed-events)))

(defn generate-url
  "Generates the URL to which datapoint should be posted."
  [opts]
  (let [scheme   (:scheme opts)
        host     (:host opts)
        port     (:port opts)
        database (:database opts)
        table    (:table opts)]
    (str scheme host ":" port "/?query=INSERT%20INTO%20" database "." table "%20FORMAT%20CSV")))

(defn post-datapoint
  "Post the riemann event as clickhouse datapoint."
  [url datapoint]
  (let [http-options {:body                  datapoint
                      :content-type          :csv
                      :conn-timeout          5000
                      :socket-timeout        5000
                      :throw-entire-message? true}]
    (http/post url http-options)))

(defn clickhouse
  "Returns a function which accepts an event and sends it to clickhouse.

   Usage:

   (batch 10000 5 (clickhouse {:host \"play.clickhouse.com\"}))

   Options:

   - `:scheme`         ClickHouse URL Scheme (default: \"http://\")
   - `:host`           ClickHouse Server IP (default: \"localhost\")
   - `:port`           ClickHouse Server Port (default: 8123)
   - `:database`       ClickHouse Database Name (default: \"default\")
   - `:table`          ClickHouse Table Name (default: \"riemann\")

   You need to first create a clickhouse table:

   CREATE TABLE default.riemann
   (
      `timestamp` DateTime,
      `host` String,
      `service` String,
      `metric` Float32,
      `tags` Array(String)
   )
   ENGINE = MergeTree
   PARTITION BY toYYYYMM(timestamp)
   ORDER BY (timestamp, host, service)
   SETTINGS index_granularity = 8192;
  "
  [opts]
  (let [opts (merge {:scheme "http://"
                     :host "localhost"
                     :port 8123
                     :database "default"
                     :table "riemann"}
                    opts)]
    (fn [events]
      (let [url (generate-url opts)
            datapoint (generate-datapoint-batch events)]
        (if-not (nil? datapoint)
          (post-datapoint url datapoint))))))