(ns riemann.transport.udp
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:import [java.net InetSocketAddress]
           [java.util.concurrent Executors]
           [io.netty.bootstrap Bootstrap]
           [io.netty.channel Channel
                             ChannelHandler
                             ChannelInitializer
                             ChannelOption
                             ChannelHandlerContext
                             DefaultMessageSizeEstimator
                             ChannelOutboundHandler
                             ChannelInboundHandler
                             ChannelInboundHandlerAdapter
                             FixedRecvByteBufAllocator]
           [io.netty.channel.group ChannelGroup]
           [io.netty.channel.socket.nio NioDatagramChannel]
           [io.netty.channel.nio NioEventLoopGroup])
  (:require [interval-metrics.core :as metrics])
  (:use [clojure.tools.logging :only [warn info]]
        [clojure.string        :only [split]]
        [riemann.instrumentation :only [Instrumented]]
        [riemann.service       :only [Service ServiceEquiv]]
        [riemann.time          :only [unix-time]]
        [riemann.transport     :only [handle
                                      ioutil-lock
                                      channel-group
                                      datagram->byte-buf-decoder
                                      protobuf-decoder
                                      msg-decoder
                                      shutdown-event-executor-group
                                      shared-event-executor
                                      channel-initializer]]))

(defn gen-udp-handler
  [core stats ^ChannelGroup channel-group handler]
  (proxy [ChannelInboundHandlerAdapter] []
    (channelActive [^ChannelHandlerContext ctx]
      (.add channel-group (.channel ctx)))

    (channelRead [^ChannelHandlerContext ctx ^Object message]
      (handler @core stats ctx message))

    (exceptionCaught [^ChannelHandlerContext ctx ^Throwable cause]
      (warn cause "UDP handler caught"))

    (isSharable [] true)))

(defn udp-handler
  "Given a core, a channel, and a message, applies the message to core."
  [core stats ctx message]
  (handle core message)
  (metrics/update! stats (- (System/nanoTime) (:decode-time message))))

(defrecord UDPServer [^String host
                      ^int port
                      max-size
                      ^int so-rcvbuf
                      ^ChannelGroup channel-group
                      ^ChannelHandler handler
                      stats
                      core
                      killer]
  ; core is an atom to a core
  ; killer is an atom to a function that shuts down the server

  ServiceEquiv
  ; TODO compare pipeline-factory!
  (equiv? [this other]
          (and (instance? UDPServer other)
               (= max-size (:max-size other))
               (= so-rcvbuf (:so-rcvbuf other))
               (= host (:host other))
               (= port (:port other))))

  Service
  (conflict? [this other]
             (and (instance? UDPServer other)
                  (= max-size (:max-size other))
                  (= so-rcvbuf (:so-rcvbuf other))
                  (= host (:host other))
                  (= port (:port other))))

  (reload! [this new-core]
           (reset! core new-core))

  (start! [this]
          (locking ioutil-lock
            (locking this
              (when-not @killer
                (let [worker-group (NioEventLoopGroup.)
                      bootstrap (Bootstrap.)]

                  ; Configure bootstrap
                  (doto bootstrap
                    (.group worker-group)
                    (.channel NioDatagramChannel)
                    (.option ChannelOption/SO_BROADCAST false)
                    (.option ChannelOption/MESSAGE_SIZE_ESTIMATOR
                             (DefaultMessageSizeEstimator. max-size))
                    (.option ChannelOption/RCVBUF_ALLOCATOR
                             (FixedRecvByteBufAllocator. max-size))
                    (.handler handler))

                  ; Setup Channel options
                  (if (> so-rcvbuf 0) (.option bootstrap ChannelOption/SO_RCVBUF so-rcvbuf))

                  ; Start bootstrap
                  (->> (InetSocketAddress. host port)
                       (.bind bootstrap)
                       (.sync)
                       (.channel)
                       (.add channel-group))

                  (info "UDP server" host port max-size so-rcvbuf "online")

                  ; fn to close server
                  (reset! killer
                          (fn killer []
                            (-> channel-group .close .awaitUninterruptibly)
                            @(shutdown-event-executor-group worker-group)
                            (info "UDP server" host port max-size so-rcvbuf "shut down"))))))))

  (stop! [this]
         (locking this
           (when @killer
             (@killer)
             (reset! killer nil))))

  Instrumented
  (events [this]
          (let [svc  (str "riemann server udp " host ":" port)
                in   (metrics/snapshot! stats)
                base {:state "ok"
                      :tags ["riemann"]
                      :time (:time in)}]
            (map (partial merge base)
                 (concat [{:service (str svc " in rate")
                           :metric (:rate in)}]
                         (map (fn [[q latency]]
                                {:service (str svc " in latency " q)
                                 :metric latency})
                              (:latencies in)))))))

(defn udp-server
  "Starts a new UDP server. Doesn't start until (service/start!).

  IMPORTANT: The UDP server has a maximum datagram size--by default, 16384
  bytes. If your client does not agree on the maximum datagram size (and send
  big messages over TCP instead), it can send large messages which will be
  dropped with protobuf parse errors in the log.

  Options:
  :host             The address to listen on (default 127.0.0.1).
  :port             The port to listen on (default 5555).
  :max-size         The maximum datagram size (default 16384 bytes).
  :so-rcvbuf        The socket option for receive buffer in bytes (SO_RCVBUF)
  :channel-group    A ChannelGroup used to track all connections
  :initializer      A ChannelInitializer"
  ([] (udp-server {}))
  ([opts]
   (let [core  (get opts :core (atom nil))
         stats (metrics/rate+latency)
         host  (get opts :host "127.0.0.1")
         port  (get opts :port 5555)
         max-size (get opts :max-size 16384)
         so-rcvbuf (get opts :so-rcvbuf -1)
         channel-group (get opts :channel-group
                            (channel-group
                              (str "udp-server" host ":" port "(" max-size ")")))
         ci (get opts :initializer
                 (channel-initializer
                   ^:shared datagram-decoder (datagram->byte-buf-decoder)
                   ^:shared protobuf-decoder (protobuf-decoder)
                   ^:shared msg-decoder      (msg-decoder)
                   ^{:shared true :executor shared-event-executor} handler
                   (gen-udp-handler core stats channel-group udp-handler)))]
     (UDPServer. host port max-size so-rcvbuf channel-group ci stats core (atom nil)))))
