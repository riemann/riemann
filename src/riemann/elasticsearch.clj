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
  [credentials es-endpoint body http-options]
  (let [base-http-options (merge {:body body
                                  :content-type :json
                                  :conn-timeout 5000
                                  :socket-timeout 5000
                                  :throw-entire-message? true}
                                 http-options)
        http-options (if credentials
                       (assoc base-http-options :basic-auth credentials)
                       base-http-options)]
    (http/post es-endpoint http-options)))

(defn elasticsearch
  "Returns a function which accepts an event and sends it to
  Elasticsearch. Custom event formatter can be provided as
  optional second argument.

  Options:

  :es-endpoint     Elasticsearch, default is \"http://127.0.0.1:9200\".
  :es-index        Index name, default is \"riemann\".
  :index-suffix    Index-suffix, default is \"-yyyy.MM.dd\".
  :type            Type to send to index, default is \"event\".
  :username        Username to authenticate with.
  :password        Password to authenticate with.

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
  (let [opts (merge {:es-endpoint "http://127.0.0.1:9200"
                     :es-index "riemann"
                     :index-suffix "-yyyy.MM.dd"
                     :type "event"}
                    opts)
        event-formatter (if (first maybe-formatter) (first maybe-formatter) format-event)]
    (fn [event]
      (let [credentials (when (and (:username opts) (:password opts))
                          [(:username opts) (:password opts)])
            body (json/generate-string (event-formatter event))
            es-endpoint (format "%s/%s%s/%s"
                                (:es-endpoint opts)
                                (:es-index opts)
                                (if (empty? (:index-suffix opts))
                                  ""
                                  (time-format/unparse (time-format/formatter (:index-suffix opts)) (datetime-from-event event)))
                                (:type opts))
            http-options {}]
        (post
         credentials
         es-endpoint
         body
         http-options)))))

(defn gen-request-bulk-body-reduce
  "Reduction fn used in `gen-request-bulk-body` to generate the body request"
  [result elem]
  (str
   result
   ;;action and metadata
   (json/generate-string {(:es-action elem) (:es-metadata elem)}) "\n"
   ;; source (optional)
   (when (:es-source elem)
     (str (json/generate-string (:es-source elem)) "\n"))))

(defn gen-request-bulk-body
  "Takes a list of events, generates the body request for Elasticsearch"
  [events]
  (reduce gen-request-bulk-body-reduce "" events))

(defn default-bulk-formatter
  "Returns a function which accepts an event and formats it for the Elasticsearch bulk API.

  Options :
  :es-index        Elasticsearch index name (without suffix).
  :type            Type to send to index.
  :es-action       Elasticsearch action, for example \"index\".
  :index-suffix    Index suffix, for example \"-yyyy.MM.dd\".

  Each event received by the function can also have these keys (which override default options), and an optional `es-id` key."
  [{:keys [es-index type es-action index-suffix]}]
  (fn [event]
    (let [special-keys [:es-index :type :es-action :es-id :index-suffix :time]
          es-index  (:es-index event es-index)
          es-type   (:type event type)
          es-action (:es-action event es-action)
          es-id     (:es-id event)
          index-suffix (:index-suffix event index-suffix)
          timestamp (time-format/unparse
                     (time-format/formatters :date-time)
                     (datetime-from-event event))
          source (-> (apply dissoc event special-keys)
                     (assoc (keyword "@timestamp") timestamp))
          metadata (let [m {:_index (str es-index
                                         (time-format/unparse
                                          (time-format/formatter index-suffix)
                                          (datetime-from-event event)))
                            :_type es-type}]
                     (if es-id
                       (assoc m :_id es-id)
                       m))]
      {:es-action es-action
       :es-metadata metadata
       :es-source source})))

(defn elasticsearch-bulk
  "Returns a function which accepts an event (or a list of events) and sends it to
  Elasticsearch using the Bulk API. Custom event formatter can be provided using the `:formatter` key.
  A formatter is a function which accepts an event.
  Event time is mandatory.

  Events should have this format :

  {:es-action \"index\"
   :es-metadata {:_index \"test\"
                 :_type \"type1\"
                 :_id \"1\"}
   :es-source {:field1 \"value1\"}}

  `:es-action` is the action (create, update, index, delete), `:es-metadata` the document metadata, and `es-source` the document source.

  More informations about the Elasticsearch bulk API: https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html

  If a formatter is specified, events will be formatted using it. You can then send events not respecting the previous format if the specified formatter converts them to it.

  Options:

  :es-endpoint     Elasticsearch, default is \"http://127.0.0.1:9200\".
  :username        Username to authenticate with.
  :password        Password to authenticate with.
  :formatter       Fn taking an event and returning it with the ES Bulk API format
  :http-options    Http options (like proxy). See https://github.com/dakrone/clj-http for option list"
  [opts]
  (let [opts (merge {:es-endpoint "http://127.0.0.1:9200"} opts)]
    (fn [events]
      (let [events (let [e (if (sequential? events) events (list events))]
                     (if (:formatter opts)
                       (map (:formatter opts) e)
                       e))
            credentials (when (and (:username opts) (:password opts))
                          [(:username opts) (:password opts)])
            body (gen-request-bulk-body events)
            http-options (merge {:content-type "application/x-ndjson"}
                                (:http-options opts {}))]
        (post
         credentials
         (str (:es-endpoint opts) "/_bulk")
         body
         http-options)))))
