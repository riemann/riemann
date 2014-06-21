(ns riemann.influxdb
  "Forwards events to InfluxDB"
  (:require [capacitor.core :as influx])
  (:use [clojure.string :only [join split]]))

(defn influxdb-series
  "Constructs a series name for an event."
  [opts event]
  ((:series opts) event))
 
(defn influxdb
  "Returns a function which accepts an event and sends it to InfluxDB.

  ;; For giving series name as the concatenation of :host and :service fields with dot separator.

  (influxdb {:host \"play.influxdb.org\" :port 8086 :series #(str (:host %) \".\" (:service %))})

  Options:

  :name           Name of the metric which is same as the series name.

  :db             Name of the DB of InfluxDB to push riemann-events.

  :username       Name of the user who is allowed to push metrics to this DB.

  :password       Password of the corresponding user.
 
  :series         Name of the InfluxDB's time-series. Default value is :service field of event,
                  incase that is nil, \"riemann-events\" will be the default name."
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 8086
                     :scheme "http"
                     :username "root"
                     :password "root"
                     :db "riemann"
                     :series :service} opts)
        client (influx/make-client opts)]
    (fn [event]
      (when (:metric event)
        (when (:service event)
          (influx/post-points client (if-let [series (influxdb-series opts event)] series "riemann-events") [{ :name (:service event)
                                                                                                               :host (if-let [hostname (:host event)] hostname "") 
                                                                                                               :state (:state event)
                                                                                                               :value (:metric event) }]))))))
