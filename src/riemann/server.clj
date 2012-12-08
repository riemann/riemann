(ns riemann.server
  "Accepts messages from external sources. Associated with a core. Sends
  incoming events to the core's streams, queries the core's index for states."
  (:import (java.net InetSocketAddress)
           (java.util.concurrent Executors)
           (java.nio.channels ClosedChannelException)
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

  (:require [riemann.query :as query]
            [riemann.index :as index]
            gloss.io)
  (:use riemann.core
        riemann.common
        riemann.pubsub
        clojure.tools.logging
        clojure.stacktrace
        lamina.core
        aleph.http
        [slingshot.slingshot :only [try+]]
        [clj-http.util :only [url-decode]]
        [clojure.string :only [split]]))

(defn handle
  "Handles a msg with the given core."
  [core msg]
  (try+
    ; Send each event/state to each stream
    (doseq [event (concat (:events msg) (:states msg))
            stream (deref (:streams core))]
      (stream event))

    (if (:query msg)
      ; Handle query
      (let [ast (query/ast (:string (:query msg)))]
          (if-let [i (deref (:index core))]
            {:ok true :events (index/search i ast)}
            {:ok false :error "no index"}))

      ; Generic acknowledge
      {:ok true})

    ; Some kind of error happened
    (catch [:type :riemann.query/parse-error] {:keys [message]}
      {:ok false :error (str "parse error: " message)})
    (catch Exception ^Exception e
      {:ok false :error (.getMessage e)})))

(defn int32-frame-decoder
  []
  ; Offset 0, 4 byte header, skip those 4 bytes.
  (LengthFieldBasedFrameDecoder. Integer/MAX_VALUE, 0, 4, 0, 4))

(defn int32-frame-encoder
  []
  (LengthFieldPrepender. 4))

(defn protobuf-frame-decoder []
  (proxy [OneToOneDecoder] []
    (decode [context channel message]
      (let [instream (ChannelBufferInputStream. message)]
        (decode-inputstream instream)))))

(defn tcp-handler
  "Returns a TCP handler for the given core"
  [core ^ChannelGroup channel-group]
  (proxy [SimpleChannelHandler] []
    (channelOpen [context ^ChannelStateEvent state-event]
                 (.add channel-group (.getChannel state-event)))

    (messageReceived [^ChannelHandlerContext context
                      ^MessageEvent message-event]
      (let [channel (.getChannel message-event)
            msg (.getMessage message-event)]
        (try
          (let  [response (handle core msg)
                 encoded (encode response)]
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

(defn channel-pipeline-factory
  "Return a factory for ChannelPipelines given a wire protocol-specific
  pipeline factory and a network protocol-specific handler."
  [pipeline-factory handler]
  (proxy [ChannelPipelineFactory] []
    (getPipeline []
      (doto ^ChannelPipeline (pipeline-factory)
        (.addLast "executor" (ExecutionHandler.
                              (OrderedMemoryAwareThreadPoolExecutor.
                               16 1048576 1048576))) ; Maaagic values!
        (.addLast "handler" handler)))))

(defn tcp-server
  "Create a new TCP server for a core. Starts immediately. Options:
  :host   The host to listen on (default 127.0.0.1).
  :port   The port to listen on. (default 5555)"
  ([core] (tcp-server core {}))
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
     (info "TCP server" (select-keys opts [:host :port]) "online")

     ; fn to close server
     (fn []
       (-> all-channels .close .awaitUninterruptibly)
       (.releaseExternalResources bootstrap)
       (info "TCP server" (select-keys opts [:host :port]) "shut down")))))

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

(defn http-query-map
  "Converts a URL query string into a map."
  [string]
  (apply hash-map
         (map url-decode
           (mapcat (fn [kv] (split kv #"=" 2))
                     (split string #"&")))))

;;; Websockets
(defn ws-pubsub-handler [core ch hs]
  (let [topic (url-decode (last (split (:uri hs) #"/" 3)))
        params (http-query-map (:query-string hs))
        query (params "query")
        pred (query/fun (query/ast query))
        sub (subscribe (:pubsub core) topic
               (fn [event]
                 (when (pred event)
                   (enqueue ch (event-to-json event)))))]
    (info "New websocket subscription to" topic ":" query)
    (receive-all ch (fn [msg]
                      (when-not msg
                        ; Shut down channel
                        (info "Closing websocket "
                              (:remote-addr hs) topic query)
                        (close ch)
                        (unsubscribe (:pubsub core) sub))))))

(defn ws-index-handler
  "Queries the index for events and streams them to the client. If subscribe is
  true, also initiates a pubsub subscription to the index topic with that
  query."
  [core ch hs]
  (let [params (http-query-map (:query-string hs))
        query (params "query")
        ast (query/ast query)]
    (when-let [i (deref (:index core))]
      (doseq [event (index/search i ast)]
        (enqueue ch (event-to-json event))))
    (if (= (params "subscribe") "true")
      (ws-pubsub-handler core ch (assoc hs :uri "/pubsub/index"))
      (close ch))))

(defn ws-handler [core]
  (fn [ch handshake]
    (info "Websocket connection from" (:remote-addr handshake)
          (:uri handshake)
          (:query-string handshake))
    (condp re-matches (:uri handshake)
      #"^/index/?$" (ws-index-handler core ch handshake)
      #"^/pubsub/[^/]+/?$" (ws-pubsub-handler core ch handshake)
      :else (do
              (info "Unknown URI " (:uri handshake) ", closing")
              (close ch)))))

(defn ws-server
  "Starts a new websocket server for a core. Starts immediately.

  Options:
  :host   The address to listen on (default 127.0.0.1)
  :post   The port to listen on (default 5556)"
  ([core] (udp-server core {}))
  ([core opts]
   (let [opts (merge {:host "127.0.0.1"
                      :port 5556}
                     opts)
         s (start-http-server (ws-handler core) {:host (:host opts)
                                                 :port (:port opts)
                                                 :websocket true})]
     (info "Websockets server" opts "online")
     (fn []
       (s)
       (info "Websockets server" opts "shut down")))))
