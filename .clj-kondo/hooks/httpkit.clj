(ns hooks.httpkit
  (:require [clj-kondo.hooks-api :as api]))

(defn with-channel
  "Analyze (org.httpkit.server/with-channel)."
  [{node :node}]
  (let [[_ req chan & body] (:children node)]
    (when-not (and req chan)
      (throw (ex-info "Missing request or channel args" {})))
    {:node (api/list-node
            (list*
              (api/token-node 'let)
              (api/vector-node [chan (api/token-node 'nil)])
              req
              body))}))
