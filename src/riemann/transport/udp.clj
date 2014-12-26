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
                             ChannelInboundHandlerAdapter]
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
                                      channel-group
                                      protobuf-decoder
                                      protobuf-encoder
                                      msg-decoder
                                      shutdown-event-executor-group
                                      shared-event-executor
                                      channel-pipeline-factory]]))

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

(defn pipeline-initializer-handler
  "A ChannelHandler that just initializes the channel with a pipeline. Lets us
  re-use the pipeline initializer logic from the TCP handler."
  [^ChannelInitializer initializer]
  (proxy [ChannelInboundHandlerAdapter] []
    (channelRegistered [ctx]
      (prn "Handler initializing channel context" ctx)
      (prn "This is" this)
      (let [pipeline (.pipeline ctx)]
        (try
          ; Don't call this initializer again
          (prn "Removing this from pipeline" pipeline)
          (.remove pipeline this)

          ; Replace the pipeline for this context with one from the initializer.
          (prn "registering with" initializer)
          (.channelRegistered initializer ctx)

          (prn "New pipeline is" pipeline)

          ; Propagate registration event
          (prn "Propagating")
          (.fireChannelRegistered ctx)

          (prn "Pipeline setup complete.")
          (catch Throwable e
            (warn e "Failed to initialize channel")
            (.close ctx)))))))

(defrecord UDPServer [^String host
                      ^int port
                      max-size
                      ^ChannelGroup channel-group
                      pipeline-factory
                      stats
                      core
                      killer]
  ; core is an atom to a core
  ; killer is an atom to a function that shuts down the server

  ServiceEquiv
  ; TODO compare pipeline-factory!
  (equiv? [this other]
          (and (instance? UDPServer other)
               (= host (:host other))
               (= port (:port other))))

  Service
  (conflict? [this other]
             (and (instance? UDPServer other)
                  (= host (:host other))
                  (= port (:port other))))

  (reload! [this new-core]
           (reset! core new-core))

  (start! [this]
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
                  (.handler (pipeline-initializer-handler pipeline-factory)))

                ; Start bootstrap
                (prn "Starting bootstrap")
                (->> (InetSocketAddress. host port)
                     (.bind bootstrap)
                     (.sync)
                     (.channel)
                     (.add channel-group))
                (prn "Bootstrap running")
                (info "UDP server" host port max-size "online")

                ; fn to close server
                (reset! killer
                        (fn killer []
                          (prn "Shutting down UDP server")
                          (-> channel-group .close .awaitUninterruptibly)
                          (prn "Channel group shut down.")
                          @(shutdown-event-executor-group worker-group)
                          (prn "UDP server shut down.")
                          (info "UDP server" host port max-size "shut down")))))))

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
  :channel-group    A ChannelGroup used to track all connections
  :pipeline-factory A ChannelInitializer"
  ([] (udp-server {}))
  ([opts]
   (let [core  (get opts :core (atom nil))
         stats (metrics/rate+latency)
         host  (get opts :host "127.0.0.1")
         port  (get opts :port 5555)
         max-size (get opts :max-size 16384)
         channel-group (get opts :channel-group
                            (channel-group
                              (str "udp-server" host ":" port "(" max-size ")")))
         pf (get opts :pipeline-factory
                 (channel-pipeline-factory
                   ^:shared protobuf-decoder (protobuf-decoder)
                   ^:shared protobuf-encoder (protobuf-encoder)
                   ^:shared msg-decoder      (msg-decoder)
                   ^{:shared true :executor shared-event-executor} handler
                   (gen-udp-handler core stats channel-group udp-handler)))]
     (UDPServer. host port max-size channel-group pf stats core (atom nil)))))
