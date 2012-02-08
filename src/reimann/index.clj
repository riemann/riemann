(ns reimann.index
  (:require [reimann.query :as query])
  (:use reimann.common)
  (:import (org.cliffc.high_scale_lib NonBlockingHashMap)))

; The index accepts states and maintains a table of the most recent state for
; each unique [host, service]. It can be searched for states matching a query.

(def default-ttl 60)

(defprotocol Index
  (clear [this] 
         "Resets the index")
  (delete [this event] 
          "Deletes any event with this host & service from index")
  (delete-exactly [this event] 
                  "Deletes event from index")
  (expire [this]
          "Return a seq of expired states from this index, removing each.")
  (search [this query-ast]
          "Returns a seq of events from the index matching this query AST")
  (update [this event] 
          "Updates index with event"))

(extend-protocol Index
  NonBlockingHashMap

  (clear [this]
         (.clear this))

  (delete [this event]
          (.remove this [(:host event) (:service event)]))

  (delete-exactly [this event]
                  (.remove this [(:host event) (:service event)] event))

  (expire [this]
          (filter
            (fn [state]
              (let [ttl (or (:ttl state) (default-ttl))
                    age (- (unix-time) (:time state))]
                (when (> age ttl)
                  (delete this state)
                  true)))
            (.values this)))

  (search [this query-ast]
          "O(n), sadly."
          (let [matching (query/fun query-ast)]
            (filter matching (.values this))))

  (update [this event]
          (when-not (= "expired" (:state event))
            (.put this [(:host event) (:service event)] event))))

(defn nbhm-index []
  "Create a new nonblockinghashmap backed index"
  (NonBlockingHashMap.))

(defn index []
  "Create a new index"
  (nbhm-index))
