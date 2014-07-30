(ns riemann.transport.websockets
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:require [riemann.query         :as query]
            [riemann.index         :as index]
            [riemann.pubsub        :as p]
            [cheshire.core         :as json]
            [interval-metrics.core :as metrics]
            [org.httpkit.server    :as http])
  (:use [riemann.common        :only [event->json ensure-event-time event->structure]]
        [riemann.core          :only [stream!]]
        [riemann.instrumentation :only [Instrumented]]
        [riemann.service       :only [Service ServiceEquiv]]
        [riemann.time          :only [unix-time]]
        [interval-metrics.measure :only [measure-latency]]
        [clojure.java.io       :only [reader]]
        [clojure.tools.logging :only [info warn debug]]
        [clj-http.util         :only [url-decode]]
        [clojure.string        :only [split]]
        [slingshot.slingshot   :only [throw+ try+]])
  (:import (java.io OutputStream
                    BufferedWriter
                    OutputStreamWriter
                    InputStreamReader
                    PipedInputStream
                    PipedOutputStream)))

(defn http-query-map
  "Converts a URL query string into a map."
  [string]
  (if-not (nil? string)
    (apply hash-map
           (map url-decode
                (mapcat (fn [kv] (split kv #"=" 2))
                        (split string #"&"))))
    {})) ; empty map if no query string

(defn json-response [status body]
  "generate a ring http response with a json body"
  {
    :status status
    :headers {"Content-Type" "application/json"}
    :body (json/encode body)
    })

(defn apply-paging [events offset limit]
  "sorts and chops the collection of events"
  (->> events
       (sort-by (fn [event] [(:host event) (:service event)]))
       (drop offset)
       (take limit)
       (map event->structure)))

(defn integer-param
  "grab and parse a value from a map, with optional predicate"
  ([params key default] (let [key-name (name key)]
                          (if (contains? params key-name)
                            (try
                              (Integer/parseInt (params key-name))
                              (catch NumberFormatException e
                                (throw (IllegalArgumentException. (str "parameter " key-name " is not an integer")))))
                            default)))

  ([params key default pred] (let [result (integer-param params key default)]
                               (if (pred result)
                                 result
                                 (throw (IllegalArgumentException. (str "parameter " (name key) " is not valid")))
                                 ))))


(defn path-fragments [uri]
  "returns a sequence of decoded uri parts.

  (uri-parts \"/thing/that/foo\")
  > (\"thing\" \"that\" \"foo\")
  "
  (drop 1 ; first element will be "" matching the chars 'before' '/'
        (map url-decode (split uri #"/"))))

(defn http-get-events [core req]
  (try+
    (let [params (http-query-map (:query-string req))
          offset (integer-param params :offset 0 #(>= % 0))
          limit  (integer-param params :limit 250 pos?)
          query  (get params "query")
          index  (:index core)
          events (index/search index (if (nil? query)
                                       true
                                       (query/ast query)))]
      (json-response 200 {:items (apply-paging events offset limit)}))
    (catch [:type :riemann.query/parse-error] {:keys [message]}
      (json-response 400 {:error (str "'query' parameter is not a valid query detail:" message)}))
    (catch IllegalArgumentException e
      (json-response 400 {:error (.getMessage e)}))))

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
                                   (http/send! ch (event->json event))))
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
  (try+
    (let [params (http-query-map (:query-string hs))
          query  (params "query")
          ast    (query/ast query)]
    (when-let [i (:index core)]
      (doseq [event (index/search i ast)]
        (http/send! ch (event->json event))))
    (if (= (params "subscribe") "true")
      (ws-pubsub-handler core stats ch (assoc hs :uri "/pubsub/index") actions)
      (http/close ch)))
    (catch [:type :riemann.query/parse-error] {:keys [message]}
      ; send 400 (bad request + close the channel)
      (http/send! ch (json-response 400 {:error (str "invalid 'query' parameter, details:" message)}) true))))

(defn http-index-handler
  [core stats ch req]
  (let [parts (drop 1 (path-fragments (:uri req))) ; drop the 'index' bit
        method (:request-method req)]
    (http/send! ch
                (condp = method
                  :get (condp re-matches (:uri req)
                         #"/index/?" (http-get-events core req)
                         {:status 404 :body "unknown uri"})
                  :else (json-response 405 {:error (str "method " method " is not accepted")}))
                true ; send close .. this is http not sockets
                )))

(defn index-handler
  "Handles queries to the internal index"
  [core stats ch req actions]
  (if (http/websocket? ch)
    (ws-index-handler core stats ch req actions)
    (http-index-handler core stats ch req)))

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
          (debug (if (http/websocket? ch) "Websocket" "HTTP") " connection from" (:remote-addr req)
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
            #"/index.*"        (index-handler @core stats ch req actions)
            #"/pubsub/[^/]+/?" (ws-pubsub-handler @core stats ch req actions)

            (do
              (info "Unknown URI " (:uri req) ", closing")
              (http/send! ch (json-response 404 {:error (str "no such uri")}) true))))

        (catch Throwable t
          (do
            (warn t "ws-handler caught; closing websocket connection.")
            (http/send! ch
                        (json-response 500
                                       {:error (str "Internal server error, detail:" (.getMessage t))}) true)))))))

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
                                              {:host host
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
  "Starts a new http/websocket server for a core. Starts immediately.

  Options:
  :host   The address to listen on (default 127.0.0.1)
  :post   The port to listen on (default 5556)

  This provides two styles of accessing riemann, websockets and plain
  http. Both methods encode events in the same way.

  Example json encoded event:

  {
     \"host\":\"host1.example.com\",
     \"service\":\"load\",
     \"state\":\"ok\",
     \"description\":\"1-minute load average per core is 0.0\",
     \"metric\":0.0,
     \"tags\":null,
     \"time\":\"2014-07-28T07:40:40.000Z\",
     \"ttl\":10.0
  }

  Note that the 'time' field is a ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZZ)

  websockets:

  All websocket requests accept or return events encoded as json
  seperated by a single '\n' char.

  /events inbound connection to publish events
  /index  query the index
          query parameters:
          'query' - riemann query to select events
          'subscribe' - if 'true' the connection will remain open
                        and will stream all matching new events
  /pubsub/{topic}
          'query' - riemann query to select events from the topic

  Errors are reported single json object with the key 'error'. Note that
  the connection will be terminated.

  Plain HTTP:

  output is in json, and lists are structured as

  {
     \"events\":[
        {\"host\":\"host\", \"service\":\"load\"},
        {\"host\":\"host\", \"service\":\"cpu\"},
     ]
  }

  GET /index - list of all events in the index
                query parameters:
                'offset' - offset of first item to return
                'limit'  - max number to return (defualt is 250)
                'query'  - riemann query to match events against

  The order of the events is host/service to allow 'offset' and
  'limit' to behave sanely. But despit this bare in mind the result
  is a snapshot of the index, and it may (is quite likely to have)
  changed by the time you make the next request for the next page of
  events.

  Errors are reported using standard http status codes, the response
  body should still be in json and contain a 'error' attribute
  with some details of the issue.

    2xx - success
    4xx - client error, specificaly
          400 - bad request, invalid or missing parameter
          404 - what you asked for doesn't exist
          405 - what you asked to do isn't supported
    5xx - some interval error, check the riemann log for more information"

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
