(ns riemann.transport.udp
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:import [java.net InetSocketAddress]
           [java.util.concurrent Executors]
           [org.jboss.netty.bootstrap ConnectionlessBootstrap]
           [org.jboss.netty.channel ChannelStateEvent
                                    Channels
                                    ExceptionEvent
                                    FixedReceiveBufferSizePredictorFactory
                                    MessageEvent
                                    SimpleChannelUpstreamHandler]
           [org.jboss.netty.channel.group ChannelGroup]
           [org.jboss.netty.channel.socket.nio NioDatagramChannelFactory])
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
                                      shared-execution-handler
                                      channel-pipeline-factory]]))

(defn gen-udp-handler
  [core stats ^ChannelGroup channel-group handler]
  (proxy [SimpleChannelUpstreamHandler] []
    (channelOpen [context ^ChannelStateEvent state-event]
                 (.add channel-group (.getChannel state-event)))

    (messageReceived [context ^MessageEvent message-event]
                     (handler @core stats message-event))

    (exceptionCaught [context ^ExceptionEvent exception-event]
      (warn (.getCause exception-event) "UDP handler caught"))))

(defn udp-handler
  "Given a core and a MessageEvent, applies the message to core."
  [core stats ^MessageEvent message-event]
  (let [msg (.getMessage message-event)]
    (handle core msg)
    (metrics/update! stats (- (System/nanoTime) (:decode-time msg)))))

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
              (let [pool (Executors/newCachedThreadPool)
                    bootstrap (ConnectionlessBootstrap.
                                (NioDatagramChannelFactory. pool))]

                ; Configure bootstrap
                (doto bootstrap
                  (.setPipelineFactory pipeline-factory)
                  (.setOption "broadcast" "false")
                  (.setOption "receiveBufferSizePredictorFactory"
                              (FixedReceiveBufferSizePredictorFactory.
                                max-size)))

                ; Start bootstrap
                (->> (InetSocketAddress. host port)
                    (.bind bootstrap)
                    (.add channel-group))
                (info "UDP server" host port max-size "online")

                ; fn to close server
                (reset! killer
                        (fn []
                          (-> channel-group .close .awaitUninterruptibly)
                          (.releaseExternalResources bootstrap)
                          (.shutdown pool)
                          (info "UDP server" host port max-size "shut down")
                          ))))))

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
  :pipeline-factory A ChannelPipelineFactory"
  ([] (udp-server {}))
  ([opts]
   (let [core  (get opts :core (atom nil))
         stats (metrics/rate+latency)
         host  (get opts :host "127.0.0.1")
         port  (get opts :port 5555)
         max-size (get opts :max-size 16384)
         channel-group (get opts :channel-group
                            (channel-group
                              (str "udp-server" host ":" port 
                                   "(" max-size ")")))
         pf (get opts :pipeline-factory
                 (channel-pipeline-factory
                   ^:shared executor         shared-execution-handler
                   ^:shared protobuf-decoder (protobuf-decoder)
                   ^:shared protobuf-encoder (protobuf-encoder)
                   ^:shared msg-decoder      (msg-decoder)
                   ^:shared handler          (gen-udp-handler core stats channel-group udp-handler)))]
     (UDPServer. host port max-size channel-group pf stats core (atom nil)))))
