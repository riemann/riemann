(ns riemann.logstash
  "Forwards events to LogStash."
  (:refer-clojure :exclude [replace])
  (:import [java.net Socket DatagramSocket DatagramPacket InetAddress]
           [java.io OutputStreamWriter BufferedWriter])
  (:require [riemann.pool :refer [fixed-pool with-pool]]
            [riemann.common :refer [event-to-json]]
            [clojure.tools.logging :refer [info]]
            [less.awful.ssl :refer [socket ssl-context]]))

(defprotocol LogStashClient
  (open [client]
        "Creates a LogStash client")
  (send-line [client line]
        "Sends a formatted line to LogStash")
  (close [client]
         "Cleans up (closes sockets etc.)"))

(defrecord LogStashTLSClient [^String host ^int port opts]
  LogStashClient
  (open [this]
    (let [sock  (socket  (ssl-context (:key opts) (:cert opts) (:ca-cert opts))
                host
                port)]
          (.startHandshake sock)
          (assoc this
                 :socket sock
                 :out (BufferedWriter. (OutputStreamWriter. (.getOutputStream sock))))))
  (send-line [this line]
    (let [out (:out this)]
          (.write ^BufferedWriter out  ^String line)
          (.flush ^BufferedWriter out)))
  (close [this]
    (.close ^BufferedWriter (:out this))
    (.close ^Socket (:socket this))))

(defrecord LogStashTCPClient [^String host ^int port]
  LogStashClient
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

(defrecord LogStashUDPClient [^String host ^int port]
  LogStashClient
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

(defn logstash
  "Returns a function which accepts an event and sends it to logstash.
  Silently drops events when logstash is down. Attempts to reconnect
  automatically every five seconds. Use:

  (logstash {:host \"logstash.local\" :port 2003})

  Options:

  - :pool-size  The number of connections to keep open. Default 4.
  - :reconnect-interval   How many seconds to wait between attempts to connect.
                          Default 5.

  - :claim-timeout        How many seconds to wait for a logstash connection from
                          the pool. Default 0.1.

  - :block-start          Wait for the pool's initial connections to open
                          before returning.

  - :protocol             Protocol to use. Either :tcp (default), :tls or :udp.

  TLS options:

  - :key              A PKCS8-encoded private key file
  - :cert             The corresponding public certificate
  - :ca-cert          The certificate of the CA which signed this key"
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 9999
                     :protocol :tcp
                     :claim-timeout 0.1
                     :pool-size 4} opts)
        pool (fixed-pool
               (fn []
                 (info "Connecting to " (select-keys opts [:host :port :protocol]))
                 (let [host (:host opts)
                       port (:port opts)
                       client (open (condp = (:protocol opts)
                                      :tcp (LogStashTCPClient. host port)
                                      :udp (LogStashUDPClient. host port)
                                      :tls (LogStashTLSClient. host port opts)))]
                   (info "Connected")
                   client))
               (fn [client]
                 (info "Closing connection to "
                       (select-keys opts [:host :port]))
                 (close client))
               {:size                 (:pool-size opts)
                :block-start          (:block-start opts)
                :regenerate-interval  (:reconnect-interval opts)})]

    (fn [event]
      (with-pool [client pool (:claim-timeout opts)]
                 (let [string (event-to-json (merge event {:source (:host event)}))]
                   (send-line client (str string "\n")))))))
