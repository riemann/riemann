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

(defn generate-create-query
  "ClickHouse query for creating table"
  [opts]
  (let [database (:database opts)
        table    (:table opts)]
    (str "CREATE TABLE IF NOT EXISTS " database "." table "(
            `timestamp` DateTime,
            `host` String,
            `service` String,
            `metric` Float32,
            `tags` Array(String)
         )
         ENGINE = MergeTree
         PARTITION BY toYYYYMM(timestamp)
         ORDER BY (timestamp, host, service)
         SETTINGS index_granularity = 8192;")))

(defn generate-create-url
  "Generates the URL to which create table query should be posted."
  [opts]
  (let [scheme   (:scheme opts)
        host     (:host opts)
        port     (:port opts)
        username (:username opts)
        password (:password opts)]
    (str scheme username ":" password "@" host ":" port)))

(defn generate-insert-url
  "Generates the URL to which datapoint should be posted."
  [opts]
  (let [scheme   (:scheme opts)
        host     (:host opts)
        port     (:port opts)
        database (:database opts)
        table    (:table opts)
        username (:username opts)
        password (:password opts)
        param    (str "INSERT INTO " database "." table " FORMAT CSV")]
    (str scheme username ":" password "@" host ":" port "/?query=" param)))

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
   - `:username`       ClickHouse User Name (default: \"default\")
   - `:password`       ClickHouse Password (default: \"\")

   It will create the clickhouse table using the following query:

   CREATE TABLE IF NOT EXISTS default.riemann
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
                     :table "riemann"
                     :username "default"
                     :password ""}
                    opts)
        _ (post-datapoint (generate-create-url opts) (generate-create-query opts))]
    (fn [events]
      (let [insert-url (generate-insert-url opts)
            datapoint (generate-datapoint-batch events)]
        (when-not (nil? datapoint)
          (post-datapoint insert-url datapoint))))))
