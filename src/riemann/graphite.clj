(ns riemann.graphite
  "Forwards events to Graphite."
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

(defprotocol GraphiteClient
  (open [client]
        "Creates a Graphite client")
  (send-line [client line]
        "Sends a formatted line to Graphite")
  (close [client]
         "Cleans up (closes sockets etc.)"))

(defrecord GraphiteTCPClient [^String host ^int port]
  GraphiteClient
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

(defrecord GraphiteUDPClient [^String host ^int port]
  GraphiteClient
  (open [this]
    (assoc this
           :socket (DatagramSocket.)
           :host host
           :port port))
  (send-line [this line]
    (let [bytes (.getBytes ^String line)
          length (count line)
          addr (InetAddress/getByName (:host this))
          datagram (DatagramPacket. bytes length ^InetAddress addr port)]
      (.send ^DatagramSocket (:socket this) datagram)))
  (close [this]
    (.close ^DatagramSocket (:socket this))))

(defn graphite-path-basic
  "Constructs a path for an event. Takes the hostname fqdn, reversed,
  followed by the service, with spaces converted to dots."
  [event]
  (let [service (:service event)
        host (:host event)
        split-service (if service (split service #" ") [])
        split-host (if host (split host #"\.") [])]
     (join "." (concat (reverse split-host) split-service))))

(defn graphite-path-percentiles
  "Like graphite-service-basic, but also converts trailing decimals like 0.95
  to 95."
  [event]
  (graphite-path-basic
    (if-let [service (:service event)]
      (assoc event :service
             ; hack hack hack hack
             (replace service
                      #"(\d+)\.(\d+)$"
                      (fn [[_ whole frac]] (str (when-not (= "0" whole))
                                                frac))))
      event)))

(defn graphite
  "Returns a function which accepts an event and sends it to Graphite.
  Silently drops events when graphite is down. Attempts to reconnect
  automatically every five seconds. Use:

  (graphite {:host \"graphite.local\" :port 2003})

  Options:

  :path       A function which, given an event, returns the string describing
              the path of that event in graphite. graphite-path-percentiles by
              default.

  :pool-size  The number of connections to keep open. Default 4.

  :reconnect-interval   How many seconds to wait between attempts to connect.
                        Default 5.

  :claim-timeout        How many seconds to wait for a graphite connection from
                        the pool. Default 0.1.

  :block-start          Wait for the pool's initial connections to open
                        before returning.

  :protocol             Protocol to use. Either :tcp (default) or :udp."
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 2003
                     :protocol :tcp
                     :claim-timeout 0.1
                     :pool-size 4
                     :path graphite-path-percentiles} opts)
        pool (fixed-pool
               (fn []
                 (info "Connecting to " (select-keys opts [:host :port]))
                 (let [host (:host opts)
                       port (:port opts)
                       client (open (condp = (:protocol opts)
                                      :tcp (GraphiteTCPClient. host port)
                                      :udp (GraphiteUDPClient. host port)))]
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
        path (:path opts)]

    (fn [event]
      (when (:metric event)
        (with-pool [client pool (:claim-timeout opts)]
                   (let [string (str (join " " [(path event)
                                                (float (:metric event))
                                                (int (:time event))])
                                     "\n")]
                     (send-line client string)))))))
