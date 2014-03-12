(ns riemann.transport.tcp
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:import [java.net InetSocketAddress]
           [java.util.concurrent Executors]
           [java.nio.channels ClosedChannelException]
           (javax.net.ssl SSLContext)
           [org.jboss.netty.bootstrap ServerBootstrap]
           [org.jboss.netty.buffer ChannelBuffers]
           [org.jboss.netty.channel ChannelHandler
                                    ChannelFuture
                                    ChannelFutureListener
                                    ChannelHandlerContext
                                    ChannelPipeline
                                    ChannelPipelineFactory
                                    Channels
                                    ChannelStateEvent
                                    ExceptionEvent
                                    MessageEvent
                                    SimpleChannelHandler]
           [org.jboss.netty.channel.group ChannelGroup]
           [org.jboss.netty.channel.socket.nio NioServerSocketChannelFactory]
           [org.jboss.netty.handler.codec.frame LengthFieldBasedFrameDecoder
                                                LengthFieldPrepender]
           [org.jboss.netty.handler.execution
            OrderedMemoryAwareThreadPoolExecutor]
           [org.jboss.netty.handler.ssl SslHandler])
  (:require [less.awful.ssl :as ssl]
            [interval-metrics.core :as metrics])
  (:use [clojure.tools.logging :only [info warn]]
        [interval-metrics.measure :only [measure-latency]]
        [riemann.instrumentation :only [Instrumented]]
        [riemann.service :only [Service ServiceEquiv]]
        [riemann.time :only [unix-time]]
        [riemann.transport :only [handle 
                                  protobuf-decoder
                                  protobuf-encoder
                                  msg-decoder
                                  msg-encoder
                                  shared-execution-handler
                                  channel-group
                                  channel-pipeline-factory]]))

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
  [core stats ^ChannelGroup channel-group handler]
  (proxy [SimpleChannelHandler] []
    (channelOpen [context ^ChannelStateEvent state-event]
      (.add channel-group (.getChannel state-event)))

    (messageReceived [^ChannelHandlerContext context
                      ^MessageEvent message-event]
        (try
          (handler @core stats message-event)
          (catch java.nio.channels.ClosedChannelException e
            (warn "channel closed"))))

    (exceptionCaught [context ^ExceptionEvent exception-event]
      (let [cause (.getCause exception-event)]
        (when-not (instance? ClosedChannelException cause)
          (warn (.getCause exception-event) "TCP handler caught")
          (.close (.getChannel exception-event)))))))

(defn tcp-handler
  "Given a core and a MessageEvent, applies the message to core and writes a
  response back on this channel."
  [core stats ^MessageEvent e]
  (let [msg (.getMessage e)
        t1  (:decode-time msg)]
    (.. e
      getChannel
      ; Actually handle request
      (write (handle core msg))
      ; Record time from parse to write completion
      (addListener
        (reify ChannelFutureListener
          (operationComplete [this fut]
                             (metrics/update! stats
                                              (- (System/nanoTime) t1))))))))

(defrecord TCPServer [^String host
                      ^int port
                      equiv
                      ^ChannelGroup channel-group
                      pipeline-factory
                      core
                      stats
                      killer]
  ; core is a reference to a core
  ; killer is a reference to a function which shuts down the server.

  ; TODO compare pipeline-factory!
  ServiceEquiv
  (equiv? [this other]
          (and (instance? TCPServer other)
               (= host (:host other))
               (= port (:port other))
               (= equiv (:equiv other))))
  
  Service
  (conflict? [this other]
             (and (instance? TCPServer other)
                  (= host (:host other))
                  (= port (:port other))))

  (reload! [this new-core]
           (reset! core new-core))

  (start! [this]
          (locking this
            (when-not @killer
              (let [boss-pool (Executors/newCachedThreadPool)
                    worker-pool (Executors/newCachedThreadPool)
                    bootstrap (ServerBootstrap.
                                (NioServerSocketChannelFactory.
                                  boss-pool
                                  worker-pool))]

                ; Configure bootstrap
                (doto bootstrap
                  (.setPipelineFactory pipeline-factory)
                  (.setOption "readWriteFair" true)
                  (.setOption "tcpNoDelay" true)
                  (.setOption "reuseAddress" true)
                  (.setOption "child.tcpNoDelay" true)
                  (.setOption "child.reuseAddress" true)
                  (.setOption "child.keepAlive" true))

                ; Start bootstrap
                (->> (InetSocketAddress. host port)
                     (.bind bootstrap)
                     (.add channel-group))
                (info "TCP server" host port "online")

                ; fn to close server
                (reset! killer 
                        (fn killer []
                          (-> channel-group .close .awaitUninterruptibly)
                          (.releaseExternalResources bootstrap)
                          (.shutdown worker-pool)
                          (.shutdown boss-pool)
                          (info "TCP server" host port "shut down")))))))

  (stop! [this]
         (locking this
           (when @killer
             (@killer)
             (reset! killer nil))))

  Instrumented
  (events [this]
          (let [svc  (str "riemann server tcp " host ":" port)
                in   (metrics/snapshot! stats)
                base {:state "ok"
                      :time (:time in)}]
            (map (partial merge base)
                 (concat [{:service (str svc " conns")
                           :metric (count channel-group)}
                          {:service (str svc " in rate")
                           :metric (:rate in)}]
                         (map (fn [[q latency]]
                                {:service (str svc " in latency " q)
                                 :metric  latency})
                              (:latencies in)))))))

