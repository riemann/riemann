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
  (:use [riemann.transport     :only [handle protobuf-frame-decoder
                                      channel-pipeline-factory]]
        [clojure.tools.logging :only [info warn]]
        [riemann.transport :only [handle]]
        [riemann.common :only [encode]]))

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
            msg     (.getMessage message-event)]
        (try
          (let  [response (handle core msg)
                 encoded  (encode response)]
            (.write channel (ChannelBuffers/wrappedBuffer encoded)))
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

(defn tcp-server
  "Create a new TCP server for a core. Starts immediately. Options:
  :host   The host to listen on (default 127.0.0.1).
  :port   The port to listen on. (default 5555)"
  ([core]
     (tcp-server core {}))
  ([core opts]
     (let [pipeline-factory #(doto (Channels/pipeline)
                               (.addLast "int32-frame-decoder"
                                         (int32-frame-decoder))
                               (.addLast "int32-frame-encoder"
                                         (int32-frame-encoder))
                               (.addLast "protobuf-decoder"
                                         (protobuf-frame-decoder)))
           opts (merge {:host "127.0.0.1"
                        :port 5555
                        :pipeline-factory pipeline-factory}
                       opts)
           bootstrap (ServerBootstrap.
                      (NioServerSocketChannelFactory.
                       (Executors/newCachedThreadPool)
                       (Executors/newCachedThreadPool)))
           all-channels (DefaultChannelGroup. (str "tcp-server " opts))
           cpf (channel-pipeline-factory
                (:pipeline-factory opts) (tcp-handler core all-channels))]

       ;; Configure bootstrap
       (doto bootstrap
         (.setPipelineFactory cpf)
         (.setOption "readWriteFair" true)
         (.setOption "tcpNoDelay" true)
         (.setOption "reuseAddress" true)
         (.setOption "child.tcpNoDelay" true)
         (.setOption "child.reuseAddress" true)
         (.setOption "child.keepAlive" true))
       
       ;; Start bootstrap
       (let [server-channel (.bind bootstrap
                                   (InetSocketAddress. ^String (:host opts)
                                                       ^Integer (:port opts)))]
         (.add all-channels server-channel))
       (info "TCP server" (select-keys opts [:host :port]) "online")
       
       ;; fn to close server
       (fn []
         (-> all-channels .close .awaitUninterruptibly)
         (.releaseExternalResources bootstrap)
         (info "TCP server" (select-keys opts [:host :port]) "shut down")))))