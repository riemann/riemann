(ns riemann.transport.tcp
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:import [java.net InetSocketAddress]
           [java.util.concurrent Executors]
           [java.nio.channels ClosedChannelException]
           (javax.net.ssl SSLContext)
           [io.netty.bootstrap ServerBootstrap]
           [io.netty.buffer ByteBufUtil]
           [io.netty.channel Channel
                             ChannelOption
                             ChannelInitializer
                             ChannelHandler
                             ChannelHandlerContext
                             ChannelFutureListener
                             ChannelInboundHandlerAdapter]
           [io.netty.channel.group ChannelGroup]
           [io.netty.handler.codec LengthFieldBasedFrameDecoder
                                   LengthFieldPrepender]
           [io.netty.handler.ssl SslHandler]
           [io.netty.channel.epoll EpollEventLoopGroup EpollServerSocketChannel]
           [io.netty.channel.kqueue KQueueEventLoopGroup KQueueServerSocketChannel]
           [io.netty.channel.nio NioEventLoopGroup]
           [io.netty.channel.socket.nio NioServerSocketChannel])
  (:require [less.awful.ssl :as ssl]
            [riemann.test :as test]
            [interval-metrics.core :as metrics])
  (:use [clojure.tools.logging :only [info warn]]
        [interval-metrics.measure :only [measure-latency]]
        [riemann.instrumentation :only [Instrumented]]
        [riemann.service :only [Service ServiceEquiv]]
        [riemann.time :only [unix-time]]
        [riemann.transport :only [handle
                                  ioutil-lock
                                  protobuf-decoder
                                  protobuf-encoder
                                  msg-decoder
                                  msg-encoder
                                  shared-event-executor
                                  shutdown-event-executor-group
                                  channel-group
                                  channel-initializer]]))

(defn int32-frame-decoder
  []
  ; Offset 0, 4 byte header, skip those 4 bytes.
  (LengthFieldBasedFrameDecoder. Integer/MAX_VALUE, 0, 4, 0, 4))

(defn int32-frame-encoder
  []
  (LengthFieldPrepender. 4))

(defn gen-tcp-handler
  "Wraps Netty boilerplate for common TCP server handlers. Given a reference to
  a core, a stats package, a channel group, and a handler fn, returns a
  ChannelInboundHandlerAdapter which calls (handler core stats
  channel-handler-context message) for each received message.

  Automatically handles channel closure, and handles exceptions thrown by the
  handler by logging an error and closing the channel."
  [core stats ^ChannelGroup channel-group handler]
  (proxy [ChannelInboundHandlerAdapter] []
    (channelActive [ctx]
      (.add channel-group (.channel ctx)))

    (channelRead [^ChannelHandlerContext ctx ^Object message]
      (try
        (handler @core stats ctx message)
        (catch java.nio.channels.ClosedChannelException e
          (warn "channel closed"))))

    (exceptionCaught [^ChannelHandlerContext ctx ^Throwable cause]
      (warn cause "TCP handler caught")
      (.close (.channel ctx)))

    (isSharable [] true)))

(defn kqueue-netty-implementation
   []
   {:event-loop-group-fn #(KQueueEventLoopGroup.)
     :channel KQueueServerSocketChannel})

(defn epoll-netty-implementation
   []
   {:event-loop-group-fn #(EpollEventLoopGroup.)
     :channel EpollServerSocketChannel})

(defn nio-netty-implementation
   []
   {:event-loop-group-fn #(NioEventLoopGroup.)
     :channel NioServerSocketChannel})