(defn ssl-handler 
  "Given an SSLContext, creates a new SSLEngine and a corresponding Netty
  SslHandler wrapping it."
  [^SSLContext context]
  (-> context
    .createSSLEngine
    (doto (.setUseClientMode false)
          (.setNeedClientAuth true))
    SslHandler.
    (doto (.setEnableRenegotiation false))))

(defn cpf
  "A channel pipeline factory for a TCP server."
  [core stats channel-group ssl-context]
  ; Gross hack; should re-work the pipeline macro
  (if ssl-context
    (channel-pipeline-factory
               ssl                 (ssl-handler ssl-context)
               int32-frame-decoder (int32-frame-decoder)
      ^:shared int32-frame-encoder (int32-frame-encoder)
      ^:shared executor            shared-execution-handler
      ^:shared protobuf-decoder    (protobuf-decoder)
      ^:shared protobuf-encoder    (protobuf-encoder)
      ^:shared msg-decoder         (msg-decoder)
      ^:shared msg-encoder         (msg-encoder)
      ^:shared handler             (gen-tcp-handler 
                                     core
                                     stats
                                     channel-group
                                     tcp-handler))
    (channel-pipeline-factory
               int32-frame-decoder (int32-frame-decoder)
      ^:shared int32-frame-encoder (int32-frame-encoder)
      ^:shared executor            shared-execution-handler
      ^:shared protobuf-decoder    (protobuf-decoder)
      ^:shared protobuf-encoder    (protobuf-encoder)
      ^:shared msg-decoder         (msg-decoder)
      ^:shared msg-encoder         (msg-encoder)
      ^:shared handler             (gen-tcp-handler 
                                     core
                                     stats
                                     channel-group
                                     tcp-handler))))

(defn tcp-server
  "Create a new TCP server. Doesn't start until (service/start!). Options:
  :host             The host to listen on (default 127.0.0.1).
  :port             The port to listen on. (default 5554 with TLS, or 5555 std)
  :core             An atom used to track the active core for this server
  :channel-group    A global channel group used to track all connections.
  :pipeline-factory A ChannelPipelineFactory for creating new pipelines.
  
  TLS options:
  :tls?             Whether to enable TLS
  :key              A PKCS8-encoded private key file
  :cert             The corresponding public certificate 
  :ca-cert          The certificate of the CA which signed this key"
  ([]
   (tcp-server {}))
  ([opts]
   (let [core          (get opts :core (atom nil))
         stats         (metrics/rate+latency)
         host          (get opts :host "127.0.0.1")
         port          (get opts :port (if (:tls? opts) 5554 5555))
         channel-group (get opts :channel-group
                            (channel-group 
                              (str "tcp-server " host ":" port)))
         equiv         (select-keys opts [:tls? :key :cert :ca-cert])
         ; Use the supplied pipeline factory...
         pf (get opts :pipeline-factory
                 ; or construct one for ourselves!
                 (if (:tls? opts)
                   ; A TLS-enabled handler
                   (do
                     (assert (:key opts))
                     (assert (:cert opts))
                     (assert (:ca-cert opts))
                     (let [ssl-context (ssl/ssl-context (:key opts)
                                                        (:cert opts)
                                                        (:ca-cert opts))]
                       (cpf core stats channel-group ssl-context)))
                   ; A standard handler
                   (cpf core stats channel-group nil)))]

       (TCPServer. host port equiv channel-group pf core stats (atom nil)))))
