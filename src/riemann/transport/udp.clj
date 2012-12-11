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
           [org.jboss.netty.channel.group ChannelGroup DefaultChannelGroup]
           [org.jboss.netty.channel.socket.nio NioDatagramChannelFactory])
  (:use [clojure.tools.logging :only [warn info]]
        [clojure.string        :only [split]]
        [riemann.transport     :only [handle protobuf-frame-decoder
                                      channel-pipeline-factory]]))

(defn udp-handler
  "Returns a UDP handler for the given core."
  [core ^ChannelGroup channel-group]
  (proxy [SimpleChannelUpstreamHandler] []
    (channelOpen [context ^ChannelStateEvent state-event]
                 (.add channel-group (.getChannel state-event)))

    (messageReceived [context ^MessageEvent message-event]
                     (handle core (.getMessage message-event)))
    (exceptionCaught [context ^ExceptionEvent exception-event]
      (warn (.getCause exception-event) "UDP handler caught"))))

(defn udp-server
  "Starts a new UDP server for a core. Starts immediately.

  IMPORTANT: The UDP server has a maximum datagram size--by default, 16384
  bytes. If your client does not agree on the maximum datagram size (and send
  big messages over TCP instead), it can send large messages which will be
  dropped with protobuf parse errors in the log.

  Options:
  :host   The address to listen on (default 127.0.0.1).
  :port   The port to listen on (default 5555).
  :max-size   The maximum datagram size (default 16384 bytes)."
  ([core] (udp-server core {}))
  ([core opts]
     (let [pipeline-factory #(doto (Channels/pipeline)
                               (.addLast "protobuf-decoder"
                                         (protobuf-frame-decoder)))
           opts (merge {:host "127.0.0.1"
                      :port 5555
                      :max-size 16384
                      :pipeline-factory pipeline-factory}
                     opts)
         bootstrap (ConnectionlessBootstrap.
                     (NioDatagramChannelFactory.
                       (Executors/newCachedThreadPool)))
         all-channels (DefaultChannelGroup. (str "udp-server " opts))
         cpf (channel-pipeline-factory
              (:pipeline-factory opts) (udp-handler core all-channels))]

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
     (info "UDP server" (select-keys opts [:host :port :max-size]) "online")

     ; fn to close server
     (fn []
       (-> all-channels .close .awaitUninterruptibly)
       (.releaseExternalResources bootstrap)
       (info "UDP server" (select-keys opts [:host :port :max-size]) 
             "shut down")))))