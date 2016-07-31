(ns riemann.druid
  "Forwards events to Druid"
  (:require
    [clj-http.client :as http]
    [cheshire.core :refer [generate-string]]
    [riemann.common :refer [unix-to-iso8601]]))

(defn post-datapoint
  "Post the riemann metrics as datapoints."
  [host port dataset json-data http-opts]
  (let [scheme "http://"
        endpoint "/v1/post/"
        url (str scheme host ":" port endpoint dataset)
        http-options (assoc (merge {:conn-timeout 5000
                                    :socket-timeout 5000
                                    :throw-entire-message? true}
                                   http-opts)
                            :body json-data
                            :content-type :json)]
    (http/post url http-options)))

(defn generate-event [event]
  {:host        (:host event)
   :service     (:service event)
   :state       (:state event)
   :timestamp   (unix-to-iso8601 (:time event))
   :tags        (:tags event)
   :description (:description event)
   :value       (:metric event)})

(defn druid
  "Returns a function which accepts single events or batches of
   events in a vector and sends them to the Druid Tranquility Server.

   Usage:
   (druid {:host \"druid.example.com\"})

   Options:
   `:host`     Hostname of Druid Tranquility server. (default: `\"localhost\"`)
   `:port`     Port at which Druid Tranquility is listening (default: `8200`)
   `:dataset`  Dataset name to be given (default: `\"riemann\"`)

   Example:
   (def druid-async
   (batch 100 1/10
     (async-queue!
       :druid-async          ; A name for the forwarder
       {:queue-size     1e4  ; 10,000 events max
        :core-pool-size 5    ; Minimum 5 threads
        :max-pools-size 100} ; Maximum 100 threads
        (druid {:host \"localhost\"}))))
  "
  [opts]
  (let [opts (merge {:host     "localhost"
                     :port     8200
                     :dataset  "riemann"}
                    opts)]
    (fn [event]
      (let [events (if (sequential? event)
                     event
                     [event])
            post-data (mapv generate-event events)
            json-data (generate-string post-data)]
        (post-datapoint (:host opts) (:port opts) (:dataset opts) json-data (get opts :http-options {}))))))
