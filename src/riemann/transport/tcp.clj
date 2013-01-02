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
                                  msg-encoder
                                  channel-pipeline-factory]]
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

(defn gen-tcp-handler
  "Wraps netty boilerplate for common TCP server handlers. Given a reference to
  a core, a channel group, and a handler fn, returns a SimpleChannelHandler
  which calls (handler core message-event) with each received message."
  [core ^ChannelGroup channel-group handler]
  (proxy [SimpleChannelHandler] []
    (channelOpen [context ^ChannelStateEvent state-event]
      (.add channel-group (.getChannel state-event)))

    (messageReceived [^ChannelHandlerContext context
                      ^MessageEvent message-event]
        (try
          (handler @core message-event)
          (catch java.nio.channels.ClosedChannelException e
            (warn "channel closed"))))
    
    (exceptionCaught [context ^ExceptionEvent exception-event]
      (let [cause (.getCause exception-event)]
        (when-not (instance? ClosedChannelException cause)
          (warn (.getCause exception-event) "TCP handler caught")
          (.close (.getChannel exception-event)))))))

(defn tcp-handler
  "Given a core and a MessageEvent, applies the message to core."
  [core ^MessageEvent e]
  (.write (.getChannel e)
          (handle core (.getMessage e))))

(defrecord TCPServer [host port pipeline-factory handler core killer]
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
                          (gen-tcp-handler core all-channels handler))]

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
                                         (msg-decoder))
                               (.addLast "msg-encoder"
                                         (msg-encoder)))]
       (TCPServer.
         (get opts :host "127.0.0.1")
         (get opts :port 5555)
         (get opts :pipeline-factory pipeline-factory)
         (get opts :handler tcp-handler)
         (atom nil)
         (atom nil)))))
