(ns riemann.influxdb
  "Forwards events to InfluxDB"
  (:require [capacitor.core :as influx])
  (:use [clojure.string :only [join split]]))

(defn influxdb
  "Returns a function which accepts an event and sends it to InfluxDB.
  Use:
  
  ;; For giving series name as the combination of :host and :service separated by dot.

  (influxdb {:host \"play.influxdb.org\" :port 8086 :series \"host.service\"})

  Options:

  :name           Name of the metric which is same as the series name.

  :db             Name of the DB of InfluxDB to push riemann-events.

  :username       Name of the user who is allowed to push metrics to this DB.

  :password       Password of the corresponding user.
 
  :series         Name of the InfluxDB time-series."

  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 8086
                     :scheme "http"
                     :username "root"
                     :password "root"
                     :db "riemann"
                     :series "service"} opts)
        client (influx/make-client opts)]
    (fn [event]
      (let [series (join "." (map (fn [e] (e event)) (vec (map (fn [k] (keyword k)) (split (:series opts) #"\.")))))]
      (when (:metric event)
        (when (:service event)
          (when (:host event)
            (influx/post-points client series [{ :name (:service event)
                                                 :host (:host event)
                                                 :state (:state event)
                                                 :value (:metric event) }]))))))))
