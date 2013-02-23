(ns riemann.transport
  "Functions used in several transports. Some netty parts transpire
  here since netty is the preferred method of providing transports"
  (:use [slingshot.slingshot :only [try+]]
        [riemann.common      :only [decode-msg]]
        [riemann.codec       :only [encode-pb-msg]]
        [riemann.index       :only [search]]
        clojure.tools.logging)
  (:require [riemann.query       :as query])
  (:import
    (java.util.concurrent TimeUnit
                          Executors)
    (com.aphyr.riemann Proto$Msg)
    (org.jboss.netty.channel ChannelPipelineFactory ChannelPipeline)
    (org.jboss.netty.channel.group ChannelGroup DefaultChannelGroup)
    (org.jboss.netty.buffer ChannelBufferInputStream)
    (org.jboss.netty.util DefaultObjectSizeEstimator)
    (org.jboss.netty.handler.codec.oneone OneToOneDecoder
                                          OneToOneEncoder)
    (org.jboss.netty.handler.codec.protobuf ProtobufDecoder
                                            ProtobufEncoder)
    (org.jboss.netty.handler.execution ExecutionHandler
                                       OrderedMemoryAwareThreadPoolExecutor)))

(defn channel-group
  "Make a channel group with a given name."
  [name]
  (DefaultChannelGroup. name))

(defmacro channel-pipeline-factory
  "Constructs an instance of a Netty ChannelPipelineFactory from a list of
  names and expressions which return handlers. Handlers with :shared metadata
  on their names are bound once and re-used in every invocation of
  getPipeline(), other handlers will be evaluated each time.

  (channel-pipeline-factory
             frame-decoder    (make-an-int32-frame-decoder)
    ^:shared protobuf-decoder (ProtobufDecoder. (Proto$Msg/getDefaultInstance))
    ^:shared msg-decoder      msg-decoder)"
  [& names-and-exprs]
  (assert (even? (count names-and-exprs)))
  (let [handlers (partition 2 names-and-exprs)
        shared (filter (comp :shared meta first) handlers)
        forms (map (fn [[h-name h-expr]]
                        `(.addLast ~(str h-name)
                                   ~(if (:shared (meta h-name))
                                     h-name
                                     h-expr)))
                   handlers)]
    `(let [~@(apply concat shared)]
       (reify ChannelPipelineFactory
         (getPipeline [this]
                      (doto (org.jboss.netty.channel.Channels/pipeline)
                        ~@forms))))))

(defn protobuf-decoder
  "Decodes protobufs to Msg objects"
  []
  (ProtobufDecoder. (Proto$Msg/getDefaultInstance)))

(defn protobuf-encoder
  "Encodes protobufs to Msg objects"
  []
  (ProtobufEncoder.))

(defn msg-decoder
  "Netty decoder for Msg protobuf objects -> maps"
  []
  (proxy [OneToOneDecoder] []
    (decode [context channel message]
            (decode-msg message))))

(defn msg-encoder
  "Netty encoder for maps -> Msg protobuf objects"
  []
  (proxy [OneToOneEncoder] []
    (encode [context channel message]
            (encode-pb-msg message))))

(defn execution-handler
  "Creates a new netty execution handler."
  []
  (ExecutionHandler.
    (OrderedMemoryAwareThreadPoolExecutor.
      16       ; Core pool size
      1048576  ; 1MB per channel queued
      10485760 ; 10MB total queued
      )))

(defonce shared-execution-handler
  (execution-handler))

(defn handle
  "Handles a msg with the given core."
  [core msg]
  (try+
   ;; Send each event/state to each stream
   (doseq [event  (concat (:events msg) (:states msg))
           stream (:streams core)]
     (stream event))

   (if (:query msg)
     ;; Handle query
     (let [ast (query/ast (:string (:query msg)))]
       (if-let [i (:index core)]
         {:ok true :events (search i ast)}
         {:ok false :error "no index"}))

      {:ok true})

   ;; Some kind of error happened
    (catch [:type :riemann.query/parse-error] {:keys [message]}
      {:ok false :error (str "parse error: " message)})
    (catch Exception ^Exception e
           {:ok false :error (.getMessage e)})))
