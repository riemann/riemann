(ns riemann.transport.websockets
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:require [riemann.query    :as query]
            [riemann.index    :as index]
            [riemann.pubsub   :as p])
  (:use [riemann.common        :only [event-to-json]]
        [riemann.service       :only [Service]]
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
    (when-let [i (:index core)]
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
      #"^/index/?$" (ws-index-handler @core ch handshake)
      #"^/pubsub/[^/]+/?$" (ws-pubsub-handler @core ch handshake)
      :else (do
              (info "Unknown URI " (:uri handshake) ", closing")
              (close ch)))))

(defrecord WebsocketServer [host port core server]
  Service
  (equiv? [this other]
          (and (instance? WebsocketServer other)
               (= host (:host other))
               (= port (:port other))))

  (reload! [this new-core]
           (reset! core new-core))

  (start! [this]
          (locking this
            (when-not @server
              (reset! server (start-http-server (ws-handler core)
                                                {:host host
                                                 :port port
                                                 :websocket true}))
              (info "Websockets server" host port "online"))))

  (stop! [this]
         (locking this
           (when @server
             (@server)
             (info "Websockets server" host port "shut down")))))

(defn ws-server
  "Starts a new websocket server for a core. Starts immediately.

  Options:
  :host   The address to listen on (default 127.0.0.1)
          Currently does nothing; this option depends on an incomplete
          feature in Aleph, the underlying networking library. Aleph will
          currently bind to all interfaces, regardless of this value.
  :post   The port to listen on (default 5556)"
  ([] (ws-server {}))
  ([opts]
   (WebsocketServer.
     (get opts :host "127.0.0.1")
     (get opts :port 5556)
     (atom nil)
     (atom nil))))
