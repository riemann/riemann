(ns riemann.server
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:import (java.net InetSocketAddress)
           (java.util.concurrent Executors)
           (org.jboss.netty.bootstrap ConnectionlessBootstrap
                                      ServerBootstrap)
           (org.jboss.netty.buffer ChannelBufferInputStream
                                   ChannelBuffers)
           (org.jboss.netty.channel ChannelHandler
                                    ChannelHandlerContext
                                    ChannelPipeline
                                    ChannelPipelineFactory
                                    ChannelStateEvent
                                    Channels
                                    ExceptionEvent
                                    FixedReceiveBufferSizePredictorFactory
                                    MessageEvent
                                    SimpleChannelHandler
                                    SimpleChannelUpstreamHandler)
           (org.jboss.netty.channel.group ChannelGroup
                                          DefaultChannelGroup)
           (org.jboss.netty.channel.socket DatagramChannelFactory)
           (org.jboss.netty.channel.socket.nio NioDatagramChannelFactory
                                               NioServerSocketChannelFactory)
           (org.jboss.netty.handler.codec.frame LengthFieldBasedFrameDecoder
                                                LengthFieldPrepender)
           (org.jboss.netty.handler.execution
             ExecutionHandler
             OrderedMemoryAwareThreadPoolExecutor))
  (:require [riemann.query :as query])
  (:require [riemann.index :as index])
  (:use [riemann.core])
  (:use [riemann.common])
  (:use clojure.tools.logging)
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
    (catch Exception ^Exception e
      {:ok false :error (.getMessage e)})))

(defn int32-frame-decoder 
  []
  ; Offset 0, 4 byte header, skip those 4 bytes.
  (LengthFieldBasedFrameDecoder. Integer/MAX_VALUE, 0, 4, 0, 4))

(defn int32-frame-encoder
  []
  (LengthFieldPrepender. 4))

(defn tcp-handler
  "Returns a TCP handler for the given core"
  [core ^ChannelGroup channel-group]
  (proxy [SimpleChannelHandler] []
    (channelOpen [context ^ChannelStateEvent state-event]
                 (.add channel-group (.getChannel state-event)))
    
    (messageReceived [^ChannelHandlerContext context 
                      ^MessageEvent message-event]
      (let [channel (.getChannel message-event)
            instream (ChannelBufferInputStream.
                       (.getMessage message-event))]
        (try
          (let [msg (decode-inputstream instream)
                response (handle core msg)
                encoded (encode response)]
            (.write channel (ChannelBuffers/wrappedBuffer encoded)))
         (catch java.nio.channels.ClosedChannelException e
           (warn "channel closed"))
         (catch com.google.protobuf.InvalidProtocolBufferException e
           (warn "invalid message, closing")
           (.close channel)))))
    
    (exceptionCaught [context ^ExceptionEvent exception-event]
                     (warn (.getCause exception-event) "TCP handler caught")
                     (.close (.getChannel exception-event)))))


(defn udp-handler
  "Returns a UDP handler for the given core."
  [core ^ChannelGroup channel-group]
  (proxy [SimpleChannelUpstreamHandler] []
    (channelOpen [context ^ChannelStateEvent state-event]
                 (.add channel-group (.getChannel state-event)))

    (messageReceived [context ^MessageEvent message-event]
                     (let [instream (ChannelBufferInputStream.
                                      (.getMessage message-event))
                           msg (decode-inputstream instream)]
                       (handle core msg)))
    
    (exceptionCaught [context ^ExceptionEvent exception-event]
                     (warn (.getCause exception-event) "UDP handler caught"))))

(defn tcp-cpf
  "TCP Channel Pipeline Factory"
  [core channel-group]
  (proxy [ChannelPipelineFactory] []
    (getPipeline []
                 (let [decoder  (int32-frame-decoder)
                       encoder  (int32-frame-encoder)
                       executor (ExecutionHandler.
                                  (OrderedMemoryAwareThreadPoolExecutor.
                                    16 1048576 1048576)) ; Maaagic values!
                       handler  (tcp-handler core channel-group)]
                   (doto (Channels/pipeline)
                     (.addLast "int32-frame-decoder" decoder)
                     (.addLast "int32-frame-encoder" encoder)
                     (.addLast "executor" executor)
                     (.addLast "handler" handler))))))

(defn udp-cpf
  "Channel Pipeline Factory"
  [core channel-group]
  (proxy [ChannelPipelineFactory] []
    (getPipeline []
                 (let [p (Channels/pipeline)
                       h (udp-handler core channel-group)]
                   (.addLast p "riemann-udp-handler" h) 
                   p))))


(defn tcp-server
  "Create a new TCP server for a core. Starts immediately. Options:
  :host   The host to listen on (default localhost).
  :port   The port to listen on. (default 5555)"
  ([core] (tcp-server core {}))
  ([core opts]
   (let [opts (merge {:host "localhost"
                      :port 5555}
                     opts)
         bootstrap (ServerBootstrap.
                     (NioServerSocketChannelFactory.
                       (Executors/newCachedThreadPool)
                       (Executors/newCachedThreadPool)))
         all-channels (DefaultChannelGroup. (str "tcp-server " opts))
         cpf (tcp-cpf core all-channels)]

     ; Configure bootstrap
     (doto bootstrap
       (.setPipelineFactory cpf)
       (.setOption "readWriteFair" true)
       (.setOption "tcpNoDelay" true)
       (.setOption "reuseAddress" true)
       (.setOption "child.tcpNoDelay" true)
       (.setOption "child.reuseAddress" true)
       (.setOption "child.keepAlive" true))

     ; Start bootstrap
     (let [server-channel (.bind bootstrap
                                 (InetSocketAddress. ^String (:host opts) 
                                                     ^Integer (:port opts)))]
       (.add all-channels server-channel))
     (info "TCP server" opts " online")

     ; fn to close server
     (fn []
       (-> all-channels .close .awaitUninterruptibly)
       (.releaseExternalResources bootstrap)))))

(defn udp-server
  "Starts a new UDP server for a core. Starts immediately. 

  IMPORTANT: The UDP server has a maximum datagram size--by default, 16384
  bytes. If your client does not agree on the maximum datagram size (and send
  big messages over TCP instead), it can send large messages which will be
  dropped with protobuf parse errors in the log.
  
  Options:
  :host   The address to listen on (default localhost).
  :port   The port to listen on (default 5555).
  :max-size   The maximum datagram size (default 16384 bytes)."
  ([core] (udp-server core {}))
  ([core opts]
   (let [opts (merge {:host "localhost"
                      :port 5555
                      :max-size 16384}
                     opts)
         bootstrap (ConnectionlessBootstrap.
                     (NioDatagramChannelFactory.
                       (Executors/newCachedThreadPool)))
         all-channels (DefaultChannelGroup. (str "udp-server " opts))
         cpf (udp-cpf core all-channels)] 
    
     ; Configure bootstrap
     (doto bootstrap
       (.setPipelineFactory cpf)
       (.setOption "broadcast" "false")
       (.setOption "receiveBufferSizePredictorFactory"
                   (FixedReceiveBufferSizePredictorFactory. (:max-size opts))))

     ; Start bootstrap
     (let [server-channel (.bind bootstrap
                                 (InetSocketAddress. ^String (:host opts)
                                                     ^Integer (:port opts)))]
       (.add all-channels server-channel))
     (info "UDP server" opts "online")

     ; fn to close server
     (fn []
       (-> all-channels .close .awaitUninterruptibly)
       (.releaseExternalResources bootstrap)))))
