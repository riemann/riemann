(ns riemann.librato
  "Forwards events to Librato Metrics."
  (:require [clojure.string :as string])
  (:use [clj-librato.metrics :only [collate annotate connection-manager
                                    update-annotation]]
        clojure.math.numeric-tower))

(defn safe-name
  "Converts a string into a safe name for Librato's metrics and streams.
  Converts spaces to periods, preserves only A-Za-z0-9.:-_, and cuts to 255
  characters."
  [s]
  (when s
    (let [s (string/replace s " " ".")
          s (string/replace s #"[^-.:_\w]" "")]
      (subs s 0 (min 255 (count s))))))

(defn event->gauge
  "Converts an event to a gauge."
  [event]
  {:name          (safe-name (:service event))
   :source        (safe-name (:host event))
   :value         (:metric event)
   :measure-time  (round (:time event))})

(def event->counter event->gauge)

(defn event->annotation
  "Converts an event to an annotation."
  [event]
  (into {}
        (filter second
              {:name        (safe-name (:service event))
               :title       (string/join
                              " " [(:service event) (:state event)])
               :source      (safe-name (:host event))
               :description (:description event)
               :start-time  (round (:time event))
               :end-time    (when (:end-time event) (round (:end-time event)))}
              )))

(defn librato-metrics
  "Creates a librato metrics adapter. Takes your username and API key, and
  returns a map of streams:

  :gauge
  :counter
  :annotation
  :start-annotation
  :end-annotation

  Gauge and counter submit events as measurements. Annotation creates an
  annotation from the given event; it will have only a start time unless
  :end-time is given. :start-annotation will *start* an annotation; the
  annotation ID for that host and service will be remembered. :end-annotation
  will submit an end-time for the most recent annotation submitted with
  :start-annotation.

  Example:

  (def librato (librato-metrics \"aphyr@aphyr.com\" \"abcd01234...\"))

  (tagged \"latency\"
    (fixed-event-window 50 (librato :gauge)))

  (where (service \"www\")
    (changed-state
      (where (state \"ok\")
        (:start-annotation librato)
        (else
          (:end-annotation librato)))))"
  ([user api-key]
     (librato-metrics user api-key {:threads 4}))
  ([user api-key connection-mgr-options]
     (let [annotation-ids (atom {})
           http-options {:connection-manager
                         (connection-manager connection-mgr-options)}]
       {::http-options http-options
        :gauge      (fn [& args]
                      (let [data (first args)
                            events (if (vector? data) data [data])
                            gauges (map event->gauge events)]
                        (collate user api-key gauges [] http-options)
                        (last gauges)))

        :counter    (fn [& args]
                      (let [data (first args)
                            events (if (vector? data) data [data])
                            counters (map event->counter events)]
                        (collate user api-key [] counters http-options)
                        (last counters)))

        :annotation (fn [event]
                      (let [a (event->annotation event)]
                        (annotate user api-key (:name a)
                                  (dissoc a :name)
                                  http-options)))

        :start-annotation (fn [event]
                            (let [a (event->annotation event)
                                  res (annotate user api-key (:name a)
                                                (dissoc a :name)
                                                http-options)]
                              (swap! annotation-ids assoc
                                     [(:host event) (:service event)] (:id res))
                              res))

        :end-annotation (fn [event]
                          (let [id ((deref annotation-ids)
                                    [(:host event) (:service event)])
                                a (event->annotation event)]
                            (when id
                              (let [res (update-annotation
                                         user api-key (:name a) id
                                         {:end-time (round (:time event))}
                                         http-options)]
                                (swap! annotation-ids dissoc
                                       [(:host event) (:service event)])
                                res))))})))
