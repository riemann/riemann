(ns riemann.prometheus
  "Forwards riemann events to Prometheus Pushgateway."
  (:require [clojure.string :as str]
            [clj-http.client :as http]))

;; Helper Functions

(def special-fields
  "A set of event fields in Riemann with special handling logic."
  #{:service :metric :tags :time :ttl})

(defn filter-event
  "Filter attributes from a Riemann event."
  [opts event]
  (->> (keys event)
       (filter #(when-not (contains? (:exclude-fields opts) %) %))
       (select-keys event)))

(defn replace-disallowed
  "Replaces all existence of disallowed characters with underscore."
  [field]
  (str/replace (str/replace field #"[^a-zA-Z0-9_]" "_") #"[_]{2,}" "_"))

(defn create-label-batch
  "Creates a Prometheus label out of a Riemann event."
  [filtered-event]
  (->> filtered-event
       (map #(when-not (nil? (second %)) (str (replace-disallowed (name (first %))) "=" (second %))))
       (str/join ",")))

(defn generate-labels-batch
  "Generates the Prometheus labels from Riemann event attributes."
  [opts event]
  (let [labels (->> event
                    (filter-event opts)
                    create-label-batch)]
    (str "{" labels "}")))

(defn generate-metricname
  "Generates the metric name as per prometheus specification."
  [event]
  (-> event
      :service
      replace-disallowed))

(defn generate-datapoint
  "Accepts riemann event and converts it into prometheus datapoint."
  [event]
  (when (and (:metric event) (:service event))
    (str (generate-metricname event) \space (float (:metric event)) \newline)))

(defn generate-datapoint-with-labels
  "Accepts riemann event and converts it into prometheus datapoint."
  [opts event]
  (when (and (:metric event) (:service event))
    (str (generate-metricname event) (generate-labels-batch opts event) \space (float (:metric event)) \newline)))

(defn generate-datapoint-batch
  "Accepts riemann event and converts it into prometheus datapoint."
  [opts events]
  (let [processed-events (map #(generate-datapoint-with-labels opts %) events)]
    (str/join "" processed-events)))

(defn create-label
  "Creates a Prometheus label out of a Riemann event."
  [filtered-event]
  (->> filtered-event
       (map #(when-not (nil? (second %)) (str "/" (replace-disallowed (name (first %))) "/" (second %))))
       (str/join "")))

(defn generate-labels
  "Generates the Prometheus labels from Riemann event attributes."
  [opts event]
  (let [instance (->> opts
                      :host
                      (str "/instance/"))
        tags (when-not (-> event
                           :tags
                           empty?) (->> (:tags event)
                                        (str/join (:separator opts))
                                        (str "/tags/")))
        labels (->> event
                    (filter-event opts)
                    create-label)]
    (str instance tags labels)))

(defn generate-url
  "Generates the URL to which datapoint should be posted."
  [opts event]
  (let [scheme "http://"
        host (:host opts)
        port (:port opts)
        endp "/metrics/job/"
        job (:job opts)
        lhost (str "/host/" (:host event))
        ltags (generate-labels opts event)]
    (str scheme host ":" port endp job lhost ltags)))

(defn generate-url-batch
  "Generates the URL to which datapoint should be posted for batch jobs."
  [opts]
  (let [scheme "http://"
        host (:host opts)
        port (:port opts)
        endp "/metrics/job/"
        job (:job opts)]
    (str scheme host ":" port endp job)))

(defn post-datapoint
  "Post the riemann metric as prometheus datapoint."
  [url datapoint]
  (let [http-options {:body                  datapoint
                      :content-type          :json
                      :conn-timeout          5000
                      :socket-timeout        5000
                      :throw-entire-message? true}]
    (http/post url http-options)))

(defn prometheus
  "Returns a function which accepts an event and sends it to prometheus.

   Usage:

   (prometheus {:host \"prometheus.example.com\"})

   Options:

   - `:host`           Prometheus Pushgateway Server IP (default: \"localhost\")
   - `:port`           Prometheus Pushgateway Server Port (default: 9091)
   - `:job`            Group Name to be assigned (default: \"riemann\")
   - `:separator`      Separator to be used for Riemann tags (default: \",\")
   - `:exclude-fields` Set of Riemann fields to exclude from Prometheus labels"
  [opts]
  (let [opts (merge {:host           "localhost"
                     :port           9091
                     :job            "riemann"
                     :separator      ","
                     :exclude-fields special-fields}
                    opts)]
    (fn [event]
      (let [url (generate-url opts event)
            datapoint (generate-datapoint event)]
        (when-not (nil? datapoint)
          (post-datapoint url datapoint))))))

(defn prometheus-batch
  "Returns a function which accepts an event and sends it to prometheus in batch.

   Usage:

   (batch 1000 5 (prometheus-batch {:host \"prometheus.example.com\"}))

   Options:

   - `:host`           Prometheus Pushgateway Server IP (default: \"localhost\")
   - `:port`           Prometheus Pushgateway Server Port (default: 9091)
   - `:job`            Group Name to be assigned (default: \"riemann\")
   - `:separator`      Separator to be used for Riemann tags (default: \",\")
   - `:exclude-fields` Set of Riemann fields to exclude from Prometheus labels"
  [opts]
  (let [opts (merge {:host           "localhost"
                     :port           9091
                     :job            "riemann"
                     :separator      ","
                     :exclude-fields special-fields}
                    opts)]
    (fn [events]
      (let [url (generate-url-batch opts)
            datapoint (generate-datapoint-batch opts events)]
        (when-not (nil? datapoint)
          (post-datapoint url datapoint))))))
