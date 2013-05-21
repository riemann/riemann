(ns riemann.pubsub
  "Provides publish-subscribe handling of events. Publishers push events onto a
  channel, which has n subscribers. Each subscriber subscribes to a channel
  with an optional predicate function. Events which match the predicate are
  sent to the subscriber."
  (:use clojure.tools.logging)
  (:import (riemann.service Service
                            ServiceEquiv)))

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(def last-sub-id
  "The most recently assigned subscription ID."
  (atom 0))

(defn sub-id
  "Returns a new unique subscription ID."
  []
  (swap! last-sub-id inc))

(defrecord Subscription [channel id f persistent?])

(defprotocol PubSub
  "The PubSub protocol defines the interface for publishing and subscribing to
  channels; essentially, sets of named callbacks."
  (subscribe! [this channel f]
              [this channel f persistent?]
              "Subscribes to the given channel. Returns a Subscription.")

  (unsubscribe! [this sub]
                "Cancels a subscription.")

  (publish! [this channel event]
            "Publish an event to a channel.")

  (sweep! [this]
          "Shuts down all non-persistent subscriptions. Used when reloading the
          pubsub system, and we want to clear any subscriptions from the old
          streams."))

; Channels is an atom wrapping a map of channel ids to subscriptions.
(defrecord PubSubService [core channels]
  PubSub
  (subscribe! [this channel f persistent?]
              (let [id (sub-id)
                    sub (Subscription. channel id f persistent?)]
                (swap! channels assoc-in [channel id] sub)
                sub))

  (subscribe! [this channel f]
              (subscribe! this channel f false))

  (unsubscribe! [this sub]
                (swap! channels dissoc-in [(:channel sub) (:id sub)]))

  (publish! [this channel event]
            (doseq [[id ^Subscription sub] (get @channels channel)]
              ((.f sub) event)))

  (sweep! [this]
          (info "Sweeping transient subscriptions.")
          (swap! channels
                 (fn [channels]
                   (into {}
                         (map (fn [[channel sub-map]]
                                (let [sub-map (into {} (filter 
                                                         (comp :persistent? val)
                                                         sub-map))]
                                  (if (empty? sub-map)
                                    channels
                                    (assoc channels channel sub-map))))
                              channels)))))

  ; All pubsub services are equivalent; we clean out old subscriptions using
  ; sweep.
  ServiceEquiv
  (equiv? [a b] (= (class a) (class b)))

  Service
  (conflict? [a b] false)

  (start! [this])

  (reload! [this new-core]
           (locking this
             (reset! core new-core)))

  (stop! [this]
         (locking this
           (info "PubSub shutting down.")
           (reset! channels {}))))

(defn pubsub-registry
  "Returns a new pubsub registry, which tracks which subscribers are
  listening to which channels."
  []
  (PubSubService. (atom nil) (atom {})))
