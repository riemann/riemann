(ns riemann.transport.tcp
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:import [java.net InetSocketAddress]
           [java.util.concurrent Executors]
           [java.nio.channels ClosedChannelException]
           [org.jboss.netty.bootstrap ServerBootstrap]
           [org.jboss.netty.buffer ChannelBuffers]
           [org.jboss.netty.channel ChannelHandler
                                    ChannelHandlerContext
                                    ChannelPipeline
                                    ChannelPipelineFactory
                                    ChannelStateEvent
                                    Channels
                                    ExceptionEvent
                                    MessageEvent
                                    SimpleChannelHandler]
           [org.jboss.netty.channel.group ChannelGroup DefaultChannelGroup]
           [org.jboss.netty.channel.socket.nio NioServerSocketChannelFactory]
           [org.jboss.netty.handler.codec.frame LengthFieldBasedFrameDecoder
                                                LengthFieldPrepender]
           [org.jboss.netty.handler.execution
            OrderedMemoryAwareThreadPoolExecutor])
  (:use [riemann.transport :only [handle 
                                  protobuf-decoder
                                  protobuf-encoder
                                  msg-decoder
                                  channel-pipeline-factory]]
        [riemann.codec :only [encode-pb-msg]]
        [riemann.service :only [Service]]
        [clojure.tools.logging :only [info warn]]
        [riemann.transport :only [handle]]))

(defn int32-frame-decoder
  []
  ; Offset 0, 4 byte header, skip those 4 bytes.
  (LengthFieldBasedFrameDecoder. Integer/MAX_VALUE, 0, 4, 0, 4))

(defn int32-frame-encoder
  []
  (LengthFieldPrepender. 4))

(defn tcp-handler
  "Returns a TCP handler around the given atom pointing to a core"
  [core ^ChannelGroup channel-group encode-fn]
  (proxy [SimpleChannelHandler] []
    (channelOpen [context ^ChannelStateEvent state-event]
      (.add channel-group (.getChannel state-event)))

    (messageReceived [^ChannelHandlerContext context
                      ^MessageEvent message-event]
      (let [channel (.getChannel message-event)
            msg     (.getMessage message-event)]
        (try
          (let [out (handle @core msg)]
            (when encode-fn
              (.write channel (encode-fn out))))
          (catch java.nio.channels.ClosedChannelException e
            (warn "channel closed"))
          (catch com.google.protobuf.InvalidProtocolBufferException e
            (warn "invalid message, closing")
            (.close channel)))))
    
    (exceptionCaught [context ^ExceptionEvent exception-event]
      (let [cause (.getCause exception-event)]
        (when-not (= ClosedChannelException (class cause))
          (warn (.getCause exception-event) "TCP handler caught")
          (.close (.getChannel exception-event)))))))

(defrecord TCPServer [host port pipeline-factory core killer write-back encode-fn]
  ; core is a reference to a core
  ; killer is a reference to a function which shuts down the server.

  Service
  ; TODO compare pipeline-factory!
  (equiv? [this other]
          (and (instance? TCPServer other)
               (= host (:host other))
               (= port (:port other))))
  
  (reload! [this new-core]
           (reset! core new-core))

  (start! [this]
          (locking this
            (when-not @killer
              (let [bootstrap (ServerBootstrap.
                                (NioServerSocketChannelFactory.
                                  (Executors/newCachedThreadPool)
                                  (Executors/newCachedThreadPool)))
                    all-channels (DefaultChannelGroup. 
                                   (str "tcp-server " host ":" port))
                    cpf (channel-pipeline-factory 
                          pipeline-factory
                          (tcp-handler core all-channels
                                       (if write-back encode-fn)))]

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
                                            (InetSocketAddress. host port))]
                  (.add all-channels server-channel))
                (info "TCP server" host port "online")

                ; fn to close server
                (reset! killer 
                        (fn []
                          (-> all-channels .close .awaitUninterruptibly)
                          (.releaseExternalResources bootstrap)
                          (info "TCP server" host port "shut down")))))))

  (stop! [this]
         (locking this
           (when @killer
             (@killer)
             (reset! killer nil)))))

(defn tcp-server
  "Create a new TCP server. Doesn't start until (service/start!). Options:
  :host   The host to listen on (default 127.0.0.1).
  :port   The port to listen on. (default 5555)
  :pipeline-factory"
  ([]
   (tcp-server {}))
  ([opts]
     (let [pipeline-factory #(doto (Channels/pipeline)
                               (.addLast "int32-frame-decoder"
                                         (int32-frame-decoder))
                               (.addLast "int32-frame-encoder"
                                         (int32-frame-encoder))
                               (.addLast "protobuf-decoder"
                                         (protobuf-decoder))
                               (.addLast "protobuf-encoder"
                                         (protobuf-encoder))
                               (.addLast "msg-decoder"
                                         (msg-decoder)))]
       (TCPServer.
         (get opts :host "127.0.0.1")
         (get opts :port 5555)
         (get opts :pipeline-factory pipeline-factory)
         (atom nil)
         (atom nil)
         (get opts :write-back true)
         (get opts :encode-fn encode-pb-msg)))))
