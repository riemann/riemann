(ns riemann.transport.websockets
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:require [riemann.query    :as query]
            [riemann.index    :as index]
            [riemann.pubsub   :as p]
            [aleph.formats    :as formats]
            [gloss.core       :as gloss]
            [cheshire.core    :as json])
  (:use [riemann.common        :only [event-to-json ensure-event-time]]
        [riemann.service       :only [Service ServiceEquiv]]
        [aleph.http            :only [start-http-server]]
        [lamina.core           :only [receive-all
                                      close
                                      channel
                                      map*
                                      channel->lazy-seq
                                      lazy-seq->channel
                                      on-closed
                                      enqueue
                                      channel?
                                      closed-channel]]
        [lamina.api            :only [bridge-join]]
        [clojure.java.io       :only [reader]]
        [clojure.tools.logging :only [info warn]]
        [clj-http.util         :only [url-decode]]
        [clojure.string        :only [split]])
  (:import (java.io OutputStream
                    BufferedWriter
                    OutputStreamWriter
                    InputStreamReader
                    PipedInputStream
                    PipedOutputStream)))

(defn http-query-map
  "Converts a URL query string into a map."
  [string]
  (apply hash-map
         (map url-decode
              (mapcat (fn [kv] (split kv #"=" 2))
                      (split string #"&")))))

(defn split-lines
  "Takes a channel of bytes and returns a channel of utf8 strings, split out by
  \n."
  [ch]
  (formats/decode-channel
    (gloss/string :utf-8 :delimiters ["\n"])
    ch))

(defn ws-pubsub-handler [core ch hs]
  (let [topic  (url-decode (last (split (:uri hs) #"/" 3)))
        params (http-query-map (:query-string hs))
        query  (params "query")
        pred   (query/fun (query/ast query))
        ; Subscribe persistently.
        sub    (p/subscribe! (:pubsub core) topic
                            (fn [event]
                              (when (pred event)
                                (enqueue ch (event-to-json event))))
                             true)]
    (info "New websocket subscription to" topic ":" query)
    (receive-all ch (fn [msg]
                      (when-not msg
                        ; Shut down channel
                        (info "Closing websocket "
                              (:remote-addr hs) topic query)
                        (close ch)
                        (p/unsubscribe! (:pubsub core) sub))))))

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

(defn json-stream-response
  "Given a channel of events, returns a Ring HTTP response with a body
  consisting of a JSON array of the channel's contents."
  [ch]
  (let [out (map* #(str (json/generate-string %) "\n") ch)]
    {:status 200
     :headers {"Content-Type" "application/x-json-stream"}
     :body out}))

(defn channel->reader
  [ch]
  (InputStreamReader.
    (formats/channel->input-stream ch)))

(defn json-channel
  "Takes a channel of bytes containing a JSON array, like \"[1 2 3]\" and
  returns a channel of JSON messages: 1, 2, and 3."
  [ch1]
  (let [reader (channel->reader ch1)
        ch2 (-> reader
              (json/parsed-seq true)
              lazy-seq->channel)]
    ; Allow control messages like close and error to propagate between ch1 and
    ; ch2.
    (on-closed ch2 #(close ch1))
    ch2))

(defn put-events-handler
  "Accepts events from the body of an HTTP request as JSON objects, one per
  line, and applies them to streams."
  [core ch req]
  (let [body (:body req)
        body (if-not (channel? body)
               (closed-channel body)
               body)]
    (->> body
      json-channel
      (map* (fn handle [event]
              (try
                (let [event (ensure-event-time event)]
                  (doseq [stream (:streams core)]
                    (stream event))
                  ; Empty OK response
                  {})
                (catch Exception ^Exception e
                  {:error (.getMessage e)}))))
      json-stream-response
      (enqueue ch))))

(defn ws-handler [core]
  "Returns a function which is called with new websocket connections.
  Responsible for routing requests to the appropriate handler."
  (fn [ch req]
    (info "Websocket connection from" (:remote-addr req)
          (:uri req)
          (:query-string req))
    (condp re-matches (:uri req)
      #"/events/?"       (put-events-handler @core ch req) 
      #"/index/?"        (ws-index-handler @core ch req)
      #"/pubsub/[^/]+/?" (ws-pubsub-handler @core ch req)
      :else (do
              (info "Unknown URI " (:uri req) ", closing")
              (close ch)))))

(defrecord WebsocketServer [host port core server]
  ServiceEquiv
  (equiv? [this other]
          (and (instance? WebsocketServer other)
               (= host (:host other))
               (= port (:port other))))

  Service
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
