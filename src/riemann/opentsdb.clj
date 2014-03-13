(ns riemann.opentsdb
  "Forwards events to OpenTSDB."
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

(defprotocol OpenTSDBClient
  (open [client]
        "Creates a OpenTSDB client")
  (send-line [client line]
        "Sends a formatted line to OpenTSDB")
  (close [client]
         "Cleans up (closes sockets etc.)"))

(defrecord OpenTSDBTelnetClient [^String host ^int port]
  OpenTSDBClient
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

(defn opentsdb-metric-name
  "Constructs a metric-name for an event."
  [event]
  (let [service (:service event)
        split-service (if service (split service #" ") [])]
     (join "." split-service)))

(defn opentsdb-tags
  "Constructs a tag-key named host from an event.
  tcollector also generates tag-key named host"
  [event]
  (if (contains? event :host)
    {:host (:host event)}
    {}))

(defn opentsdb
  "Returns a function which accepts an event and sends it to OpenTSDB.
  Silently drops events when OpenTSDB is down. Attempts to reconnect
  automatically every five seconds. Use:

  (opentsdb {:host \"opentsdb.local\" :port 4242})

  Options:

  :metric-name    A function which, given an event, returns the string describing
                  the path of that event in opentsdb. opentsdb-metric-name by
                  default.

  :tags    A function which, given an event, returns the hash-map for the tags.
           opentsdb-tags by default.

  :pool-size  The number of connections to keep open. Default 4.

  :reconnect-interval   How many seconds to wait between attempts to connect.
                        Default 5.

  :claim-timeout        How many seconds to wait for a opentsdb connection from
                        the pool. Default 0.1.

  :block-start          Wait for the pool's initial connections to open
                        before returning."
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 4242
                     :protocol :tcp
                     :claim-timeout 0.1
                     :pool-size 4
                     :tags opentsdb-tags
                     :metric-name opentsdb-metric-name} opts)
        pool (fixed-pool
               (fn []
                 (info "Connecting to " (select-keys opts [:host :port]))
                 (let [host (:host opts)
                       port (:port opts)
                       client (open (OpenTSDBTelnetClient. host port))]
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
                                                          (long (:time event))
                                                          (float (:metric event))]
                                                          (map
                                                            (fn [e] (format "%s=%s" (name (key e)) (val e)))
                                                            (tags event))))
                                       "\n")]
                       (send-line client string))))))))
