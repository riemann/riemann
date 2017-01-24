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
    (java.util List)
    (java.util.concurrent TimeUnit
                          Executors)
    (io.riemann.riemann Proto$Msg)
    (io.netty.channel ChannelInitializer
                      Channel
                      ChannelPipeline
                      ChannelHandler)
    (io.netty.channel.group ChannelGroup
                            DefaultChannelGroup)
    (io.netty.channel.socket DatagramPacket)
    (io.netty.buffer ByteBufInputStream)
    (io.netty.handler.codec MessageToMessageDecoder
                            MessageToMessageEncoder)
    (io.netty.handler.codec.protobuf ProtobufDecoder
                                     ProtobufEncoder)
    (io.netty.util ReferenceCounted)
    (io.netty.util.concurrent Future
                              EventExecutorGroup
                              DefaultEventExecutorGroup
                              ImmediateEventExecutor)
    (java.net InetAddress
              UnknownHostException)))

(def ioutil-lock
  "There's a bug in JDK 6, 7, and 8 which can cause a deadlock initializing
  sse-server and netty concurrently; we serialize them with this lock.
  https://github.com/riemann/riemann/issues/617"
  (Object.))

(defn ^DefaultChannelGroup channel-group
  "Make a channel group with a given name."
  [name]
  (DefaultChannelGroup. name (ImmediateEventExecutor/INSTANCE)))

(defn derefable
  "A simple wrapper for a netty future which on deref just calls
  (syncUninterruptibly f), and returns the future's result."
  [^Future f]
  (reify clojure.lang.IDeref
    (deref [_]
      (.syncUninterruptibly f)
      (.get f))))

(defn ^Future shutdown-event-executor-group
  "Gracefully shut down an event executor group. Returns a derefable future."
  [^EventExecutorGroup g]
  ; 10ms quiet period, 10s timeout.
  (derefable (.shutdownGracefully g 10 1000 TimeUnit/MILLISECONDS)))

(defn retain
  "Retain a ReferenceCounted object, if x is such an object. Otherwise, noop.
  Returns x."
  [x]
  (when (instance? ReferenceCounted x)
    (.retain ^ReferenceCounted x))
  x)

(defmacro channel-initializer
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
        pipeline-name (vary-meta (gensym "pipeline")
                                 assoc :tag `ChannelPipeline)
        forms (map (fn [[h-name h-expr]]
                     `(.addLast ~pipeline-name
                                ~(when-let [e (:executor (meta h-name))]
                                   e)
                                ~(str h-name)
                                ~(if (:shared (meta h-name))
                                   h-name
                                   h-expr)))
                   handlers)]
;    (prn forms)
    `(let [~@(apply concat shared)]
       (proxy [ChannelInitializer] []
         (initChannel [~'ch]
           (let [~pipeline-name (.pipeline ^Channel ~'ch)]
             ~@forms
             ~pipeline-name))))))

(defn protobuf-decoder
  "Decodes protobufs to Msg objects"
  []
  (ProtobufDecoder. (Proto$Msg/getDefaultInstance)))

(defn protobuf-encoder
  "Encodes protobufs to Msg objects"
  []
  (ProtobufEncoder.))

(defn datagram->byte-buf-decoder
  "A decoder that turns DatagramPackets into ByteBufs."
  []
  (proxy [MessageToMessageDecoder] []
    (decode [context ^DatagramPacket message ^List out]
      (.add out (retain (.content message))))

    (isSharable [] true)))

(defn msg-decoder
  "Netty decoder for Msg protobuf objects -> maps"
  []
  (proxy [MessageToMessageDecoder] []
    (decode [context message ^List out]
      (.add out (decode-msg message)))
    (isSharable [] true)))

(defn msg-encoder
  "Netty encoder for maps -> Msg protobuf objects"
  []
  (proxy [MessageToMessageEncoder] []
    (encode [context message ^List out]
      (.add out (encode-pb-msg message)))
    (isSharable [] true)))

(defn event-executor
  "Creates a new netty execution handler for processing events. Defaults to 1
  thread per core."
  []
  (DefaultEventExecutorGroup. (.. Runtime getRuntime availableProcessors)))

(defonce ^DefaultEventExecutorGroup shared-event-executor
  (event-executor))

(defonce instrumentation
  (let [^DefaultEventExecutorGroup executor shared-event-executor
        svc "riemann netty event-executor "]
    (reify Instrumented
      (events [this]
        (let [base {:state "ok"
                    :tags  ["riemann"]
                    :time  (unix-time)}
              queue-size (reduce + (map #(.pendingTasks %)
                                        (iterator-seq (.iterator executor))))]
                   (map (partial merge base)
                        [{:service (str svc "threads active")
                          :metric (.. executor executorCount)}
                         {:service (str svc "queue size")
                          :metric queue-size}]))))))

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

(defn resolve-host
  "Resolves a hostname to a random IP"
  [host]
  (try
    (.getHostAddress (rand-nth (InetAddress/getAllByName host)))
    (catch UnknownHostException e
      host)))
