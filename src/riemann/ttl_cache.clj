(ns riemann.ttl-cache
  "Variation of clojure.core.cache.TTLCache which uses a priority-map to track key expiration"
  (:require [clojure.core.cache :refer [defcache CacheProtocol lookup has? hit miss seed evict]])
  (:require [clojure.data.priority-map :refer [priority-map]]))

(defn- key-killer
  [ttl expiry now]
  (let [ks (map key (take-while #(> (- now (val %)) expiry) ttl))]
    #(apply dissoc % ks)))

(defcache TTLCache [cache ttl ttl-ms]
  CacheProtocol
  (lookup [this item]
    (let [ret (lookup this item ::nope)]
      (when-not (= ret ::nope) ret)))
  (lookup [this item not-found]
    (if (has? this item)
      (get cache item)
      not-found))
  (has? [_ item]
    (let [t (get ttl item (- ttl-ms))]
      (< (- (System/currentTimeMillis)
            t)
         ttl-ms)))
  (hit [this item] this)
  (miss [this item result]
    (let [now  (System/currentTimeMillis)
          kill-old (key-killer ttl ttl-ms now)]
      (TTLCache. (assoc (kill-old cache) item result)
                 (assoc (kill-old ttl) item now)
                 ttl-ms)))
  (seed [_ base]
    (let [now (System/currentTimeMillis)]
      (TTLCache. base
                 (apply priority-map (for [x base] [(key x) now]))
                 ttl-ms)))
  (evict [_ key]
    (TTLCache. (dissoc cache key)
               (dissoc ttl key)
               ttl-ms))
  Object
  (toString [_]
    (str cache \, \space ttl \, \space ttl-ms)))

(defn ttl-cache-factory
  "Returns a TTL cache with the cache and expiration-table initialied to `base` --
   each with the same time-to-live.
   This function also allows an optional `:ttl` argument that defines the default
   time in milliseconds that entries are allowed to reside in the cache."
  [base & {ttl :ttl :or {ttl 2000}}]
  {:pre [(number? ttl) (<= 0 ttl)
         (map? base)]}
  (clojure.core.cache/seed (TTLCache. {} {} ttl) base))
