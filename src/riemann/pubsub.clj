(ns riemann.pubsub
  "Provides publish-subscribe handling of events. Publishers push events onto a
  channel, which has n subscribers. Each subscriber subscribes to a channel
  with an optional predicate function. Events which match the predicate are
  sent to the subscriber.")

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
  (atom {:channels    {}
         :last-sub-id 0}))

(defn publish
  "Publish an event to the given channel in a registry."
  [registry channel event]
  (let [channels (:channels @registry)]
    (doseq [[id f] (channels channel)]
      (f event))))

(defn subscribe
  "Subscribe to the given channel in a registry with f, which is called with
  each event that arrives on that channel. Returns an ID for the subscription."
  [registry channel f]
  (let [sub-id          (-> @registry :last-sub-id inc)
        inner-subscribe (fn [registry channel f]
                          (-> registry
                              (assoc-in [:channels channel sub-id] f)
                              (assoc :last-sub-id sub-id)))]
    (swap! registry inner-subscribe channel f)
    sub-id))

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
     (swap! registry dissoc-in [:channels channel id]))

  ([registry id]
     (swap! registry update-in [:channels]
            #(reduce merge (for [[chan subs] %] {chan (dissoc subs id)})))))