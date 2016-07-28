(ns riemann.kairosdb
  "Forwards events to KairosDB."
  (:refer-clojure :exclude [replace])
  (:import
   (java.net Socket
             DatagramSocket
             DatagramPacket
             InetAddress)
   (java.io Writer OutputStreamWriter))
  (:require [clj-http.client :as client]
            [cheshire.core :as json])
  (:use [clojure.string :only [split join replace]]
        clojure.tools.logging
        riemann.pool
        riemann.common))

(defprotocol KairosDBClient
  (open [client]
        "Creates a KairosDB client")
  (send-metrics [client metrics]
                "Sends a collection of metrics to KairosDB")
  (close [client]
         "Cleans up (closes sockets etc.)"))

(defn- metric->telnet
  "Constructs a KairosDB telnet metric from a metric map"
  [metric]
  (str (join " "
             (concat ["put"
                      (:name metric)
                      (:timestamp metric)
                      (:value metric)]
                     (map
                      (fn [e] (format "%s=%s" (name (key e)) (val e)))
                      (:tags metric))))
       "\n"))

(defrecord KairosDBTelnetClient [^String host ^int port]
  KairosDBClient
  (open [this]
    (let [sock (Socket. host port)]
      (assoc this
             :socket sock
             :out (OutputStreamWriter. (.getOutputStream sock)))))
  (send-metrics [this metrics]
    (let [out (:out this)]
      (doseq [metric metrics]
        (.write ^OutputStreamWriter out
                ^String (metric->telnet metric)))
      (.flush ^OutputStreamWriter out)))
  (close [this]
    (.close ^OutputStreamWriter (:out this))
    (.close ^Socket (:socket this))))

(defrecord KairosDBHTTPClient [^String host ^int port]
  KairosDBClient
  (open [this]
    ;; Using Riemann's pool management, hence :threads 1 and :default-per-route 1 here.
    (let [conn-mgr (clj-http.conn-mgr/make-reusable-conn-manager {:threads 1
                                                                  :timeout 0
                                                                  :default-per-route 1})]
      (assoc this :conn-mgr conn-mgr)))
  (send-metrics [this metrics]
    (let [metric-json (json/generate-string metrics)]
      (client/post (str "http://" host (when port (str ":" port))
                        "/api/v1/datapoints")
                   {:body metric-json
                    :connection-manager (:conn-mgr this)})))
  (close [this]
    (clj-http.conn-mgr/shutdown-manager (:conn-mgr this))
    this))

(defn kairosdb-metric-name
  "Constructs a metric-name for an event."
  [event]
  (let [service (:service event)
        split-service (if service (split service #" ") [])]
     (join "." split-service)))

(defn kairosdb-tags
  "Constructs tags from an event.
  Fqdn in kairosdb is usually passed as a tag."
  [event]
  (if (contains? event :host)
    {:fqdn (:host event)}
    {}))

(defn make-kairosdb-client [protocol host port]
  (case protocol
    :tcp (open (KairosDBTelnetClient. host port))
    :http (open (KairosDBHTTPClient. host port))))

(defn kairosdb
  "Returns a function which accepts a single event or collection of events
  and sends them to KairosDB. Silently drops events when KairosDB is down.
  Attempts to reconnect automatically every five seconds. Use:

  (kairosdb {:host \"kairosdb.local\" :port 4242 :protocol :tcp})

  or

  (kairosdb {:host \"kairosdb.local\" :port 8080 :protocol :http})

  Options:

  :protocol       :tcp to use the Telnet API, or :http for the HTTP REST API.
                  Default :tcp.

  :metric-name    A function which, given an event, returns the string describing
                  the path of that event in kairosdb. kairosdb-metric-name by
                  default.

  :tags    A function which, given an event, returns the hash-map for the tags.
           kairosdb-tags by default.

  :pool-size  The number of connections to keep open. Default 4.

  :reconnect-interval   How many seconds to wait between attempts to connect.
                        Default 5.

  :claim-timeout        How many seconds to wait for a kairosdb connection from
                        the pool. Default 0.1.

  :block-start          Wait for the pool's initial connections to open
                        before returning.

  :ttl                  A function which, given an event, returns the TTL in
                        seconds.
                        Note: TTL is only supported in the HTTP API, and is
                              ignored when sent via Telnet."
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 4242
                     :protocol :tcp
                     :claim-timeout 0.1
                     :pool-size 4
                     :tags kairosdb-tags
                     :metric-name kairosdb-metric-name
                     :ttl (constantly 0)} opts)
        pool (fixed-pool
              (fn []
                (info "Connecting to " (select-keys opts [:protocol :host :port]))
                (let [client (make-kairosdb-client (:protocol opts) (:host opts) (:port opts))]
                  (info "Connected")
                  client))
              (fn [client]
                (info "Closing connection to "
                      (select-keys opts [:protocol :host :port]))
                (close client))
              (-> opts
                  (select-keys [:block-start])
                  (assoc :size (:pool-size opts))
                  (assoc :regenerate-interval (:reconnect-interval opts))))
        metric-name (:metric-name opts)
        tags (:tags opts)
        ttl (:ttl opts)]
    (letfn [(make-metric [event]
              {:name (metric-name event)
               :timestamp (long (* 1000 (:time event)))
               :value (float (:metric event))
               :tags (tags event)
               :ttl (ttl event)})]
      (fn [es]
        (when-let [es (seq (filter (every-pred :service :metric)
                                   (if (map? es)
                                     [es]
                                     es)))]
          (with-pool [client pool (:claim-timeout opts)]
            (send-metrics client
                          (map make-metric es))))))))
