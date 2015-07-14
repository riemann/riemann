(ns riemann.influxdb
  "Forwards events to InfluxDB. Supports both 0.8 and 0.9 APIs."
  (:require
    [capacitor.core :as capacitor]
    [cheshire.core :as json]
    [clj-http.client :as http]
    [clojure.set :as set]
    [riemann.common :refer [unix-to-iso8601]]))


;; ## Helper Functions

(def special-fields
  "A set of event fields in Riemann with special handling logic."
  #{:host :service :time :metric :tags :ttl})


(defn mark-tags
  "Marks an event with metadata that denotes a set of fields to use as
  series tags in InfluxDB. Returns the updated event."
  [fields event]
  (vary-meta event update-in [::tags] set/union (set fields)))


(defn marked-tags
  "Returns a set of the fields marked as tags in InfluxDB."
  [event]
  (::tags (meta event)))


(defn event-tags
  "Generates a map of InfluxDB tags from a Riemann event. Any fields in the
  event which are marked in the metadata will be converted to a string
  key/value entry in the tag map."
  [event]
  (->> (select-keys event (marked-tags event))
       (map #(vector (name (key %)) (str (val %))))
       (into {})))


(defn event-fields
  "Generates a map of InfluxDB fields from a Riemann event. The event's
  `metric` is converted to the `value` field, and any additional event fields
  which are not standard Riemann properties or marked as tags in the metadata
  will also be present."
  [event]
  (let [ignored-fields (set/union special-fields (marked-tags event))]
    (-> event
        (->> (remove (comp ignored-fields key))
             (remove (comp nil? val))
             (map #(vector (name (key %)) (val %)))
             (into {}))
        (assoc "value" (:metric event)))))



;; ## InfluxDB 0.8.x

(defn event->point-8
  "Transform a Riemann event to an InfluxDB point, or nil if the event is
  missing a metric or service."
  [event]
  (when (and (:metric event) (:service event))
    (merge
      {:name (:service event)
       :host (or (:host event) "")
       :time (:time event)
       :value (:metric event)}
      (apply dissoc event special-fields))))


(defn events->points-8
  "Takes a series fn that finds the series for a given event, and a sequence of
  events, and emits a map of series names to vectors of points for that series."
  [series-fn events]
  (persistent!
    (reduce (fn [m event]
              (let [series (or (series-fn event) "riemann-events")
                    point  (event->point-8 event)]
                (if point
                  (assoc! m series (conj (get m series []) point))
                  m)))
            (transient {})
            events)))


(defn influxdb-8
  "Returns a function which accepts an event, or sequence of events, and sends
  it to InfluxDB. Compatible with the 0.8.x series.

  ; For giving series name as the concatenation of :host and :service fields
  ; with dot separator.

  (influxdb-8 {:host   \"play.influxdb.org\"
               :port   8086
               :series #(str (:host %) \".\" (:service %))})

  0.8 Options:

  :name           Name of the metric which is same as the series name.

  :series         Function which takes an event and returns the InfluxDB series
                  name to use. Defaults to :service. If this function returns
                  nil, series names default to \"riemann-events\"."
  [opts]
  (let [opts (merge {:username "root"
                     :password "root"
                     :series :service}
                    opts)
        series (:series opts)
        client (capacitor/make-client opts)]

    (fn stream [events]
      (let [events (if (sequential? events) events (list events))
            points (events->points-8 series events)]
        (when-not (empty? points)
          (doseq [[series points] points]
            (capacitor/post-points client series "s" points)))))))



;; ## InfluxDB 0.9.0

(defn event->point-9
  "Converts a Riemann event into an InfluxDB point if it has a time, service,
  and metric. If the event's metadata contains a `::tags` key, it will be
  treated as a set of event attributes to use as InfluxDB series tags."
  [event]
  (when (and (:time event) (:service event) (:metric event))
    {"measurement" (:service event)
     "time" (unix-to-iso8601 (:time event))
     "tags" (event-tags event)
     "fields" (event-fields event)}))


(defn events->points-9
  "Converts one or more Riemann events into a seq of InfluxDB points. The given
  set of tag fields will be applied to every event. Events which map to `nil`
  are removed from the final collection."
  [tag-fields events]
  (-> events
      (cond->>
        (not (sequential? events))
          (list)
        (seq tag-fields)
          (map (partial mark-tags tag-fields)))
      (->>
        (map event->point-9)
        (remove nil?)
        (seq))))


(defn influxdb-9
  "Returns a function which accepts an event, or sequence of events, and writes
  them to InfluxDB. Compatible with the 0.9.x series.

  (influxdb-9 {:host \"influxdb.example.com\"
               :db \"my_db\"
               :retention \"raw\"
               :tag-fields #{:host :sys :env}})

  0.9 Options:

  `:retention`      Name of retention policy to use. (optional)

  `:tag-fields`     A set of event fields to map into InfluxDB series tags.
                    (default: `#{:host}`)

  `:tags`           A common map of tags to apply to all points. (optional)

  `:timeout`        HTTP timeout in milliseconds. (default: `5000`)"
  [opts]
  (let [write-url
        (format "%s://%s:%s/write" (:scheme opts) (:host opts) (:port opts))

        payload-base
        (cond->
          {"database" (:db opts)}
          (:retention opts)
            (assoc "retentionPolicy" (:retention opts))
          (seq (:tags opts))
            (assoc "tags" (:tags opts)))

        http-opts
        (cond->
          {:socket-timeout (:timeout opts 5000) ; ms
           :conn-timeout   (:timeout opts 5000) ; ms
           :content-type   :json}
          (:username opts)
            (assoc :basic-auth [(:username opts)
                                (:password opts)]))

        tag-fields (:tag-fields opts #{:host})]
    (fn stream
      [events]
      (when-let [points (events->points-9 tag-fields events)]
        (->> points
             (assoc payload-base "points")
             (json/generate-string)
             (assoc http-opts :body)
             (http/post write-url))))))



;; ## Stream Construction

(defn influxdb
  "Returns a function which accepts an event, or sequence of events, and writes
  them to InfluxDB as a batch of measurement points. For performance, you should
  wrap this stream with `batch` or an asynchronous queue.

  (influxdb {:host \"influxdb.example.com\"
             :db \"my_db\"
             :user \"riemann\"
             :password \"secret\"})

  General Options:

  `:version`        Version of InfluxDB client to use. Should be one of `:0.8`
                    or `:0.9`. (default: `:0.8`)

  `:db`             Name of the database to write to. (default: `\"riemann\"`)

  `:scheme`         URL scheme for endpoint. (default: `\"http\"`)

  `:host`           Hostname to write points to. (default: `\"localhost\"`)

  `:port`           API port number. (default: `8086`)

  `:username`       Database user to authenticate as. (optional)

  `:password`       Password to authenticate with. (optional)

  See `influxdb-8` and `influxdb-9` for version-specific options."
  [opts]
  (let [opts (merge {:version :0.8
                     :db "riemann"
                     :scheme "http"
                     :host "localhost"
                     :port 8086}
                    opts)]
    (case (:version opts)
      :0.8 (influxdb-8 opts)
      :0.9 (influxdb-9 opts))))
