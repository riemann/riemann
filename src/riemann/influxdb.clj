(ns riemann.influxdb
  "Forwards events to InfluxDB"
  (:require [capacitor.core :as influx]))

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
          (influx/post-points client (:service event) [{ :name (:service event)
                                                         :host (:host event)
                                                         :state (:state event)
                                                         :value (:metric event) }])))))))
