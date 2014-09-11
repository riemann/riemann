(ns riemann.logentries
  "Forwards events to Logentries."
  (:import
   (java.net Socket)
   (java.io OutputStreamWriter))
  (:use clojure.tools.logging
        riemann.pool
        riemann.common))

(defn format-event-data [data]
  (apply str
         (map
           (fn [[k v]] (str " " (name k) "='" v "'"))
           data)))

(defn event-to-le-format [data]
  (let [message (:description data)]
    (if message
      (str message "," (format-event-data (dissoc data :description)))
      (format-event-data data))))

(defprotocol LogentriesClient
  (open [client]
        "Creates a Logentries client")
  (send-line [client line]
        "Sends a formatted line to Logentries")
  (close [client]
         "Cleans up (closes sockets etc.)"))

(defrecord LogentriesTokenClient [^String host ^int port ^String token]
  LogentriesClient
  (open [this]
    (let [sock (Socket. host port)]
      (assoc this 
             :socket sock
             :out (OutputStreamWriter. (.getOutputStream sock)))))
  (send-line [this line]
    (let [out (:out this)
          line (str line " " token "\n")]
      (.write ^OutputStreamWriter out ^String line)
      (.flush ^OutputStreamWriter out)))
  (close [this]
    (.close ^OutputStreamWriter (:out this))
    (.close ^Socket (:socket this))))

(defn logentries
  "Returns a function which accepts an event and sends it to Logentries.
  Silently drops events when Logentries is down. Attempts to reconnect
  automatically every five seconds. Use:

  (logentries {:token \"2bfbea1e-10c3-4419-bdad-7e6435882e1f\"})

  Options:

  :pool-size            The number of connections to keep open. Default 4.

  :reconnect-interval   How many seconds to wait between attempts to connect.
                        Default 5.

  :claim-timeout        How many seconds to wait for a Logentries connection from
                        the pool. Default 0.1.

  :block-start          Wait for the pool's initial connections to open
                        before returning."
  [opts]
  (let [host               (get opts :host "data.logentries.com")
        port               (get opts :port 80)
        token              (get opts :token)
        claim-timeout      (get opts :claim-timeout 0.1)
        pool-size          (get opts :pool-size 4)
        block-start        (get opts :block-start)
        reconnect-interval (get opts :reconnect-interval 5)
        pool (fixed-pool
               (fn []
                 (info "Connecting to " host port)
                 (let [client (open (LogentriesTokenClient. host port token))]
                   (info "Connected")
                   client))
               (fn [client]
                 (info "Closing connection to " host port)
                 (close client))
               {:size                 pool-size
                :block-start          block-start
                :regenerate-interval  reconnect-interval})]

    (fn [event]
      (with-pool [client pool claim-timeout]
        (send-line client (event-to-le-format event))))))
