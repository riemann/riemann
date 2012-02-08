(ns reimann.client
  (:require [aleph.tcp])
  (:use [reimann.common])
  (:use [lamina.core])
  (:use [lamina.connections])
  (:use [gloss.core])
  (:use [protobuf.core])
  (:use [clojure.contrib.logging]))

; Alter client with a new connection.
(defn open-tcp-conn [client]
  (log :debug (str "opening TCP connection to " client))
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

; Send bytes over the given client and await reply, no error handling.
(defn send-message-raw [client, raw]
  (let [c (deref (:conn client))]
    (enqueue c raw)
    (wait-for-message c 5000)))

; Send a message over the given client, and await reply.
; Will retry connections once, then fail returning false.
(defn send-message [client, message]
  (locking client
    (let [raw (protobuf-dump message)]
       (try 
         (decode (send-message-raw client raw))
         (catch Exception e
           (log :warn "first send failed, retrying" e)
           (try 
             (open-tcp-conn client)
             (decode (send-message-raw client raw))
             (catch Exception e
               (log :warn "second send failed" e)
               false)))))))

(defn query [client string]
  "Query the server for states in the index. Returns a list of states."
  (let [resp (send-message client
               (protobuf Msg :query
                 (protobuf Query :string string)))]
    (:states resp)))

; Send an event Protobuf
(defn send-event-protobuf [client event]
  (send-message client
    (protobuf Msg :events [event])))

; Send an event (any map; will be passed to (event)) over the given client
(defn send-event [client eventmap]
  (send-event-protobuf client (event eventmap)))

; Send a state Protobuf
(defn send-state-protobuf [client event]
  (send-message client
    (protobuf Msg :states [event])))

; Send an event (any map; will be passed to (event)) over the given client
(defn send-state [client statemap]
  (send-state-protobuf client (state statemap)))

(defstruct tcp-client-struct :host :port :conn)

; Create a new TCP client
(defn tcp-client [& { :keys [host port]
                      :or {port 5555
                           host "localhost"}
                      :as opts}]
  (let [c (struct tcp-client-struct host port (ref nil))]
    (open-tcp-conn c)
    c))

; Close a client
(defn close-client [client]
  (dosync
    (lamina.core/close (deref (:conn client)))))
