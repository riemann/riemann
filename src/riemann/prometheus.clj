(ns riemann.prometheus
  "Forwards riemann events to Prometheus Pushgateway."
  (:require [clojure.string :as str]
            [clj-http.client :as http]))

;; Helper Functions

(defn replace-disallowed
  "Replaces all existence of space with underscore."
  [field]
  (str/escape field {\space "_", \. "_", \: "_" \- "_"}))

(defn generate-metricname
  "Generates the metric name as per prometheus specification."
  [event]
  (replace-disallowed (:service event)))

(defn generate-datapoint
  "Accepts riemann event and converts it into prometheus datapoint."
  [event]
  (when (and (:metric event) (:service event))
    (str (generate-metricname event) \space (:metric event) \newline)))


(defn generate-labels
  "Generates the Prometheus labels from Riemann tags."
  [tagv]
  (let [tagk (->> (range)
                  (-> tagv count)
                  (vec))]
    (clojure.string/join "" (map #(str "/tag" %1 "/" %2) tagk tagv))))

(defn generate-url
  "Generates the URL to which datapoint should be posted."
  [opts event]
  (let [scheme "http://"
        host   (:host opts)
        port   (:port opts)
        endp   "/metrics/job/"
        job    (:job opts)
        lhost  (str "/host/" (:host event))
        ltags  (generate-labels (:tags event))]
    (str scheme host ":" port endp job lhost ltags)))

(defn post-datapoint
  "Post the riemann metric as prometheus datapoint."
  [url datapoint]
  (let [http-options {:body datapoint
                      :content-type :json
                      :conn-timeout 5000
                      :socket-timeout 5000
                      :throw-entire-message? true}]
    (http/post url http-options)))

(defn prometheus
  "Returns a function which accepts an event and sends it to prometheus.

   Usage:
   (prometheus {:host \"prometheus.example.com\"})

   Options:
   `:host` Prometheus Pushgateway Server IP (default: \"localhost\")
   `:port` Prometheus Pushgateway Server Port (default: 9091)
   `:job`  Group Name to be assigned (default: \"riemann\")
  "
  [opts]
  (let [opts (merge {:host "localhost"
                     :port 9091
                     :job  "riemann"}
                    opts)]
    (fn [event]
      (let [url (generate-url opts event)
            datapoint (generate-datapoint event)]
        (when (and (:metric event) (:service event))
          (post-datapoint url datapoint))))))
