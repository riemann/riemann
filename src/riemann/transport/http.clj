(ns riemann.transport.http
  "Provides restful HTTP interface to the core index."
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
        [aleph.http            :only [start-http-server,wrap-ring-handler]]
        [lamina.api            :only [bridge-join]]
        [interval-metrics.measure :only [measure-latency]]
        [clojure.java.io       :only [reader]]
        [clojure.tools.logging :only [info warn]]
        [clj-http.util         :only [url-decode]]
        [clojure.string        :only [split,join]]
        [net.cgrand.moustache  :only [app]]
        [ring.middleware.params :only [wrap-params]]
        ))

(defn json-response [status body]
  "generate a ring http response with a json body"
  {
    :status status
    :headers {"Content-Type" "application/json"}
    :body (json/encode body)
  })

(defn apply-paging [s offset limit]
  (take limit (drop offset s)))

(defn integer-param

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
                                 )))
  )

(defn get-events [core req]
  (try
    (let [params (:params req)
          offset (integer-param params :offset 0 #(>= % 0))
          limit  (integer-param params :limit 250 pos?)
          q      (get params "q")
          index  (:index @core)
          items  (if (nil? q)
                   index
                   (index/search index (query/ast q)))]
      (json-response 200 {
                           :items (apply-paging items offset limit)
                           }))
    (catch IllegalArgumentException e
      (json-response 400 {:message (.getMessage e)}))))

(defn get-events-by-host [core req host]
  (try
    (let [params (:params req)
          offset (integer-param params :offset 0)
          limit  (integer-param params :limit 250)
          i      (filter #(= (:host %) host)
                         (:index @core))]
      (json-response 200 {
                          :items (apply-paging i offset limit)
                          }))
    (catch IllegalArgumentException e
      (json-response 400 {:message (.getMessage e)}))))

(defn get-events-by-host-service [core req host service]
  (let [event (index/lookup (:index @core) host service)]
    (if (nil? event)
      (json-response 404 {:message "no such event"})
      (json-response 200 event))))

(defn http-handler [core]
  "Returns a function which is called with new http connections.
   Responsible for routing requests to the appropriate handler."
  (wrap-params
    (app
      ["events"] {:get #(get-events core %)}
      ["events" host] {:get #(get-events-by-host core % host)}
      ["events" host service] {:get #(get-events-by-host-service core % host service)}
     )))

(defrecord HttpServer [host port core server stats]
  ServiceEquiv
  (equiv? [this other]
          (and (instance? HttpServer other)
               (= host (:host other))
               (= port (:port other))))

  Service
  (conflict? [this other]
             (and (instance? HttpServer other)
                  (= host (:host other))
                  (= port (:port other))))

  (reload! [this new-core]
           (reset! core new-core))

  (start! [this]
          (locking this
            (when-not @server
              (reset! server (start-http-server (wrap-ring-handler (http-handler core))
                                                {:host host
                                                 :port port}))
              (info "HTTP server" host port "online"))))

  (stop! [this]
         (locking this
           (when @server
             (@server)
             (info "HTTP server" host port "shut down"))))

  Instrumented
  (events [this]
          ; Take snapshots of our current stats.
          (let [svc (str "riemann server http " host ":" port)
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

(defn http-server
  "Starts a new http server for a core. Starts immediately.

  Options:
  :host   The address to listen on (default 127.0.0.1)
          Currently does nothing; this option depends on an incomplete
          feature in Aleph, the underlying networking library. Aleph will
          currently bind to all interfaces, regardless of this value.
  :post   The port to listen on (default 5558)

  All output is in json, and lists are structured as

  {
     \"items\":[
        {\"key1\":\"value1\"},
        {\"key1\":\"value2\"}
     ]
  }

  lists can be paged using query parameters 'offset' and 'limit'
  GET /events - list of all events in the index
                query parameters:
                'offset' - offset of first item to return
                'limit'  - max number to return (defualt is 250)
                'q'      - riemann query to match events against
  GET /events/{hostname} - list of all events for a single host
                query parameters:
                'offset' - offset of first item to return
                'limit'  - max number to return (defualt is 250)

  GET /events/{hostname}/{eventname} - a single event
  "
  ([] (http-server {}))
  ([opts]
   (HttpServer.
     (get opts :host "127.0.0.1")
     (get opts :port 5558)
     (atom nil)
     (atom nil)
     {:out   (metrics/rate+latency)
      :in    (metrics/rate+latency)
      :conns (atom 0)})))
