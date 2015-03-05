(ns riemann.influxdb
  "Forwards events to InfluxDB"
  (:require [capacitor.core :as influx])
  (:use [clojure.string :only [join split]]))

(defn event->point
  "Transform a Riemann event to an InfluxDB point, or nil if the event is
  missing a metric or service."
  [event]
  (when (and (:metric event) (:service event))
    (merge
      {:name (:service event)
       :host (or (:host event) "")
       :state (:state event)
       :time (:time event)
       :value (:metric event)
       }
      (apply dissoc event [:service :host :state :metric :tags :time ]))))

(defn events->points
  "Takes a series fn that finds the series for a given event, and a sequence of
  events, and emits a map of series names to vectors of points for that series."
  [series-fn events]
  (persistent!
    (reduce (fn [m event]
              (let [series (or (series-fn event) "riemann-events")
                    point  (event->point event)]
                (if point
                  (assoc! m series (conj (get m series []) point))
                  m)))
            (transient {})
            events)))

(defn influxdb
  "Returns a function which accepts an event, or sequence of events, and sends
  it to InfluxDB.

  ; For giving series name as the concatenation of :host and :service fields
  ; with dot separator.

  (influxdb {:host   \"play.influxdb.org\"
             :port   8086
             :series #(str (:host %) \".\" (:service %))})

  Options:

  :name           Name of the metric which is same as the series name.

  :db             Name of the DB of InfluxDB to push riemann-events.

  :username       Name of the user who is allowed to push metrics to this DB.

  :password       Password of the corresponding user.

  :series         Function which takes an event and returns the InfluxDB series
                  name to use. Defaults to :service. If this function returns
                  nil, series names default to \"riemann-events\"."
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 8086
                     :scheme "http"
                     :username "root"
                     :password "root"
                     :db "riemann"
                     :series :service}
                    opts)
        series (:series opts)
        client (influx/make-client opts)]

    (fn stream [events]
      (let [events (if (sequential? events) events (list events))
            points (events->points series events)]
        (when-not (empty? points)
          (doseq [[series points] points]
            (influx/post-points client series "s" points)))))))
