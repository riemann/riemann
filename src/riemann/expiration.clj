(ns riemann.expiration
  "Many places in Riemann need to understand whether the events they're working
  with are currently valid, and whether a given host/service combo has expired.
  The expiration tracker provides a stateful data structure for tracking new
  events, figuring out when expirations should be emitted, and calling back
  when they need to occur."
  (:require [riemann [time   :as time :refer [unix-time every!]]
                     [common :refer [pkey]]]
            [clojure.data.priority-map :refer [priority-map]]))

(defn expiration-time
  "When will an event expire?"
  [event]
  ; TODO: bring this back--it breaks a bunch of tests tho
  ; (assert (:time event))
  (let [time (get event :time (unix-time))]
    (if (= "expired" (:state event))
      time
      (if-let [ttl (:ttl event)]
        (+ time ttl)
        Double/POSITIVE_INFINITY))))

(defn expired?
  "Is this event expired? Events are expired if their state is \"expired\" and
  time is past, if their time + ttl is less than the current time."
  [event]
  (< (expiration-time event) (unix-time)))

(defprotocol Tracker
  (update! [t event]   "Update a tracker with a new event.")
  (expired-events! [t] "Returns a seq of expired events, which are
                        deleted from the tracker as they're yielded.")
  (shutdown! [t]       "Release resources associated with the tracker."))

(defrecord PriorityMapTracker [events task]
  ; events is an atom wrapping a priority map
  ; task is a reference to a Task for expiring events
  Tracker
  (update! [t event]
    (let [t (expiration-time event)]
      (swap! events assoc (pkey event) t)))

  (expired-events! [t]
    (let [e (atom nil)]
      (swap! events (fn puller [events]
                      (let [[[host service] expiration-time :as p]
                            (peek events)]
                        (cond ; No events
                              (nil? p)
                              events

                              ; Not today, Satan, not today!
                              (<= (time/unix-time) expiration-time)
                              events

                              ; Time to die.
                              :else
                              (do (reset! e {:host    host
                                             :service service
                                             :state   "expired"
                                             :time    expiration-time})
                                  (pop events))))))
      (when-let [expired @e]
        (cons expired (lazy-seq (expired-events! t))))))

  (shutdown! [t]
    (time/cancel @task)))

(defn tracker!
  "Constructs a new expiration tracker which expires events every
  expiration-interval seconds, calling (expired-sink event) with each."
  ([expired-sink]
   (tracker! expired-sink 1))
  ([expired-sink expiration-interval]
   (let [t (PriorityMapTracker. (atom (priority-map))
                                (promise))]
     ; TODO: cancel task when there's nothing in the state table, and recreate
     ; as necessary.
     (deliver (:task t) (time/every! expiration-interval
                                     expiration-interval
                                     (fn expire! []
                                       (doseq [e (expired-events! t)]
                                         (expired-sink e)))))
     t)))
