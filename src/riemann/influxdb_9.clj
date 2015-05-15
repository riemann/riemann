(ns riemann.influxdb-9
  "Functions to write events as points to an InfluxDB 0.9 series cluster."
  (:require
    [cheshire.core :as json]
    [clj-http.client :as http]
    [clojure.set :as set]
    [riemann.common :refer [unix-to-iso8601]]))


(defn write-endpoint
  "Generates a URL for the write endpoint based on an InfluxDB opts map."
  [opts]
  (str (if (:tls opts) "https" "http")
       "://" (:host opts)
       \: (:port opts 8086)
       "/write"))


(defn event-tags
  "Generates a map of InfluxDB tags from a Riemann event. Any entries in the
  event which are named in `tag-keys` will be converted to a string key/value
  entry in the tag map. The event's `host` is always included."
  [tag-keys event]
  (->> (conj tag-keys :host)
       (select-keys event)
       (map #(vector (name (key %)) (str (val %))))
       (into {})))


(defn event-fields
  "Generates a map of InfluxDB fields from a Riemann event. The event's
  `metric` is converted to the `value` field, and any additional event entries
  which are not standard Riemann fields or in `tag-keys` will also be present."
  [tag-keys event]
  (let [standard-keys #{:host :service :time :metric :description :tags :ttl}
        ignored-keys (set/union standard-keys tag-keys)]
    (-> event
        (->> (remove (comp ignored-keys key))
             (map #(vector (name (key %)) (val %)))
             (into {}))
        (assoc "value" (:metric event)))))


(defn event->point
  "Converts a Riemann event into an InfluxDB point if it has a time, service,
  and metric."
  [tag-keys event]
  (when (and (:time event) (:service event) (:metric event))
    {"name" (:service event)
     "time" (unix-to-iso8601 (:time event))
     "tags" (event-tags tag-keys event)
     "fields" (event-fields tag-keys event)}))


(defn events->points
  "Converts a collection of Riemann events into InfluxDB points. Events which
  map to nil are removed from the final collection."
  [tag-keys events]
  (vec (remove nil? (map (partial event->point tag-keys) events))))


(defn influxdb
  "Returns a function which accepts an event, or sequence of events, and writes
  them to InfluxDB as a batch of measurement points. For performance, you should
  wrap this stream with `batch` or an asynchronous queue.

      (influxdb {:host \"influxdb.example.com\"
                 :database \"my_db\"
                 :user \"riemann\"
                 :password \"secret\"})

  Options:

  `:host`           Hostname to write points to.

  `:port`           API port number. (optional)

  `:tls`            Whether to write using HTTPS. (optional)

  `:database`       Name of database to write to.

  `:retention`      Name of retention policy to use. (optional)

  `:username`       Database user to authenticate as.

  `:password`       Password to authenticate with.

  `:tags`           A common map of tags to apply to all events. (optional)

  `:tag-keys`       A set of event fields to map into InfluxDB series tags.
                    (optional)"
  [opts]
  (let [write-url (write-endpoint opts)
        tag-keys (:tag-keys opts #{})
        payload-base (cond-> {"database" (:database opts)}
                       (:retention opts)
                         (assoc "retentionPolicy" (:retention opts))
                       (seq (:tags opts))
                         (assoc "tags" (:tags opts)))
        http-opts {:socket-timeout 5000 ; ms
                   :conn-timeout   5000 ; ms
                   :content-type   :json
                   :basic-auth [(:username opts)
                                (:password opts)]}]
    (fn stream
      [events]
      (let [events (if (sequential? events) events (list events))
            points (events->points tag-keys events)]
        (when-not (empty? points)
          (->> points
               (assoc payload-base "points")
               (json/generate-string)
               (assoc http-opts :body)
               (http/post write-url)))))))
