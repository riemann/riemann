(ns riemann.graphite
  "Forwards events to Graphite."
  (:refer-clojure :exclude [replace])
  (:import [java.net Socket])
  (:import [java.io Writer])
  (:import [java.io OutputStreamWriter])
  (:import [java.util.concurrent ArrayBlockingQueue]
           (java.net InetSocketAddress)
           (java.util.concurrent Executors)
           (org.jboss.netty.util CharsetUtil)
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
           (org.jboss.netty.handler.codec.string StringDecoder StringEncoder)
           (org.jboss.netty.handler.codec.frame LengthFieldBasedFrameDecoder
                                                LengthFieldPrepender
                                                DelimiterBasedFrameDecoder
                                                Delimiters)
           (org.jboss.netty.handler.codec.oneone OneToOneDecoder)
           (org.jboss.netty.handler.execution
            ExecutionHandler
            OrderedMemoryAwareThreadPoolExecutor
            MemoryAwareThreadPoolExecutor))
  (:use [clojure.string :only [split join replace]])
  (:use clojure.tools.logging)
  (:use riemann.common)
  (:require [riemann.server :as server]))

(defn graphite-path-basic
  "Constructs a path for an event. Takes the hostname fqdn, reversed,
  followed by the service, with spaces converted to dots."
  [event]
  (let [service (:service event)
        host (:host event)
        split-service (if service (split service #" ") [])
        split-host (if host (split host #"\.") [])]
     (join "." (concat (reverse split-host) split-service))))

(defn graphite-path-percentiles
  "Like graphite-service-basic, but also converts trailing decimals like 0.95
  to 95."
  [event]
  (graphite-path-basic
    (if-let [service (:service event)]
      (assoc event :service
             (replace service
                      #"(\d+\.\d+)$"
                      (fn [[_ x]] (str (int (* 100 (read-string x)))))))
      event)))

(defn graphite 
  "Returns a function which accepts an event and sends it to Graphite.
  Silently eats events when graphite is down. Attempts to reconnect
  automatically every five seconds. Use:
  
  (graphite {:host \"graphite.local\" :port 2003})
  
  Set :path (fn [event] some-string) to change the path for each event. Uses
  graphite-path-percentiles by default."
  [opts]
  (let [opts (merge {:host "localhost" 
                     :port 2003
                     :socket-count (* 2 (.availableProcessors
                                         (Runtime/getRuntime)))
                     :path graphite-path-percentiles} opts)
        sockets (ArrayBlockingQueue. (:socket-count opts) true)
        add-socket (fn []
                     (info (str "Opening connection to " opts))
                     (let [sock (Socket. (:host opts) (:port opts))
                           out (OutputStreamWriter.(.getOutputStream sock))]
                       (.offer sockets [sock out])))
        path (:path opts)]

    ; Try to connect immediately
    (try
      (dotimes [n (:socket-count opts)]
        (add-socket))
      (catch Exception e
        (warn e (str "Couldn't send to graphite " opts))))
    (fn [event]
      (when (:metric event)
        (let [[sock out] (.take sockets)
              string (str (join " " [(path event) 
                                     (float (:metric event))
                                     (int (:time event))])
                          "\n")]
          (try
            (.write ^OutputStreamWriter out string)
            (.flush ^OutputStreamWriter out)
            (.offer sockets [sock out])
            (catch Exception e
              (warn e (str "Couldn't send to graphite " opts))
              (.close out)
              (.close sock)
              (future
                ; Reconnect in 5 seconds
                (Thread/sleep 5000)
                (add-socket)))))))))

(defn graphite-frame-decoder
  "A closure which yields a graphite frame-decoder. Taking an argument
   which will be given to decode-graphite-line (hence the closure)"
  [parser-fn]
  (fn []
    (proxy [OneToOneDecoder] []
      (decode [context channel message]
        (decode-graphite-line message parser-fn)))))

(defn graphite-handler
  "Returns a Graphite handler for the given core"
  [core ^ChannelGroup channel-group]
  (proxy [SimpleChannelHandler] []
    (channelOpen [context ^ChannelStateEvent state-event]
                 (.add channel-group (.getChannel state-event)))
    
    (messageReceived [^ChannelHandlerContext context 
                      ^MessageEvent message-event]
      (let [channel (.getChannel message-event)
            msg (.getMessage message-event)]
        (try
          (let  [response (server/handle core msg)
                 encoded (encode response)]
            (.write channel (ChannelBuffers/wrappedBuffer encoded)))
         (catch java.nio.channels.ClosedChannelException e
           (warn "channel closed")))))
    (exceptionCaught [context ^ExceptionEvent exception-event]
      (warn (.getCause exception-event) "Graphite handler caught")
      (.close (.getChannel exception-event)))))

(defn graphite-cpf
  "Graphite Channel Pipeline Factory"
  [core channel-group message-decoder]
  (warn "graphite-cpf")
  (proxy [ChannelPipelineFactory] []
    (getPipeline []
      (let [decoder (StringDecoder. CharsetUtil/UTF_8)
            encoder (StringEncoder. CharsetUtil/UTF_8)
            executor (ExecutionHandler.
                                  (OrderedMemoryAwareThreadPoolExecutor.
                                    16 1048576 1048576)) ;; Magic is the best!
            handler  (graphite-handler core channel-group)]
        (doto (Channels/pipeline)
          (.addLast "framer" (DelimiterBasedFrameDecoder.
                              1024 ;; Will the magic ever stop ?
                              (Delimiters/lineDelimiter)))
          (.addLast "string-decoder" decoder)
          (.addLast "string-encoder" encoder)
          (.addLast "message-decoder" (message-decoder))
          (.addLast "executor" executor)
          (.addLast "handler" handler))))))

(defn graphite-server
  "Start a graphite-server, some bits could be factored with tcp-server.
   Only the default option map and the bootstrap change."
  ([core] (graphite-server core {}))
  ([core opts]
   (let [opts (merge {:host "localhost"
                      :port 2003
                      :message-decoder graphite-frame-decoder}
                     opts)
         bootstrap (ServerBootstrap.
                     (NioServerSocketChannelFactory.
                       (Executors/newCachedThreadPool)
                       (Executors/newCachedThreadPool)))
         all-channels (DefaultChannelGroup. (str "graphite-server " opts))
         cpf (graphite-cpf core all-channels
                           ((:message-decoder opts) (:parser-fn opts)))]

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
     (info "Graphite server" opts " online")

     ; fn to close server
     (fn []
       (-> all-channels .close .awaitUninterruptibly)
       (.releaseExternalResources bootstrap)))))
