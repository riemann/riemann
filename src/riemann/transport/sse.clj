(ns riemann.transport.sse
  "Exposes subscriptions to the index as server sent event channels"
  (:require [riemann.query            :as query]
            [riemann.index            :as index]
            [interval-metrics.core    :as metrics]
            [riemann.common           :as common]
            [riemann.pubsub           :refer [subscribe! unsubscribe!]]
            [riemann.instrumentation  :refer [Instrumented]]
            [riemann.service          :refer [Service ServiceEquiv]]
            [riemann.time             :refer [unix-time]]
            [aleph.http               :refer [start-http-server]]
            [lamina.core              :refer [channel
                                              enqueue
                                              on-closed
                                              run-pipeline]]
            [clojure.tools.logging    :refer [info]]
            [clj-http.util            :refer [url-decode]]
            [clojure.string           :refer [split]]))

(def ^{:doc "Prepare an event for sending out on the wire."}
  event-to-server-sent-event
  (comp (partial format "data: %s\n\n") 
        common/event-to-json))

(defn http-query-map
  "Converts a URL query string into a map."
  [string]
  (apply hash-map
         (map url-decode
              (mapcat (fn [kv] (split kv #"=" 2))
                      (split string #"&")))))

(defn sse-error-uri
  "Dummy 404 output for invalid URIs"
  [ch uri]
  (info "invalid URI: " uri)
  (enqueue ch {:status 404 :headers {"Connection" "close"}}))

(defn sse-out
  "Yield a function that given an incoming event will
   test for a given predicate and when it matches will
   format it for sending it over a SSE channel."
  [{:keys [out] :as stats} ch pred]
  (fn [event]
    ;; Send event to client, measuring write latency
    (when (pred event)
      (let [t1 (System/nanoTime)]
        (run-pipeline
         (enqueue ch (event-to-server-sent-event event))
         ;; When the write completes, measure latency
         (fn [_] (metrics/update! out (- (System/nanoTime) t1))))))))

(defn sse-handler
  "Queries the index for events and streams them to the client,
  also initiates a pubsub subscription to the index topic with that
  query."
  [core stats headers]
  (fn [resp-ch {:keys [uri query-string remote-addr] :as req}]
    (info "SSE channel request from: " remote-addr uri query-string)
    (if (re-matches #"^/index/?" uri)
      (let [ch                     (channel)
            {:keys [pubsub index]} @core
            {:strs [query]}        (http-query-map query-string)
            ast                    (query/ast query)]

        ; first send all known events
        (when index
          (doseq [event (index/search index ast)]
            (enqueue ch (event-to-server-sent-event event))))

        ; now subscribe to the core
        (let [pred (query/fun ast)
              sub  (subscribe! pubsub "index" (sse-out stats ch pred) true)]
          (info "New SSE subscription to index for:" query)

          (on-closed ch (fn []
                          (info "Closing SSE channel " remote-addr query)
                          (unsubscribe! pubsub sub)))

          (enqueue resp-ch {:status 200 :headers headers :body ch})))
      (sse-error-uri resp-ch uri))))

(defrecord SSEServer [host port headers core server stats]
  ServiceEquiv
  (equiv? [this other]
          (and (instance? SSEServer other)
               (= host (:host other))
               (= port (:port other))))

  Service
  (conflict? [this other]
             (and (instance? SSEServer other)
                  (= host (:host other))
                  (= port (:port other))))

  (reload! [this new-core]
           (reset! core new-core))

  (start! [this]
          (locking this
            (when-not @server
              (reset! server (start-http-server
                              (sse-handler core stats headers)
                              {:host host :port port}))
              (info "SSE server" host port "online"))))

  (stop! [this]
         (locking this
           (when @server
             (@server)
             (info "SSE server" host port "shut down"))))

  Instrumented
  (events [this]
          ;; Take snapshots of our current stats.
          (let [svc (str "riemann server sse " host ":" port)
                base {:time (unix-time)
                      :state "ok"}
                out (metrics/snapshot! (:out stats))
                in  (metrics/snapshot! (:in stats))]
            (map (partial merge base)
                 (concat [;; Connections
                          {:service (str svc " conns")
                           :metric (deref (:conns stats))}
                         
                          ;; Rates
                          {:service (str svc " out rate")
                           :metric (:rate out)}
                          {:service (str svc " in rate")
                           :metric (:rate in)}]

                         ;; Latencies
                         (map (fn [[q latency]]
                                {:service (str svc " out latency " q)
                                 :metric latency})
                              (:latencies out))
                         (map (fn [[q latency]]
                                {:service (str svc " in latency " q)
                                 :metric latency})
                              (:latencies in)))))))

(defn sse-server
  "Starts a new SSE server for a core. Starts immediately.

  Options:
  :host    The address to listen on (default 127.0.0.1)
           Currently does nothing; this option depends on an incomplete
           feature in Aleph, the underlying networking library. Aleph will
           currently bind to all interfaces, regardless of this value.
  :port    The port to listen on (default 5558)
  :headers Additional headers to send with the reply. By default
           Content-Type is set to text/event-stream and Cache-Control to
           no-cache. If you do not expose your client web application behind
           the same host, you will probably need to add an
           Access-Control-Allow-Origin header here"
  ([] (sse-server {}))
  ([{:keys [host port headers]
     :or   {host    "127.0.0.1"
            port    5558
            headers {}}}]
     (SSEServer.
      host
      port
      (merge {"Content-Type"  "text/event-stream"
              "Cache-Control" "no-cache"}
             headers)
      (atom nil)
      (atom nil)
      {:out   (metrics/rate+latency)
       :in    (metrics/rate+latency)
       :conns (atom 0)})))
