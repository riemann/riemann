(ns reimann.server
  (:use [reimann.core])
  (:use [reimann.common])
  (:require [reimann.query :as query])
  (:require [reimann.index :as index])
  (:use clojure.contrib.logging)
  (:use lamina.core)
  (:use aleph.tcp)
  (:use gloss.core)
  (:use [protobuf.core])
  (:require gloss.io))

; maybe later
;(defn udp-server []
;  (let [channel (wait-for-result (udp-socket {:port 5555}))]   

; Handles a buffer with the given core.
(defn handle [core buffer]
  (let [msg (decode buffer)]
    ; Send each event/state to each stream
    (doseq [event (concat (:events msg) (:states msg))
            stream (deref (:streams core))]
      (stream event))
   
    (if (:query msg)
      ; Handle query
      (let [ast (query/ast (:string (:query msg)))]
        (if-let [i (deref (:index core))]
          (protobuf Msg :ok true :states (index/search i ast))
          (protobuf Msg :ok false :error "no index")))

      ; Generic acknowledge 
      (protobuf Msg :ok true))))

; Returns a handler that applies messages to the given streams (by reference)
(defn handler [core]
  (fn [channel client-info]
    (receive-all channel (fn [buffer]
      (when buffer
        ; channel isn't closed; this is our message
        (try
          (enqueue channel (protobuf-dump (handle core buffer)))
          (catch java.nio.channels.ClosedChannelException e
            (log :warn (str "channel closed")))
          (catch com.google.protobuf.InvalidProtocolBufferException e
            (log :warn (str "invalid message, closing " client-info))
            (close channel))
          (catch Exception e
            (log :warn (str "Exception " e))
            (close channel))))))))

(defn tcp-server
  ([core]
    (tcp-server core {}))
  ([core opts]
  (let [handler (handler core)]
    (start-tcp-server handler 
      (merge {
        :port 5555
        :frame (finite-block :int32)
      } opts)))))