(def netty-implementation
  (let [mac-or-freebsd? (re-find #"(mac|freebsd)" (System/getProperty "os.name"))
       linux? (re-find  #"linux" (System/getProperty "os.name"))
       sfbit? (re-find #"(x86_64|amd64)" (System/getProperty "os.arch"))
       native?  (= (System/getProperty "netty.native.implementation") "true")]
    (cond (and native? sfbit? linux?) (epoll-netty-implementation)
             (and native? sfbit? mac-or-freebsd?) (kqueue-netty-implementation)
             ::else (nio-netty-implementation))))

(defn tcp-handler
  "Given a core, a channel, and a message, applies the message to core and
  writes a response back on this channel."
  [core stats ^ChannelHandlerContext ctx ^Object message]
  (let [t1 (:decode-time message)]
    (.. ctx
      ; Actually handle request
      (writeAndFlush (handle core message))

      ; Record time from parse to write completion
      (addListener
        (reify ChannelFutureListener
          (operationComplete [this fut]
            (metrics/update! stats
                             (- (System/nanoTime) t1))))))))

(defrecord TCPServer [^String host
                      ^int port
                      ^int so-backlog
                      equiv
                      ^ChannelGroup channel-group
                      ^ChannelInitializer initializer
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
          (when-not test/*testing*
            (locking ioutil-lock
              (locking this
                (when-not @killer
                  (let [event-loop-group-fn (:event-loop-group-fn
                                             netty-implementation)
                        boss-group (event-loop-group-fn)
                        worker-group (event-loop-group-fn)
                        bootstrap (ServerBootstrap.)]
                    ; Configure bootstrap
                    (doto bootstrap
                      (.group boss-group worker-group)
                      (.channel (:channel netty-implementation))
                      (.option ChannelOption/SO_REUSEADDR true)
                      (.option ChannelOption/SO_BACKLOG so-backlog)
                      (.childOption ChannelOption/SO_REUSEADDR true)
                      (.childOption ChannelOption/SO_KEEPALIVE true)
                      (.childHandler initializer))
                    ; Start bootstrap
                    (->> (InetSocketAddress. host port)
                         (.bind bootstrap)
                         (.sync)
                         (.channel)
                         (.add channel-group))
                    (info "TCP server" host port "online")
                    ; fn to close server
                    (reset! killer
                            (fn killer []
                              (.. channel-group close awaitUninterruptibly)
                                        ; Shut down workers and boss concurrently.
                              (let [w (shutdown-event-executor-group worker-group)
                                    b (shutdown-event-executor-group boss-group)]
                                @w
                                @b)
                              (info "TCP server" host port "shut down")))))))))

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
                      :tags ["riemann"]
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
    ; TODO: Where did this go in 4.0.21?
    ; (doto (.setEnableRenegotiation false))
    ))

(defn initializer
  "A channel pipeline initializer for a TCP server."
  [core stats channel-group ssl-context]
  ; Gross hack; should re-work the pipeline macro
  (if ssl-context
    (channel-initializer
               ssl                 (ssl-handler ssl-context)
               int32-frame-decoder (int32-frame-decoder)
      ^:shared int32-frame-encoder (int32-frame-encoder)
      ^:shared protobuf-decoder    (protobuf-decoder)
      ^:shared protobuf-encoder    (protobuf-encoder)
      ^:shared msg-decoder         (msg-decoder)
      ^:shared msg-encoder         (msg-encoder)
      ^{:shared true :executor shared-event-executor} handler
      (gen-tcp-handler core stats channel-group tcp-handler))

    (channel-initializer
               int32-frame-decoder  (int32-frame-decoder)
      ^:shared int32-frame-encoder  (int32-frame-encoder)
      ^:shared protobuf-decoder     (protobuf-decoder)
      ^:shared protobuf-encoder     (protobuf-encoder)
      ^:shared msg-decoder          (msg-decoder)
      ^:shared msg-encoder          (msg-encoder)
      ^{:shared true :executor shared-event-executor} handler
      (gen-tcp-handler core stats channel-group tcp-handler))))

(defn tcp-server
  "Create a new TCP server. Doesn't start until (service/start!).

  Options:
  :host             The host to listen on (default 127.0.0.1).
  :port             The port to listen on. (default 5554 with TLS, or 5555 std)
  :core             An atom used to track the active core for this server.
  :so-backlog       The maximum queue length for incoming tcp connections (default 50).
  :channel-group    A global channel group used to track all connections.
  :initializer      A ChannelInitializer for creating new pipelines.

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
         so-backlog    (get opts :so-backlog 50)
         channel-group (get opts :channel-group
                            (channel-group
                              (str "tcp-server " host ":" port)))
         equiv         (select-keys opts [:tls? :key :cert :ca-cert])
         ; Use the supplied pipeline factory...
         initializer (get opts :initializer
                          ; or construct one for ourselves!
                          (if (:tls? opts)
                            ; A TLS-enabled handler
                            (do
                              (assert (:key opts))
                              (assert (:cert opts))
                              (assert (:ca-cert opts))
                              (let [ssl-context (ssl/ssl-context
                                                  (:key opts)
                                                  (:cert opts)
                                                  (:ca-cert opts))]
                                (initializer core stats channel-group
                                             ssl-context)))

                            ; A standard handler
                            (initializer core stats channel-group nil)))]

       (TCPServer. host port so-backlog equiv channel-group initializer core stats
                   (atom nil)))))
