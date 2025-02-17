(ns riemann.index
  "Maintains a stateful index of events by [host, service] key. Can be queried
  to return the most recent indexed events matching some expression. Can expire
  events which have exceeded their TTL. Presently the only implementation of
  the index protocol is backed by a nonblockinghashmap, but I plan to add an
  HSQLDB backend as well.

  Indexes must extend three protocols:

  - Index: indexing and querying events
  - Seqable: returning a list of events
  - Service: lifecycle management"
  (:refer-clojure :exclude [update])
  (:require [riemann.query :as query]
            [riemann.common :refer [deprecated]]
            [riemann.instrumentation :refer [Instrumented]]
            [clojure.tools.logging :refer [error]]
            [riemann.time :refer [unix-time]]
            [riemann.service :refer [Service ServiceEquiv]])
  (:import [org.cliffc.high_scale_lib NonBlockingHashMap]))

(defprotocol Index
  (clear [this]
    "Resets the index")
  (delete [this event]
    "Deletes any event with this host & service from index. Returns the deleted
    event, or nil.")
  (delete-exactly [this event]
    "Deletes event from index. Returns the deleted event, or nil.")
  (expire [this]
    "Return a seq of expired states from this index, removing each.")
  (search [this query-ast]
    "Returns a seq of events from the index matching this query AST")
  (insert [this event]
    "Updates index with event")
  (lookup [this host service]
    "Lookup an indexed event from the index"))

; The index accepts states and maintains a table of the most recent state for
; each unique [host, service]. It can be searched for states matching a query.

(def default-ttl 60)

(defn query-for-host-and-service
  "Check if the AST is only searching for the host and service"
  [query-ast]
  (when (and (list? query-ast)
             (= 'and (first query-ast)))
    (let [and-exprs (rest query-ast)]
      (when (and (= 2 (count and-exprs))
                 (every? list? and-exprs)
                 (= 2 (count (filter #(= (first %) '=) and-exprs))))
        (let [host    (first (filter #(= (second %) :host) and-exprs))
              service (first (filter #(= (second %) :service) and-exprs))]
          (when (and host service)
            [(last host) (last service)]))))))

(defn nbhm-index
  "Create a new nonblockinghashmap backed index"
  []
  (let [hm (NonBlockingHashMap.)]
    (reify
      Index
      (clear [this]
        (.clear hm))

      (delete [this event]
        (.remove hm [(:host event) (:service event)]))

      (delete-exactly [this event]
        (.remove hm [(:host event) (:service event)] event))

      (expire [this]
        (filter
         (fn [event]
           (try
             (let [age (- (unix-time) (:time event))
                   ttl (or (:ttl event) default-ttl)]
               (when (< ttl age)
                 (delete-exactly this event)
                 true))
             (catch Exception e
               (do (error e "Caught exception while trying to expire this event"
                          event)
                   (delete-exactly this event)
                   false))))
         (.values hm)))

      (search [this query-ast]
        "O(n) unless the query is for exactly a host and service"
        (if-let [[host service] (query-for-host-and-service query-ast)]
          (when-let [e (.lookup this host service)]
            (list e))
          (let [matching (query/fun query-ast)]
            (filter matching (.values hm)))))


      (insert [this event]
        (if (= "expired" (:state event))
          (delete this event)
          (.put hm [(:host event) (:service event)] event)))

      (lookup [this host service]
        (.get hm [host service]))

      Instrumented
      (events [this]
        (let [base {:state "ok"
                    :time (unix-time)
                    :tags ["riemann"]}]
          (map (partial merge base)
               [{:service "riemann index size"
                 :metric (.size hm)}])))

      clojure.lang.Seqable
      (seq [this]
        (seq (.values hm)))

      ServiceEquiv
      (equiv? [this other] (= (class this) (class other)))

      Service
      (conflict? [this _other] false)
      (reload! [this _new-core])
      (start! [this])
      (stop! [this]))))

(defn index
  "Create a new index (currently: an nhbm index)"
  []
  (nbhm-index))

(defn update
  [index-instance event]
  (deprecated "Update is now a reserved name in clojure, please use insert!"
              (insert index-instance event)))
