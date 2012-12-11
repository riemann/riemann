(ns riemann.transport.websockets
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:require [riemann.query    :as query]
            [riemann.index    :as index]
            [riemann.pubsub   :as p])
  (:use [riemann.common        :only [event-to-json]]
        [riemann.core          :only [core]]
        [aleph.http            :only [start-http-server]]
        [lamina.core           :only [receive-all close enqueue]]
        [clojure.tools.logging :only [info warn]]
        [clj-http.util         :only [url-decode]]
        [clojure.string        :only [split]]))

(defn http-query-map
  "Converts a URL query string into a map."
  [string]
  (apply hash-map
         (map url-decode
              (mapcat (fn [kv] (split kv #"=" 2))
                      (split string #"&")))))

;;; Websockets
(defn ws-pubsub-handler [core ch hs]
  (let [topic  (url-decode (last (split (:uri hs) #"/" 3)))
        params (http-query-map (:query-string hs))
        query  (params "query")
        pred   (query/fun (query/ast query))
        sub    (p/subscribe (:pubsub core) topic
                            (fn [event]
                              (when (pred event)
                                (enqueue ch (event-to-json event)))))]
    (info "New websocket subscription to" topic ":" query)
    (receive-all ch (fn [msg]
                      (when-not msg
                        ; Shut down channel
                        (info "Closing websocket "
                              (:remote-addr hs) topic query)
                        (close ch)
                        (p/unsubscribe (:pubsub core) sub))))))

(defn ws-index-handler
  "Queries the index for events and streams them to the client. If subscribe is
  true, also initiates a pubsub subscription to the index topic with that
  query."
  [core ch hs]
  (let [params (http-query-map (:query-string hs))
        query  (params "query")
        ast    (query/ast query)]
    (when-let [i (deref (:index core))]
      (doseq [event (index/search i ast)]
        (enqueue ch (event-to-json event))))
    (if (= (params "subscribe") "true")
      (ws-pubsub-handler core ch (assoc hs :uri "/pubsub/index"))
      (close ch))))

(defn ws-handler [core]
  (fn [ch handshake]
    (info "Websocket connection from" (:remote-addr handshake)
          (:uri handshake)
          (:query-string handshake))
    (condp re-matches (:uri handshake)
      #"^/index/?$" (ws-index-handler core ch handshake)
      #"^/pubsub/[^/]+/?$" (ws-pubsub-handler core ch handshake)
      :else (do
              (info "Unknown URI " (:uri handshake) ", closing")
              (close ch)))))

(defn ws-server
  "Starts a new websocket server for a core. Starts immediately.

  Options:
  :host   The address to listen on (default 127.0.0.1)
  :post   The port to listen on (default 5556)"
  ([core] (ws-server core {}))
  ([core opts]
   (let [opts (merge {:host "127.0.0.1"
                      :port 5556}
                     opts)
         s (start-http-server (ws-handler core) {:host (:host opts)
                                                 :port (:port opts)
                                                 :websocket true})]
     (info "Websockets server" opts "online")
     (fn []
       (s)
       (info "Websockets server" opts "shut down")))))