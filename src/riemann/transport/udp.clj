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
        [riemann.service       :only [Service]]
        [riemann.transport     :only [handle 
                                      protobuf-decoder
                                      protobuf-encoder
                                      msg-decoder
                                      channel-pipeline-factory]]))

(defn udp-handler
  "Returns a UDP handler for the given atom to a core."
  [core ^ChannelGroup channel-group]
  (proxy [SimpleChannelUpstreamHandler] []
    (channelOpen [context ^ChannelStateEvent state-event]
                 (.add channel-group (.getChannel state-event)))

    (messageReceived [context ^MessageEvent message-event]
                     (handle @core (.getMessage message-event)))

    (exceptionCaught [context ^ExceptionEvent exception-event]
      (warn (.getCause exception-event) "UDP handler caught"))))

(defrecord UDPServer [host port max-size pipeline-factory core killer]
  ; core is an atom to a core
  ; killer is an atom to a function that shuts down the server
  
  Service
  ; TODO compare pipeline-factory!
  (equiv? [this other]
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
                                (NioDatagramChannelFactory. pool))
                    all-channels (DefaultChannelGroup. 
                                   (str "udp-server " host port max-size))
                    cpf (channel-pipeline-factory
                          pipeline-factory (udp-handler core all-channels))]

                ; Configure bootstrap
                (doto bootstrap
                  (.setPipelineFactory cpf)
                  (.setOption "broadcast" "false")
                  (.setOption "receiveBufferSizePredictorFactory"
                              (FixedReceiveBufferSizePredictorFactory. max-size)))

                ; Start bootstrap
                (let [server-channel (.bind bootstrap
                                            (InetSocketAddress. host port))]
                  (.add all-channels server-channel))
                (info "UDP server" host port max-size "online")

                ; fn to close server
                (reset! killer
                        (fn []
                          (-> all-channels .close .awaitUninterruptibly)
                          (.releaseExternalResources bootstrap)
                          (.shutdown pool)
                          (info "UDP server" host port max-size "shut down")
                          ))))))

  (stop! [this]
         (locking this
           (when @killer
             (@killer)
             (reset! killer nil)))))


(defn udp-server
  "Starts a new UDP server. Doesn't start until (service/start!).

  IMPORTANT: The UDP server has a maximum datagram size--by default, 16384
  bytes. If your client does not agree on the maximum datagram size (and send
  big messages over TCP instead), it can send large messages which will be
  dropped with protobuf parse errors in the log.

  Options:
  :host       The address to listen on (default 127.0.0.1).
  :port       The port to listen on (default 5555).
  :max-size   The maximum datagram size (default 16384 bytes).
  :pipeline-factory"
  ([] (udp-server {}))
  ([opts]
   (let [pipeline-factory #(doto (Channels/pipeline)
                             (.addLast "protobuf-encoder"
                                       (protobuf-encoder))
                             (.addLast "protobuf-decoder"
                                       (protobuf-decoder))
                             (.addLast "msg-decoder"
                                       (msg-decoder)))]
                                       
     (UDPServer.
       (get opts :host "127.0.0.1")
       (get opts :port 5555)
       (get opts :max-size 16384)
       (get opts :pipeline-factory pipeline-factory)
       (atom nil)
       (atom nil)))))
