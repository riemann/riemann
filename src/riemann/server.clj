(ns riemann.server
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:import (java.net InetSocketAddress)
           (java.util.concurrent Executors)
           (org.jboss.netty.bootstrap ConnectionlessBootstrap)
           (org.jboss.netty.buffer ChannelBufferInputStream)
           (org.jboss.netty.channel ChannelHandler
                                    ChannelHandlerContext
                                    ChannelPipeline
                                    ChannelPipelineFactory
                                    Channels
                                    ExceptionEvent
                                    FixedReceiveBufferSizePredictorFactory
                                    MessageEvent
                                    SimpleChannelUpstreamHandler)
           (org.jboss.netty.channel.group DefaultChannelGroup)
           (org.jboss.netty.channel.socket DatagramChannelFactory)
           (org.jboss.netty.channel.socket.nio NioDatagramChannelFactory))
  (:require [riemann.query :as query])
  (:require [riemann.index :as index])
  (:use [riemann.core])
  (:use [riemann.common])
  (:use clojure.tools.logging)
  (:use lamina.core)
  (:use aleph.tcp)
  (:use gloss.core)
  (:use [protobuf.core])
  (:use [slingshot.slingshot :only [try+]])
  (:use clojure.stacktrace)
  (:require gloss.io))

(defn handle
  "Handles a msg with the given core."
  [core msg]
  (try+
    ; Send each event/state to each stream
    (doseq [event (concat (:events msg) (:states msg))
            stream (deref (:streams core))]
      (stream event))
   
    (if (:query msg)
      ; Handle query
      (let [ast (query/ast (:string (:query msg)))]
          (if-let [i (deref (:index core))]
            {:ok true :events (index/search i ast)}
            {:ok false :error "no index"}))

      ; Generic acknowledge 
      {:ok true})

    ; Some kind of error happened
    (catch [:type :riemann.query/parse-error] {:keys [message]}
      {:ok false :error (str "parse error: " message)})
    (catch Exception e
      (prn "Finally caught" e)
      {:ok false :error (.getMessage e)})))

(defn tcp-handler
  "Returns a handler that applies messages to the given core."
  [core]
  (fn [channel client-info]
    (receive-all channel (fn [buffer]
      (when buffer
        ; channel isn't closed; this is our message
        (try
          (enqueue channel (encode (handle core (decode buffer))))
          (catch java.nio.channels.ClosedChannelException e
            (warn (str "channel closed")))
          (catch com.google.protobuf.InvalidProtocolBufferException e
            (warn (str "invalid message, closing " client-info))
            (close channel))
          (catch Exception e
            (warn e "Handler error")
            (close channel))))))))

(defn tcp-server
  "Create a new TCP server for a core. Starts immediately. Options:
  :port   The port to listen on.
  :host   The host to listen on."
  ([core] (tcp-server core {}))
  ([core opts]
    (let [opts (merge {:port 5555
                       :frame (finite-block :int32)}
                      opts)
        handler (tcp-handler core)
        server (start-tcp-server handler opts)] 
      (info (str "TCP server " (select-keys [:host :port] opts) " online"))
      server)))

(defn udp-handler
  "Returns a UDP handler for the given core."
  [core channel-group]
  (proxy [SimpleChannelUpstreamHandler] []
    (channelOpen [context state-event]
                 (.add channel-group (.getChannel state-event)))

    (messageReceived [context message-event]
                     (let [instream (ChannelBufferInputStream.
                                      (.getMessage message-event))
                           msg (decode-inputstream instream)]
                       (handle core msg)))
    
    (exceptionCaught [context exception-event]
                     (warn (.getCause exception-event) "UDP handler caught"))))

(defn udp-cpf
  "Channel Pipeline Factory"
  [core channel-group]
  (proxy [ChannelPipelineFactory] []
    (getPipeline []
                 (let [p (Channels/pipeline)
                       h (udp-handler core channel-group)]
                   (.addLast p "riemann-udp-handler" h) 
                   p))))

(defn udp-server
  "Starts a new UDP server for a core. Starts immediately. 

  IMPORTANT: The UDP server has a maximum datagram size--by default, 16384
  bytes. If your client does not agree on the maximum datagram size (and send
  big messages over TCP instead), it can send large messages which will be
  dropped with protobuf parse errors in the log.
  
  Options:
  :port   The port to listen on.
  :max-size   The maximum datagram size (default 16384 bytes)."
  ([core] (udp-server core {}))
  ([core opts]
   (let [opts (merge {:port 5555
                      :max-size 16384}
                     opts)
         bootstrap (ConnectionlessBootstrap.
                     (NioDatagramChannelFactory.
                       (Executors/newCachedThreadPool)))
         all-channels (DefaultChannelGroup.)
         cpf (udp-cpf core all-channels)] 
    
     ; Configure bootstrap
     (doto bootstrap
       (.setPipelineFactory cpf)
       (.setOption "broadcast" "false")
       (.setOption "receiveBufferSizePredictorFactory"
                   (FixedReceiveBufferSizePredictorFactory. (:max-size opts))))

     ; Start bootstrap
     (let [server-channel (.bind bootstrap (InetSocketAddress. (:port opts)))]
       (.add all-channels server-channel))
     (info "UDP server " opts " online")

     ; fn to close server
     (fn []
       (-> all-channels .close .awaitUninterruptibly)
       (.releaseExternalResources bootstrap)))))
