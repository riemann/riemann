(ns hooks.streams
  (:require [clj-kondo.hooks-api :as api]))

;; allowed symbols in (where) block
(def where-syms '#{host service state metric metric_f time
                   ttl description tags tagged tagged-all tagged-any})

(defn where
  "Analyze (riemann.streams/where)"
  [{:keys [node]}]
  (let [[_ sym & body] (:children node)]
    (when-not (contains? where-syms (:value sym))
      (throw (ex-info (str "(where) accepts one of: " where-syms) {})))
    {:node (first body)}))
