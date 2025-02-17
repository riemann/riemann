(ns riemann.influxdb2
  "Forwards events to InfluxDB v2."
  (:import [com.influxdb.client InfluxDBClientFactory]
           [com.influxdb.client.domain WritePrecision]
           [com.influxdb.client.write Point]))

(defn create-client
  "Returns a `com.influxdb.client.InfluxDBClient` instance."
  [{:keys [organization bucket scheme host port token]}]
  (let [url (str scheme "://" host ":" port)]
    (InfluxDBClientFactory/create url (char-array token) organization bucket)))

(defn get-time-unit
  [precision]
  (cond
    (= precision :milliseconds) WritePrecision/MS
    (= precision :microseconds) WritePrecision/US
    (= precision :nanoseconds) WritePrecision/NS
    (= precision :seconds) WritePrecision/S
    :else WritePrecision/S))

(defn convert-time
  [time-event precision]
  (cond
    (= precision :milliseconds) (long (* 1000 time-event))
    (= precision :microseconds) (long (* 1000000 time-event))
    (= precision :nanoseconds) (long (* 1000000000 time-event))
    (= precision :seconds) (long time-event)
    :else (long time-event)))

(defn event->point
  "Returns a `com.influxdb.client.write.Point` instance."
  [event opts]
  (let [precision (:precision opts)
        point (doto (Point/measurement (:service event))
                (.addTag "host" (or (:host event) ""))
                (.addField "value" (:metric event))
                (.time (convert-time (:time event) precision) (get-time-unit precision)))]
    point))

(defn event->points
  [events opts]
  (mapv #(event->point % opts) events))

(defn post-data
  "Post datapoint to InfluxDB endpoint."
  [connection data]
  (try
    (let [api (.getWriteApiBlocking connection)]
      (.writePoints api data))
    (catch Exception e
      (println "caught exception" (.getMessage e)))))

(def default-opts
  "Default InfluxDB options"
  {:organization "riemann"
   :bucket "riemann"
   :scheme "http"
   :host "localhost"
   :port 8086
   :token "riemann"
   :precision :seconds})

(defn influxdb2
  "Returns a function which accepts an event, or sequence of events, and writes
  them to InfluxDB as a batch of measurement points. For performance, you should
  wrap this stream with `batch` or an asynchronous queue.

  Support InfluxDB 2.x and InfluxDB 1.8+.

  ```clojure
  (def influxdb2-forwarder
    (batch 100 1/10
      (influxdb2 {:host \"influxdb.example.com\"
                  :organization \"riemann\"
                  :bucket \"riemann\"
                  :token \"riemann\"})))
  ```

  General Options:

  - `:organization`   Name of the organization to write to. (default: `\"riemann\"`)
  - `:bucket`         Name of the bucket to write to. (default: `\"riemann\"`)
  - `:scheme`         URL scheme for endpoint. (default: `\"http\"`)
  - `:host`           Hostname to write points to. (default: `\"localhost\"`)
  - `:port`           Port number. (default: `8086`)
  - `:token`          Auth token to use to write data. (default: `\"riemann\"`)
  - `:precision`      The time precision. Possibles values are `:seconds`, `:milliseconds` and `:microseconds` and `:nanoseconds` (default `:seconds`).
  "
  [opts]
  (let [opts (merge default-opts opts)
        connection (create-client opts)]
    (fn [event]
      (let [events (if (sequential? event)
                     event
                     [event])
            data (event->points events opts)]
        (post-data connection data)))))
