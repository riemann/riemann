(ns riemann.elasticsearch
  "Forwards events to Elasticsearch."
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clj-time.coerce :as time-coerce]
            [clj-time.format :as time-format]))

(defn- datetime-from-event
  "Returns the datetime from event correcting (secs -> millisecs) before conversion."
  [event]
  (time-coerce/from-long (long (* 1000 (:time event)))))

(defn- format-event
  "Formats an event for Elasticsearch, drops \"description\" and re-formats \"time\"."
  [event]
  {:host (:host event)
   :service (:service event)
   :metric (:metric event)
   :state (:state event)
   :tags (:tags event)
   (keyword "@timestamp") (time-format/unparse (time-format/formatters :date-time) (datetime-from-event event))})

(defn- post
  "POST to Elasticsearch."
  [credentials esindex formatted-event]
  (let [base-http-options {:body (json/generate-string formatted-event)
                      :content-type :json
                      :conn-timeout 5000
                      :socket-timeout 5000
                      :throw-entire-message? true}
        http-options (if credentials
                  (assoc base-http-options :basic-auth credentials)
                  base-http-options)]
    (http/post esindex http-options)))

(defn elasticsearch
  "Returns a function which accepts an event and sends it to
  Elasticsearch. Custom event formatter can be provided as
  optional second argument.

  Options:

  :es-endpoint     Elasticsearch, default is \"http://127.0.0.1:9200\".
  :es-index        Index name, default is \"riemann\".
  :index-suffix    Index-suffix, default is \"-yyyy.MM.dd\".
  :type            Type to send to index, default is \"event\".
  :username        Username to authenticate with
  :password        Password to authenticate with

  Example:

  (elasticsearch
    ; ES options
    {:es-endpoint \"https:example-elastic.com\"
     :index-suffix \"-yyyy.MM\"}
    ; (optional) custom event formatter
    (fn [event]
      (let
        [newtags (concat (:tags event) [\"extra-tag\"])]
        (merge event {:tags newtags}))))"
  [opts & maybe-formatter]
  (let
    [opts (merge {:es-endpoint "http://127.0.0.1:9200"
                  :es-index "riemann"
                  :index-suffix "-yyyy.MM.dd"
                  :type "event"}
                 opts)
     event-formatter (if (first maybe-formatter) (first maybe-formatter) format-event)]

    (fn[event] (post
                 (if (and (:username opts) (:password opts))
                   [(:username opts) (:password opts)])
                 (format "%s/%s%s/%s"
                         (:es-endpoint opts)
                         (:es-index opts)
                         (time-format/unparse (time-format/formatter (:index-suffix opts)) (datetime-from-event event))
                         (:type opts))
                 (event-formatter event)))))
