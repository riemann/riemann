(ns riemann.pubsub
  "Provides publish-subscribe handling of events. Publishers push events onto a
  channel, which has n subscribers. Each subscriber subscribes to a channel
  with an optional predicate function. Events which match the predicate are
  sent to the subscriber."

  (:use riemann.common))

; Registry:
;   channel1:
;     id1: fun1
;     id2: fun2
;     id3: fun3
;   channel2:
;     id4: fun1

(defn pubsub-registry
  "Returns a new pubsub registry, which tracks which subscribers are
  listening to which channels."
  []
  {:channels (ref {})
   :last-sub-id (ref 0)})

(defn publish
  "Publish an event to the given channel in a registry."
  [registry channel event]
  (let [channels (deref (:channels registry))]
    (doseq [[id f] (channels channel)]
      (f event))))

(defn subscribe
  "Subscribe to the given channel in a registry with f, which is called with
  each event that arrives on that channel. Returns an ID for the subscription."
  [registry channel f]
  (let [channels (:channels registry)]
    (dosync
      (let [sub-id (alter (:last-sub-id registry) inc)]
        (alter channels assoc-in [channel sub-id] f)
        sub-id))))

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

(defn unsubscribe
  "Unsubscribe from the given registry by id. If you provide a channel to
  unsubscribe from, O(1). If you provide only the id, O(channels)."
  ([registry channel id]
   (let [channels (:channels registry)]
     (dosync
       (alter channels dissoc-in [channel id]))))

  ([registry id]
   (let [channels (:channels registry)]
     (dosync
       (ref-set channels
                (into {} (for [[channel channel-subs] (deref channels)]
                           [channel (dissoc channel-subs id)])))))))
