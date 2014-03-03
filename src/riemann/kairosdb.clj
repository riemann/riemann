(ns riemann.kairosdb
  "Forwards events to KairosDB."
  (:refer-clojure :exclude [replace])
  (:import
   (java.net Socket
             DatagramSocket
             DatagramPacket
             InetAddress)
   (java.io Writer
            OutputStreamWriter)
   (org.jboss.netty.channel Channels)
   (org.jboss.netty.handler.codec.frame DelimiterBasedFrameDecoder
                                        Delimiters)
   (org.jboss.netty.handler.codec.oneone OneToOneDecoder)
   (org.jboss.netty.handler.codec.string StringDecoder StringEncoder)
   (org.jboss.netty.util CharsetUtil))
  (:use [clojure.string :only [split join replace]]
        clojure.tools.logging
        riemann.pool
        riemann.common))

(defprotocol KairosDBClient
  (open [client]
        "Creates a KairosDB client")
  (send-line [client line]
        "Sends a formatted line to KairosDB")
  (close [client]
         "Cleans up (closes sockets etc.)"))

(defrecord KairosDBTelnetClient [^String host ^int port]
  KairosDBClient
  (open [this]
    (let [sock (Socket. host port)]
      (assoc this
             :socket sock
             :out (OutputStreamWriter. (.getOutputStream sock)))))
  (send-line [this line]
    (let [out (:out this)]
      (.write ^OutputStreamWriter out ^String line)
      (.flush ^OutputStreamWriter out)))
  (close [this]
    (.close ^OutputStreamWriter (:out this))
    (.close ^Socket (:socket this))))

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

(defn kairosdb
  "Returns a function which accepts an event and sends it to KairosDB.
  Silently drops events when KairosDB is down. Attempts to reconnect
  automatically every five seconds. Use:

  (kairosdb {:host \"kairosdb.local\" :port 4242})

  Options:

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
                        before returning."
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 4242
                     :protocol :tcp
                     :claim-timeout 0.1
                     :pool-size 4
                     :tags kairosdb-tags
                     :metric-name kairosdb-metric-name} opts)
        pool (fixed-pool
               (fn []
                 (info "Connecting to " (select-keys opts [:host :port]))
                 (let [host (:host opts)
                       port (:port opts)
                       client (open (KairosDBTelnetClient. host port))]
                   (info "Connected")
                   client))
               (fn [client]
                 (info "Closing connection to "
                       (select-keys opts [:host :port]))
                 (close client))
               (-> opts
                   (select-keys [:block-start])
                   (assoc :size (:pool-size opts))
                   (assoc :regenerate-interval (:reconnect-interval opts))))
        metric-name (:metric-name opts)
        tags (:tags opts)]

    (fn [event]
      (when (:metric event)
        (when (:service event)
          (with-pool [client pool (:claim-timeout opts)]
                     (let [string (str (join " " (concat ["put"
                                                          (metric-name event)
                                                          (long (* 1000 (:time event)))
                                                          (float (:metric event))]
                                                          (map
                                                            (fn [e] (format "%s=%s" (name (key e)) (val e)))
                                                            (tags event))))
                                       "\n")]
                       (send-line client string))))))))
