(ns riemann.ttl-cache
  "Variation of clojure.core.cache.TTLCache which uses a priority-map to track key expiration.
   Object expiration is expected to be passed in as (:expires object)"
  (:require [clojure.core.cache :refer [defcache CacheProtocol lookup has? hit miss seed evict]]
            [clojure.data.priority-map :refer [priority-map]]
            [riemann.time :refer [unix-time]]))

(defn- key-killer [ttl now]
  (let [ks (map key (take-while #(> now (val %)) ttl))]
    #(apply dissoc % ks)))

(defcache TTLCache [cache ttl]
  CacheProtocol
  (lookup [this item]
    (let [ret (lookup this item ::nope)]
      (when-not (= ret ::nope) ret)))
  (lookup [this item not-found]
    (if (has? this item)
      (get cache item)
      not-found))
  (has? [_ item]
    (let [t (get ttl item -1)]
      (< (unix-time) t)))
  (hit [this item] this)
  (miss [this item result]
    (let [now (unix-time)
          kill-old (key-killer ttl now)]
      (TTLCache. (assoc (kill-old cache) item result)
                 (assoc (kill-old ttl) item (:expires result)))))
  (seed [_ base]
    (TTLCache. base
               (apply priority-map (for [x base] [(key x) (:expires (val x))]))))
  (evict [_ key]
    (TTLCache. (dissoc cache key)
               (dissoc ttl key)))
  Object
  (toString [_]
    (str cache \, \space ttl)))

(defn ttl-cache-factory
  "Returns a TTL cache with the cache and expiration-table initialied to `base` --
   each with the same time-to-live.
   This function also allows an optional `:ttl` argument that defines the default
   time in milliseconds that entries are allowed to reside in the cache."
  [base]
  {:pre [(map? base)]}
  (clojure.core.cache/seed (TTLCache. {} {}) base))
