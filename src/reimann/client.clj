(ns reimann.client
  "Network client for connecting to a Reimann server. Usage:
  
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
  (:use [reimann.common])
  (:use [lamina.core])
  (:use [lamina.connections])
  (:use [gloss.core])
  (:use [protobuf.core])
  (:use clojure.tools.logging))

(defn open-tcp-conn 
  "Opens a TCP connection on client. Modifies client's connection ref."
  [client]
  (debug (str "opening TCP connection to " client))
  (dosync
    ; Close this client
    (when-let [cur (deref (:conn client))]
      (close (deref (:conn client))))

    ; Open new client
    (ref-set (:conn client)
               (wait-for-result
                 (aleph.tcp/tcp-client {:host (:host client)
                                        :port (:port client)
                                        :frame (finite-block :int32)})))))

(defn send-message-raw
  "Send bytes over the given client and await reply, no error handling."
  [client raw]
  (let [c (deref (:conn client))]
    (enqueue c raw)
    (wait-for-message c 5000)))

(defn send-message
  "Send a message over the given client, and await reply.
  Will retry connections once, then fail returning false."
  [client message]
  (locking client
    (let [raw (encode message)]
       (try 
         (decode (send-message-raw client raw))
         (catch Exception e
           (warn e "first send failed, retrying")
           (open-tcp-conn client)
           (decode (send-message-raw client raw)))))))

(defn query 
  "Query the server for events in the index. Returns a list of events."
  [client string]
  (let [resp (send-message client 
                           {:query (protobuf Query :string string)})]
    (:states resp)))

(defn send-event-protobuf
  "Send an event Protobuf."
  [client event]
  (send-message client {:events [event]}))

(defn send-event
  "Send an event over client."
  [client eventmap]
  (send-message client {:events [eventmap]}))

(defn send-state-protobuf 
  "Send a state Protobuf."
  [client event]
  (send-message client {:states [event]}))

(defn send-state 
  "Send a state."
  [client statemap]
  (send-message client {:states [statemap]}))

(defstruct tcp-client-struct :host :port :conn)

(defn tcp-client 
  "Create a new TCP client. Example:

  (tcp-client)
  (tcp-client :host \"foo\" :port 5555)"
  [& { :keys [host port]
       :or {port 5555
            host "localhost"}
       :as opts}]
  (let [c (struct tcp-client-struct host port (ref nil))]
    (open-tcp-conn c)
    c))

(defn close-client
  "Close a client."
  [client]
  (dosync
    (lamina.core/close (deref (:conn client)))))
