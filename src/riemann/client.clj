(ns riemann.client
  "Network client for connecting to a Riemann server. Usage:
  
  (def c (tcp-client :host \"monitoring.local\"))
 
  (send-event c {:service \"fridge\" 
                 :state \"running\" 
                 :metric 2.0
                 :tags [\"joke\"]})

  (query c \"tagged \\\"joke\\\"\")
  => [{:service \"fridge\" ... }]

  (close c)

  Clients are mildly resistant to failure; they will attempt to reconnect a 
  dropped connection once before giving up. I reason that it's easier for you
  to simply catch and log than to handle reconnecting yourself--but that said,
  I'm open to suggestions here."

  (:require [aleph.tcp])
  (:import [riemann.client RiemannClient
                           RiemannTcpClient]
           [java.net InetSocketAddress]
           [java.io IOException])
  (:use [riemann.common])
  (:use [lamina.core])
  (:use [lamina.connections])
  (:use [gloss.core])
  (:use [protobuf.core])
  (:use clojure.tools.logging))

(defn reconnect-client
  "Reconnect client."
  [^RiemannClient client]
  (locking client
    (.disconnect client)
    (.connect client)))

(defmacro with-io-retry
  "Calls body, retries one IOException by reconnecting client, raises if that
  fails too."
  [^RiemannClient client & body]
  `(try
    (do ~@body)
    (catch IOException e#
      (warn "Client recovering from IO exception")
      (reconnect-client ~client)
      (do ~@body))))

(defn query
  "Query the server for events in the index. Returns a list of events."
  [^RiemannClient client string]
  (map decode-pb-event (with-io-retry client (.query client string))))

(defn send-event
  "Send an event over client."
  [^RiemannClient client event]
  (with-io-retry client 
    (let [e (.event client)]
      (when-let [h (:host event)] (.host e h))
      (when-let [s (:service event)] (.service e s))
      (when-let [s (:state event)] (.state e s))
      (when-let [d (:description event)] (.description e d))
      (when-let [m (:metric event)] (.metric e (float m)))
      (when-let [t (:tags event)] (.tags e t))
      (when-let [t (:time event)] (.time e (int t)))
      (when-let [t (:ttl event)] (.ttl e (float t)))

      (.send e))))

(defn tcp-client 
  "Create a new TCP client. Example:

  (tcp-client)
  (tcp-client :host \"foo\" :port 5555)"
  [& { :keys [host port]
       :or {port 5555
            host "localhost"}
       :as opts}]
  (doto (RiemannTcpClient. (InetSocketAddress. host port))
    (.connect)))

(defn close-client
  "Close a client."
  [client]
  (.disconnect client))
