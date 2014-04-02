(ns riemann.atom-index
  "Maintains a stateful index of events by [host, service] key. Can be queried
  to return the most recent indexed events matching some expression. Can expire
  events which have exceeded their TTL.

  Implemented using a clojure hash map"
  (:require [riemann.query :as query]
            [riemann.index :as index])
  (:use [riemann.time :only [unix-time]]
        riemann.service
        [riemann.index :only [Index default-ttl]]))


; The index accepts states and maintains a table of the most recent state for
; each unique [host, service]. It can be searched for states matching a query.


(defn atom-index
  "Create a new clojure map backed index"
  []
  (let [in (atom {})]
    (reify
      Index
      (clear [this]
        (reset! in {}))

      (delete [this event]
        (swap! in dissoc [(:host event) (:service event)]))

      (delete-exactly [this event]
        (let [key [(:host event) (:service event)]]
          (swap! in (fn [in]
                      (if (= event (get in key))
                        (dissoc in key)
                        in)))))

      (expire [this]
        (filter
          (fn [{:keys [ttl time] :or {ttl default-ttl} :as state}]
            (let [age (- (unix-time) time)]
              (when (> age ttl)
                (.delete this state)
                true)))
          (vals @in)))

      (lookup [this host service]
        (get @in [host service]))

      (search [this query-ast]
        "O(n) unless the query is for exactly a host and service"
        (if-let [[host service] (index/query-for-host-and-service query-ast)]
          (when-let [e (.lookup this host service)]
            (list e))
          (let [matching (query/fun query-ast)]
            (filter matching (vals @in)))))

      (update [this event]
        (if (= "expired" (:state event))
          (.delete this event)
          (swap! in assoc [(:host event) (:service event)] event)))


      clojure.lang.Seqable
      (seq [this]
        (seq (vals @in)))

      ServiceEquiv
      (equiv? [this other] (= (class this) (class other)))

      Service
      (conflict? [this other] false)
      (reload! [this new-core])
      (start! [this])
      (stop! [this]))))

