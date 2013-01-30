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
    (com.aphyr.riemann Proto$Msg)
    (org.jboss.netty.channel ChannelPipelineFactory ChannelPipeline)
    (org.jboss.netty.buffer ChannelBufferInputStream)
    (org.jboss.netty.handler.codec.oneone OneToOneDecoder
                                          OneToOneEncoder)
    (org.jboss.netty.handler.codec.protobuf ProtobufDecoder
                                            ProtobufEncoder)
    (org.jboss.netty.handler.execution ExecutionHandler
                                       OrderedMemoryAwareThreadPoolExecutor)))

(defprotocol Transport
  "A riemann transport is a way of emitting and receiving events
   over the wire."
  (setup [this opts]
    "Setup step for transports. In order to handle server life-cycle
     correctly, can be called several times.")
  (capabilities [this]
    "Return a collection of keywords representing what the transport
     can handle, possible values are: :queries and :events")
  (start [this]
    "Start listening for events and ")
  (stop [this]
    "Gracefully stop the server"))

(def shared-execution-handler
  "ExecutionHandler instance shared among all channel pipeline factories."
  (ExecutionHandler. (OrderedMemoryAwareThreadPoolExecutor. 16 1048576 1048576))) ; Maaagic values!

(defn channel-pipeline-factory
  "Return a factory for ChannelPipelines given a wire protocol-specific
  pipeline factory and a network protocol-specific handler."
  [pipeline-factory handler]
  (reify ChannelPipelineFactory
    (getPipeline [this]
      (doto ^ChannelPipeline (pipeline-factory)
        (.addLast "executor" shared-execution-handler) 
        (.addLast "handler" handler)))))

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
