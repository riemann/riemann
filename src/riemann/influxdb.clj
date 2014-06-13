(ns riemann.influxdb
  "Forwards events to InfluxDB"
  (:require [capacitor.core :as influx])
  (:use [clojure.string :only [split join]]))

(defn influxdb-metric-name
  "Constructs a metric-name for an event."
  [event]
  (let [service (:service event)
        split-service (if service (split service #" ") [])]
     (join "." split-service)))

(defn influxdb-series-name
  "Constructs a series-name for an event by picking the first word of the 
   service field of an event."
  [event]
  (let [service (:service event)]
    (first (if service (split service #" ")))))

(defn influxdb
  "Returns a function which accepts an event and sends it to InfluxDB.
  Use:

  (influxdb {:host \"play.influxdb.org\" :port 8086})

  Options:

  :name           Name of the metric which is same as the series name.
  
  :db             Name of the DB of InfluxDB to push riemann-events.

  :username       Name of the user who is allowed to push metrics to this DB.

  :password       Password of the corresponding user."
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 8086
                     :scheme "http"
                     :username "root"
                     :password "root"
                     :db "riemann" } opts)
        client (influx/make-client opts)]
    (fn [event]
    (when (:metric event)
      (when (:service event)
        (when (:host event)
          (influx/post-points client (influxdb-series-name event) [{ :name (influxdb-metric-name event)
                                                                     :host (:host event)
                                                                     :state (:state event)
                                                                     :value (:metric event) }])))))))
