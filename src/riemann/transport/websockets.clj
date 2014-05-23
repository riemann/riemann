(ns riemann.transport.websockets
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:require [riemann.query         :as query]
            [riemann.index         :as index]
            [riemann.pubsub        :as p]
            [aleph.formats         :as formats]
            [gloss.core            :as gloss]
            [cheshire.core         :as json]
            [interval-metrics.core :as metrics])
  (:use [riemann.common        :only [event-to-json ensure-event-time]]
        [riemann.core          :only [stream!]]
        [riemann.instrumentation :only [Instrumented]]
        [riemann.service       :only [Service ServiceEquiv]]
        [riemann.time          :only [unix-time]]
        [aleph.http            :only [start-http-server]]
        [lamina.core           :only [channel
                                      channel?
                                      channel->lazy-seq
                                      close
                                      closed-channel
                                      enqueue
                                      lazy-seq->channel
                                      map*
                                      on-closed
                                      receive-all
                                      run-pipeline]]
        [lamina.api            :only [bridge-join]]
        [interval-metrics.measure :only [measure-latency]]
        [clojure.java.io       :only [reader]]
        [clojure.tools.logging :only [info warn debug]]
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

(defn ws-pubsub-handler
  "Subscribes to a pubsub channel and streams events back over the WS conn."
  [core stats ch hs]
  (if-not (:websocket? hs)
    (warn "Ignoring non-websocket request to websocket server.")
    (let [topic  (url-decode (last (split (:uri hs) #"/" 3)))
          params (http-query-map (:query-string hs))
          query  (params "query")
          pred   (query/fun (query/ast query))
          ; Subscribe persistently.
          sub    (p/subscribe! (:pubsub core) topic
                               (fn emit [event]
                                 (when (pred event)
                                   ; Send event to client, measuring write
                                   ; latency
                                   (let [t1 (System/nanoTime)]
                                     (run-pipeline
                                       (enqueue ch (event-to-json event))
                                       {:error-handler (fn [_] (close ch))}
                                       ; When the write completes, measure
                                       ; latency
                                       (fn measure [_]
                                         (metrics/update!
                                           (:out stats)
                                           (- (System/nanoTime) t1)))
                                       {:error-handler (fn [_] (close ch))}))))

                               true)]
      (info "New websocket subscription to" topic ":" query)

      ; When the channel closes, unsubscribe.
      (on-closed ch (fn []
                      (info "Closing websocket " (:remote-addr hs) topic query)
                      (p/unsubscribe! (:pubsub core) sub)))

      ; Close channel on nil msg
      (receive-all ch (fn [msg]
                        (when-not msg
                          ; Shut down channel
                          (close ch)))))))

(defn ws-index-handler
  "Queries the index for events and streams them to the client. If subscribe is
  true, also initiates a pubsub subscription to the index topic with that
  query."
  [core stats ch hs]
  (if-not (:websocket? hs)
    (warn "Ignoring non-websocket request to websocket server.")
    (let [params (http-query-map (:query-string hs))
          query  (params "query")
          ast    (query/ast query)]
      (when-let [i (:index core)]
        (doseq [event (index/search i ast)]
          (enqueue ch (event-to-json event))))
      (if (= (params "subscribe") "true")
        (ws-pubsub-handler core stats ch (assoc hs :uri "/pubsub/index"))
        (close ch)))))

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
  [core stats ch req]
  (let [body (:body req)
        body (if-not (channel? body)
               (closed-channel body)
               body)]

    ; Track connections
    (swap! (:conns stats) inc)
    (on-closed body #(swap! (:conns stats) dec))

    (->> body
      json-channel
      (map* (fn handle [event]
              (measure-latency (:in stats)
                               (try
                                 (let [event (ensure-event-time event)]
                                   (stream! core event)
                                   ; Empty OK response
                                   {})
                                 (catch Exception ^Exception e
                                   {:error (.getMessage e)})))))
      json-stream-response
      (enqueue ch))))

(defn ws-handler [core stats]
  "Returns a function which is called with new websocket connections.
  Responsible for routing requests to the appropriate handler."
  (fn handle [ch req]
    (try
      (debug "Websocket connection from" (:remote-addr req)
            (:uri req)
            (:query-string req))

      ; Stats
      (when (:websocket? req)
        (swap! (:conns stats) inc)
        (on-closed ch #(swap! (:conns stats) dec)))

      ; Route request
      (condp re-matches (:uri req)
        #"/events/?"       (put-events-handler @core stats ch req)
        #"/index/?"        (ws-index-handler @core stats ch req)
        #"/pubsub/[^/]+/?" (ws-pubsub-handler @core stats ch req)
        (do
          (info "Unknown URI " (:uri req) ", closing")
          (close ch)))

      (catch Throwable t
        (do
          (warn t "ws-handler caught; closing websocket connection.")
          (close ch))))))

(defrecord WebsocketServer [host port core server stats]
  ServiceEquiv
  (equiv? [this other]
          (and (instance? WebsocketServer other)
               (= host (:host other))
               (= port (:port other))))

  Service
  (conflict? [this other]
             (and (instance? WebsocketServer other)
                  (= host (:host other))
                  (= port (:port other))))

  (reload! [this new-core]
           (reset! core new-core))

  (start! [this]
          (locking this
            (when-not @server
              (reset! server (start-http-server (ws-handler core stats)
                                                {:host host
                                                 :port port
                                                 :websocket true}))
              (info "Websockets server" host port "online"))))

  (stop! [this]
         (locking this
           (when @server
             (@server)
             (info "Websockets server" host port "shut down"))))

  Instrumented
  (events [this]
          ; Take snapshots of our current stats.
          (let [svc (str "riemann server ws " host ":" port)
                base {:time (unix-time)
                      :state "ok"}
                out (metrics/snapshot! (:out stats))
                in  (metrics/snapshot! (:in stats))]
            (map (partial merge base)
                 (concat [; Connections
                          {:service (str svc " conns")
                           :metric (deref (:conns stats))}
                         
                          ; Rates
                          {:service (str svc " out rate")
                           :metric (:rate out)}
                          {:service (str svc " in rate")
                           :metric (:rate in)}]

                         ; Latencies
                         (map (fn [[q latency]]
                                {:service (str svc " out latency " q)
                                 :metric latency})
                              (:latencies out))
                         (map (fn [[q latency]]
                                {:service (str svc " in latency " q)
                                 :metric latency})
                              (:latencies in)))
             ))))

(defn ws-server
  "Starts a new websocket server for a core. Starts immediately.

  Options:
  :host   The address to listen on (default 127.0.0.1)
          Currently does nothing; this option depends on an incomplete
          feature in Aleph, the underlying networking library. Aleph will
          currently bind to all interfaces, regardless of this value.
  :port   The port to listen on (default 5556)"
  ([] (ws-server {}))
  ([opts]
   (WebsocketServer.
     (get opts :host "127.0.0.1")
     (get opts :port 5556)
     (atom nil)
     (atom nil)
     {:out   (metrics/rate+latency)
      :in    (metrics/rate+latency)
      :conns (atom 0)})))
