(ns riemann.transport.websockets
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:require [riemann.query         :as query]
            [riemann.index         :as index]
            [riemann.pubsub        :as p]
            [cheshire.core         :as json]
            [interval-metrics.core :as metrics]
            [org.httpkit.server    :as http])
  (:use [riemann.common        :only [event-to-json ensure-event-time]]
        [riemann.core          :only [stream!]]
        [riemann.instrumentation :only [Instrumented]]
        [riemann.service       :only [Service ServiceEquiv]]
        [riemann.time          :only [unix-time]]
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

(defn ws-pubsub-handler
  "Subscribes to a pubsub channel and streams events back over the WS conn."
  [core stats ch hs actions]
  (if-not (http/websocket? ch)
    (warn "Ignoring non-websocket request to websocket server.")
    (let [topic  (url-decode (last (split (:uri hs) #"/" 3)))
          params (http-query-map (:query-string hs))
          query  (params "query")
          pred   (query/fun (query/ast query))
          ; Subscribe persistently.
          sub    (p/subscribe! (:pubsub core) topic
                               (fn emit [event]
                                 (when (pred event)
                                   ;; http-kit's send does not provide a way
                                   ;; to measure output latency
                                   (http/send! ch (event-to-json event))))
                               true)]
      (info "New websocket subscription to" topic ":" query)

      ; When the channel closes, unsubscribe.
      (swap! actions conj
             (fn []
               (info "Closing websocket " (:remote-addr hs) topic query)
               (p/unsubscribe! (:pubsub core) sub)))

      ; Close channel on nil msg
      (http/on-receive ch (fn [data] (when-not data (http/close ch)))))))

(defn ws-index-handler
  "Queries the index for events and streams them to the client. If subscribe is
  true, also initiates a pubsub subscription to the index topic with that
  query."
  [core stats ch hs actions]
  (if-not (http/websocket? ch)
    (warn "Ignoring non-websocket request to websocket server.")
    (let [params (http-query-map (:query-string hs))
          query  (params "query")
          ast    (query/ast query)]
      (when-let [i (:index core)]
        (doseq [event (index/search i ast)]
          (http/send! ch (event-to-json event))))
      (if (= (params "subscribe") "true")
        (ws-pubsub-handler core stats ch (assoc hs :uri "/pubsub/index") actions)
        (http/close ch)))))

(defn input-event!
  [core stats event]
  (measure-latency
   (:in stats)
   (try
     (let [event (ensure-event-time event)]
       (stream! core event)
       {})
     (catch Exception ^Exception e
            {:error (.getMessage e)}))))

(defn put-events-handler
  "Accepts events from the body of an HTTP request as JSON objects, one per
  line, and applies them to streams."
  [core stats ch req]

  (if (http/websocket? ch)
    (http/on-receive
     ch
     (fn [data]
       (if data
         (http/send! ch
                     {:status 200
                      :headers {"Content-Type" "application/json-stream"}
                      :body (json/generate-string
                             (input-event! core stats
                                           (json/parse-string data true)))})
         (http/close ch))))
    (let [body (reader (:body req))]
      (loop [line (.readLine body)]
        (when line
          (try
            (let [event (json/parse-string line true)]
              (http/send! ch
                          {:status 200
                           :headers {"Content-Type" "application/json"}
                           :body (str
                                  (json/generate-string
                                   (input-event! core stats event))
                                  "\n")}
                          false))
            (catch Exception ^Exception e
                   (http/send! ch
                               {:status 200
                                :headers {"Content-Type" "application/json"}
                                :body (str
                                       (json/generate-string
                                        {:error (.getMessage e)}) "\n")}
                               true)))
          (recur (.readLine body))))
      (http/close ch))))

(defn ws-handler [core stats]
  "Returns a function which is called with new websocket connections.
  Responsible for routing requests to the appropriate handler."
  (fn handle [req]
    (http/with-channel req ch
      (try
        (let [actions (atom [])]
          (debug "Websocket connection from" (:remote-addr req)
                 (:uri req)
                 (:query-string req))

                                        ; Stats
          (when (http/websocket? ch)
            (swap! (:conns stats) inc)
            (http/on-close ch
                           (fn [_]
                             (swap! (:conns stats) dec)
                             (doseq [action @actions]
                               (action)))))

          ;; Route request
          (condp re-matches (:uri req)
            #"/events/?"       (put-events-handler @core stats ch req)
            #"/index/?"        (ws-index-handler @core stats ch req actions)
            #"/pubsub/[^/]+/?" (ws-pubsub-handler @core stats ch req actions)
            (do
              (info "Unknown URI " (:uri req) ", closing")
              (http/close ch))))

        (catch Exception t
          (do
            (warn t "ws-handler caught; closing websocket connection.")
            (http/close ch)))))))

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
              (reset! server (http/run-server (ws-handler core stats)
                                              {:ip host
                                               :port port}))
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
                      :tags ["riemann"]
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
