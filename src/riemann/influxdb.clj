(ns riemann.influxdb
  "Forwards events to InfluxDB. Supports InfluxDB 0.9 or Higher"
  (:require
    [clojure.set :as set])
  (:import
   (java.util.concurrent TimeUnit)
   (javax.net.ssl SSLContext X509TrustManager HostnameVerifier)
   (java.security SecureRandom)
   (java.security.cert X509Certificate)
   (org.influxdb InfluxDB InfluxDBFactory InfluxDB$ConsistencyLevel)
   (org.influxdb.dto BatchPoints Point)))

(defn nil-or-empty-str
  [s]
  (or (nil? s) (= "" s)))

;; specific influxdb-deprecated code
(def special-fields
  "A set of event fields in Riemann with special handling logic."
  #{:host :service :time :metric :tags :ttl :precision :tag-fields})

;; specific influxdb-deprecated code
(defn event-tags
  "Generates a map of InfluxDB tags from a Riemann event. Any fields in the
  event which are named in `tag-fields` will be converted to a string key/value
  entry in the tag map."
  [tag-fields event]
  (->> (select-keys event tag-fields)
       (remove (fn [[k v]] (nil-or-empty-str v)))
       (map #(vector (name (key %)) (str (val %))))
       (into {})))

;; specific influxdb-deprecated code
(defn event-fields
  "Generates a map of InfluxDB fields from a Riemann event. The event's
  `metric` is converted to the `value` field, and any additional event fields
  which are not standard Riemann properties or in `tag-fields` will also be
  present."
  [tag-fields event]
  (let [ignored-fields (set/union special-fields tag-fields)]
    (-> event
        (->> (remove (comp ignored-fields key))
             (remove (fn [[k v]] (nil-or-empty-str v)))
             (map #(vector (name (key %)) (val %)))
             (into {}))
        (assoc "value" (:metric event)))))

(defn get-trust-manager
  "Returns an array with an instance of `X509TrustManager`
  Used for trust all certs in the influxdb `insecure` mode."
  []
  (let [trust-manager (proxy [X509TrustManager] []
                        (checkServerTrusted [_ _])
                        (checkClientTrusted [_ _] )
                        (getAcceptedIssuers [] (make-array X509Certificate 0)))]
    (into-array (list trust-manager))))

(defn get-ssl-factory
  "Get an instance of `javax.net.ssl.SSLSocketFactory`"
  []
  (let [ssl-context (SSLContext/getInstance "TLS")]
    (.init ssl-context nil (get-trust-manager) (new SecureRandom))
    (.getSocketFactory ssl-context)))

(defn get-hostname-verifier
  "Get an instance of `javax.net.ssl.HostnameVerifier`"
  []
  (let [verifier (proxy [HostnameVerifier] []
                   (verify [_ _] true))]
    verifier))

(defn get-builder
  "Returns a new okhttp3.OkHttpClient$Builder"
  [{:keys [timeout insecure]}]
  (let [builder (new okhttp3.OkHttpClient$Builder)]
    (when insecure
      (.sslSocketFactory builder (get-ssl-factory))
      (.hostnameVerifier builder (get-hostname-verifier)))
    (doto builder
      (.readTimeout timeout TimeUnit/MILLISECONDS)
      (.writeTimeout timeout TimeUnit/MILLISECONDS)
      (.connectTimeout timeout TimeUnit/MILLISECONDS))))

(defn get-client
  "Returns an `org.influxdb.InfluxDB` instance"
  [{:keys [scheme host port username password] :as opts}]
  (let [url (str scheme "://" host ":" port)]
    (InfluxDBFactory/connect url username password (get-builder opts))))

(defn get-batchpoint
  "Returns a `org.influxdb.dto.BatchPoints` instance"
  [{:keys [tags db retention consistency]}]
  (let [builder (doto (BatchPoints/database db)
                      (.consistency (InfluxDB$ConsistencyLevel/valueOf consistency)))]
    (when retention (.retentionPolicy builder retention))
    (doseq [[k v] tags] (.tag builder (name k) (str v)))
    (.build builder)))

(defn get-time-unit
  "returns a value from the TimeUnit enum depending of the `precision` parameters.
  The `precision` parameter is a keyword whose possibles values are `:seconds`, `:milliseconds` and `:microseconds`.
  Returns `TimeUnit/SECONDS` by default"
  [precision]
  (cond
    (= precision :milliseconds) TimeUnit/MILLISECONDS
    (= precision :microseconds) TimeUnit/MICROSECONDS
    (= precision :seconds) TimeUnit/SECONDS
    true TimeUnit/SECONDS))

(defn convert-time
  "Converts the `time-event` parameter (which is time second) in a new time unit specified by the `precision` parameter. It also converts the time to long.
  The `precision` parameter is a keyword whose possibles values are `:seconds`, `:milliseconds` and `:microseconds`.
  Returns time in seconds by default"
  [time-event precision]
  (cond
    (= precision :milliseconds) (long (* 1000 time-event))
    (= precision :microseconds) (long (* 1000000 time-event))
    (= precision :seconds) (long time-event)
    true (long time-event)))

(defn converts-double
  "if n if a ratio or a BigInt, converts it to double. Returns n otherwise"
  [n]
  (if (or (ratio? n) (instance? clojure.lang.BigInt n))
    (double n)
    n))

;; specific influxdb-deprecated code
(defn event->point-9
  "Converts a Riemann event into an InfluxDB Point (an instance of `org.influxdb.dto.Point`.
  The first parameter is the event. The `:precision` key of the event is used to converts the event `time` into the correct time unit (default seconds).
  The second parameter is the option map passed to the influxdb stream."
  [event opts]
  (when (and (:time event) (:service event) (:metric event))
    (let [precision (:precision event (:precision opts))
          builder (doto (Point/measurement (:service event))
                        (.time (convert-time (:time event) precision) (get-time-unit precision)))
          tag-fields (set/union (:tag-fields opts) (:tag-fields event))
          tags   (event-tags tag-fields event)
          fields (event-fields tag-fields event)]
      (doseq [[k v] tags] (.tag builder k v))
      (doseq [[k v] fields] (.field builder k (converts-double v)))
      (.build builder))))

(defn event->point
  "Converts a Riemann event into an InfluxDB Point (an instance of `org.influxdb.dto.Point`.
  The first parameter is the event.
  The `:precision` event key is used to converts the event `time` into the correct time unit (default seconds).
  The `:measurement` event key is the influxdb measurement.
  The `:influxdb-tags` event key contains all the Influxdb tags.
  The `:influxdb-fields` event key contains all the Influxdb fields.
  The second parameter is the option map passed to the influxdb stream."
  [event opts]
  (when (and (:time event) (:measurement event))
    (let [precision (:precision event (:precision opts)) ;; use seconds default
          builder (doto (Point/measurement (:measurement event))
                        (.time (convert-time (:time event) precision) (get-time-unit precision)))
          tags   (:influxdb-tags event)
          fields (:influxdb-fields event)]
      (doseq [[k v] tags] (when-not (nil-or-empty-str v) (.tag builder (name k) (str v))))
      (doseq [[k v] fields] (when-not (nil-or-empty-str v) (.field builder (name k) (converts-double v))))
      (.build builder))))

(def default-opts
  "Default influxdb options"
  {:db "riemann"
   :scheme "http"
   :version :deprecated
   :host "localhost"
   :username "root"
   :precision :seconds
   :port 8086
   :tags {}
   :tag-fields #{:host}
   :consistency "ONE"
   :timeout 5000
   :insecure false})

(defn write-batch-point
  "Write to influxdb the `batch-point` using the `connection`"
  [^InfluxDB connection batch-point]
  (.write connection batch-point))

(defn get-batchpoints
  "Create a `org.influxdb.dto.BatchPoints` for each element in the `events` list.
  `events` is a list where each element is a list of events.
  Each element contains events with the same `:db`, `:retention` and `:consistency` keys.
  These options are used to create the BatchPoints.
  `opts` are the global influxdb stream options."
  [opts events]
  (map
   (fn [events]
     (when (not-empty events)
       (let [event (first events)
             opts (merge opts ;; we can have per event :db :retention :consistency
                         (select-keys event [:db :retention :consistency]))
             ^BatchPoints batch-point (get-batchpoint opts)
             _ (doseq [event events] (.point batch-point (event->point event opts)))]
         batch-point)))
   events))

(defn partition-events
  "`events` is a list of events.
  partition `events` depending of the `db`, `retention` and `consistency` keys
  returns a list, each element being a list of events (the result of the partitioning)."
  [events]
  (->> (if (sequential? events) events (list events))
       (group-by #(select-keys % [:db :retention :consistency]))
       (vals)))

(defn influxdb-new-stream
  "Returns a function which accepts an event, or sequence of events, and writes
  them to InfluxDB.

  streams receive an event or a list of events. Each event can have these keys :
  `:measurement`     The influxdb measurement.
  `:influxdb-tags`   A map of influxdb tags. Exemple : `{:foo \"bar\"}`
  `:influxdb-fields` A map of influxdb fields. Exemple : `{:bar \"baz\"}`
  `:precision`       The time precision. Possibles values are `:seconds`, `:milliseconds` and `:microseconds` (default `:seconds`). The event `time` will be converted.
  `:db`              Name of the database to write to. (optional)
  `:retention`       Name of retention policy to use. (optional)
  `:consistency`     The InfluxDB consistency level (default: `\"ONE\"`). Possibles values are ALL, ANY, ONE, QUORUM."
  [opts]
  (let [opts (merge default-opts opts)
        connection (get-client opts)]
    (fn streams
      [events]
      (let [events-partition (partition-events events)
            batch-points (get-batchpoints opts events-partition)]
        (doseq [batch-point batch-points] (write-batch-point connection batch-point))
        batch-points))))

(defn influxdb-deprecated
  "Returns a function which accepts an event, or sequence of events, and writes
  them to InfluxDB.

  influxdb-deprecated specifics options :
  `:tag-fields`     A set of event fields to map into InfluxDB series tags.
                    (default: `#{:host}`).

  Each event can have these keys :
  `:tag-fields`     A set of event fields to map into InfluxDB series tags..
  `:precision`       The time precision. Possibles values are `:seconds`, `:milliseconds` and `:microseconds` (default `:seconds`). The event `time` will be converted."
  [opts]
  (let [opts (merge default-opts opts)
        connection (get-client opts)]
    (fn streams
      [events]
      (let [events (->> (if (sequential? events) events (list events))
                        (keep #(event->point-9 % opts)))
            ^BatchPoints batch-point (get-batchpoint opts)
            _ (doseq [event events] (.point batch-point event))]
        (write-batch-point connection batch-point)
        batch-point))))

(defn influxdb
  "Returns a function which accepts an event, or sequence of events, and writes
  them to InfluxDB as a batch of measurement points. For performance, you should
  wrap this stream with `batch` or an asynchronous queue.
  Support InfluxdbDB 0.9 and higher.
  (influxdb {:host \"influxdb.example.com\"
             :db \"my_db\"
             :user \"riemann\"
             :password \"secret\"})
  General Options:
  `:db`             Name of the database to write to. (default: `\"riemann\"`)
  `:version`        Version of InfluxDB client to use. (default: `\":deprecated\"`)
  `:scheme`         URL scheme for endpoint. (default: `\"http\"`)
  `:host`           Hostname to write points to. (default: `\"localhost\"`)
  `:port`           API port number. (default: `8086`)
  `:username`       Database user to authenticate as. (default: `\"root\"`)
  `:password`       Password to authenticate with. (optional)
  `:tags`           A common map of tags to apply to all points. (optional)
  `:retention`      Name of retention policy to use. (optional)
  `:timeout`        HTTP timeout in milliseconds. (default: `5000`)
  `:consistency`    The InfluxDB consistency level (default: `\"ONE\"`). Possibles values are ALL, ANY, ONE, QUORUM.
  `:insecure`       If scheme is https and certficate is self-signed. (optional)
  `:precision`      The time precision. Possibles values are `:seconds`, `:milliseconds` and `:microseconds` (default `:seconds`). The event `time` will be converted.

  See `influxdb-deprecated` and `influxdb-new-stream` for version-specific options."
  [opts]
  (let [opts (merge default-opts opts)]
    (if (= :new-stream (:version opts))
      (influxdb-new-stream opts)
      (influxdb-deprecated opts))))
