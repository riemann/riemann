(ns riemann.blueflood
  "Forwards events to Blueflood"
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.string :as s]
            [clojure.tools.logging :as logging]
            [riemann.streams :as streams]
            [riemann.config :as config]))

(def version "1.0")
(def url-template "http://%s:%s/v2.0/%s/ingest")
(def defaults
  {:ttl 2592000
   :host "localhost"
   :port "19000"
   :tenant-id "tenant-id"
   :n 100
   :dt 1})

(defn- prep-event-for-bf [ev]
  {:collectionTime (:time ev)
   :ttlInSeconds (or (:ttl ev) (defaults :ttl))
   :metricValue (:metric ev)
   :metricName (s/join "." [(:host ev) (:service ev)])})

(defn- bf-body [evs]
  (->> evs
       (map prep-event-for-bf)
       json/generate-string))

(defn log-bf-body [evs]
  (let [r (bf-body evs)]
    (logging/info "bf-body" r)
    r))

(defn blueflood-ingest-synchronous [url & children]
  (fn [evs]
    (client/post
     url
     {:body (bf-body evs)
      :content-type :json
      :accept :json
      :socket-timeout 5000
      :conn-timeout 5000
      :throw-entire-message? true})
    (streams/call-rescue evs children)))

(defn blueflood-ingest [opts & children]
  "A stream which creates a batch, optionally asynchronous, of events to 
  forward to BF

  Options:
  Parameters to Blueflood server
  :host BF hostname
  :port BF port
  :tenant-id BF tenant for this batch of metrics

  Parameters to riemann.streams/batch, (they pass through unchanged.)
  :n Max number of events in a batch
  :dt Max seconds in a batch

  Parameters to riemann.config/async-queue! (they pass through unchanged.)
  :async-queue-name Name of queue; if nil, stream is synchronous
  		    	    	   (i.e. async-queue! stream not used.)
  :threadpool-service-opts Options to riemann.service/threadpool-service
  Use:
     (blueflood-ingest {:host \"blueflood-server\" 
                        :tenant-id \"tenant\"
                        :async-queue-name :bf-queue})

  or for synchronous, just:
     (blueflood-ingest {:host \"blueflood-server\" 
                        :tenant-id \"tenant\"})"
  (let [opts (merge defaults opts)
        {:keys [n dt host port tenant-id
                async-queue-name threadpool-service-opts]} opts
        url (format url-template host port tenant-id)
        bf-stream (apply blueflood-ingest-synchronous url children)]
    (streams/where 
     ;; BF doesn't handle events with null metrics so drop them
     metric
     (streams/batch 
      n dt 
      (if async-queue-name
        (config/async-queue! async-queue-name
                             threadpool-service-opts bf-stream)
        bf-stream)))))
