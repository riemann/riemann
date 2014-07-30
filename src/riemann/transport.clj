(ns riemann.transport
  "Functions used in several transports. Some netty parts transpire
  here since netty is the preferred method of providing transports"
  (:use [slingshot.slingshot :only [try+]]
        [riemann.core        :only [stream!]]
        [riemann.common      :only [decode-msg]]
        [riemann.codec       :only [encode-pb-msg]]
        [riemann.index       :only [search]]
        [riemann.instrumentation :only [Instrumented]]
        [riemann.time         :only [unix-time]]
        clojure.tools.logging)
  (:require [riemann.query       :as query])
  (:import
    (java.util.concurrent TimeUnit
                          Executors)
    (com.aphyr.riemann Proto$Msg)
    (io.netty.channel ChannelInitializer Channel ChannelHandler)
    (io.netty.channel.group ChannelGroup DefaultChannelGroup)
    (io.netty.buffer ByteBufInputStream)
    (io.netty.handler.codec MessageToMessageDecoder
                            MessageToMessageEncoder)
    (io.netty.handler.codec.protobuf ProtobufDecoder
                                            ProtobufEncoder)
    (io.netty.util.concurrent DefaultEventExecutorGroup ImmediateEventExecutor)))

(defn channel-group
  "Make a channel group with a given name."
  [name]
  (DefaultChannelGroup. name (ImmediateEventExecutor/INSTANCE)))

(defmacro channel-pipeline-factory
  "Constructs an instance of a Netty ChannelInitializer from a list of
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
                     `(.addLast ~(when-let [e (:executor (meta h-name))]
                                   e)
                                ~(str h-name)
                                ~(if (:shared (meta h-name))
                                   h-name
                                   h-expr)))
                   handlers)]
    `(let [~@(apply concat shared)]
       (proxy [ChannelInitializer] []
         (initChannel [~'ch]
           (doto (.pipeline ~'ch)
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
  (proxy [MessageToMessageDecoder] []
    (decode [context message out]
      (.add out (decode-msg message)))
    (isSharable [] true)))

(defn msg-encoder
  "Netty encoder for maps -> Msg protobuf objects"
  []
  (proxy [MessageToMessageEncoder] []
    (encode [context message out]
      (.add out (encode-pb-msg message)))
    (isSharable [] true)))

(defn event-executor
  "Creates a new netty execution handler."
  []
  (DefaultEventExecutorGroup. 100))

(defonce ^DefaultEventExecutorGroup shared-event-executor
  (event-executor))

(defonce instrumentation
  (let [^DefaultEventExecutorGroup executor shared-event-executor
        svc "riemann netty event-executor "]

    (reify Instrumented
      (events [this]
        (comment (let [base {:state "ok" :time (unix-time)}]
                   (map (partial merge base)
                        [{:service (str svc "queue size")
                          :metric  (.. executor getQueue size)}
                         {:service (str svc "threads active")
                          :metric (.. executor getActiveCount)}])))
        []
        ))))

(defn handle
  "Handles a msg with the given core."
  [core msg]
  (try+
    ;; Send each event/state to each stream
    (doseq [event (:states msg)] (stream! core event))
    (doseq [event (:events msg)] (stream! core event))

    (if (:query msg)
      ;; Handle query
      (let [ast (query/ast (:string (:query msg)))]
        (if-let [i (:index core)]
          {:ok true :events (search i ast)}
          {:ok false :error "no index"}))

      ; Otherwise just return an ack
      {:ok true})

    ;; Some kind of error happened
    (catch [:type :riemann.query/parse-error] {:keys [message]}
      {:ok false :error (str "parse error: " message)})
    (catch Exception ^Exception e
      {:ok false :error (.getMessage e)})))
