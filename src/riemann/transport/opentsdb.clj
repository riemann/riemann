(ns riemann.transport.opentsdb
  (:import
    [io.netty.util CharsetUtil]
    [io.netty.handler.codec MessageToMessageDecoder]
    [io.netty.handler.codec.string StringDecoder
                                   StringEncoder]
    [io.netty.handler.codec DelimiterBasedFrameDecoder
                            Delimiters]
    [io.netty.channel ChannelHandlerContext])
  (:require [interval-metrics.core :as metrics])
  (:use [riemann.core :only [stream!]]
        [riemann.common :only [event]]
        [riemann.transport.tcp :only [tcp-server
                                      gen-tcp-handler]]
        [riemann.transport :only [channel-initializer
                                  channel-group
                                  shared-event-executor]]
        [interval-metrics.measure :only [measure-latency]]
        [slingshot.slingshot :only [try+ throw+]]
        [clojure.string :only [split join replace-first trimr]]
        [clojure.walk :only [keywordize-keys]]
        [clojure.tools.logging :only [warn]]))

(defn decode-opentsdb-line
  "Parse an OpenTSDB message string into an event."
  [line]
  (let [[verb service timestamp metric & tags] (split line #"\s")]
    ; Validate format
    (cond
          (= "version" verb)
          (throw+ "version request")

          (= "" verb)
          (throw+ "blank line")

          (not service)
          (throw+ "no metric name")

          (not timestamp)
          (throw+ "no timestamp")

          (not metric)
          (throw+ "no metric")

          (re-find #"(?i)nan" metric)
          (throw+ "NaN metric"))

    (let [metric (try (Double. metric)
                      (catch NumberFormatException e
                        (throw+ "invalid metric")))
          timestamp (try (Long. timestamp)
                         (catch NumberFormatException e
                           (throw+ "invalid timestamp")))
          description service
          service (if-let [tagstr (when-not (empty? tags) (join " " tags))]
                    (-> service
                        (str " " (replace-first tagstr #"\bhost=[^\s$]+\s?" ""))
                        trimr)
                    service)
          fields (->> tags
                      (map (fn [t]
                              (-> t
                                 (replace-first #"\bservice=" "servicetag=")
                                 (split #"="))))
                      flatten
                      (apply hash-map)
                      keywordize-keys)
          ]

      ; Construct event
      ; (defrecord Event [host service state description metric tags time ttl])
      (event (merge {:service service
                     :metric metric
                     :description description
                     :time timestamp}
                    fields)))))

(defn opentsdb-frame-decoder
  "A MessageToMessageDecoder that reads strings and emits either :version or
  events."
  [parser-fn]
  (let [parser-fn (or parser-fn identity)]
    (proxy [MessageToMessageDecoder] []
      (decode [context message out]
        (try+
          (.add out
                (if (= "version" message)
                  :version
                  (-> message
                      decode-opentsdb-line
                      parser-fn)))
          (catch Object e
            (throw (RuntimeException.
                     (str "OpenTSDB server parse error (" e "): "
                          (pr-str message)))))))

      (isSharable [] true))))

(defn opentsdb-handler
  "Messages to this handler are either :version or an event. Responds to
  version requests, and applies events to the core."
  [core stats ^ChannelHandlerContext ctx message]
  (if (= :version message)
    ; Respond with version
    (.writeAndFlush ctx "net.opentsdb\nBuilt on ... (riemann-opentsdb)\n")
    ; Stream event
    (stream! core message)))

(defn initializer
  "The pipeline of decoders, handlers, etc for parsing and handling messages."
  [parser-fn message-handler]
  (channel-initializer
    frame-decoder (DelimiterBasedFrameDecoder. 1024 (Delimiters/lineDelimiter))
    ^:shared string-decoder   (StringDecoder. CharsetUtil/UTF_8)
    ^:shared string-encoder   (StringEncoder. CharsetUtil/UTF_8)
    ^:shared opentsdb-decoder (opentsdb-frame-decoder parser-fn)
    ^{:shared   true
      :executor shared-event-executor} handler message-handler))

(defn opentsdb-server
  "Start a opentsdb-server. Options:

  :host       \"127.0.0.1\"
  :port       4242
  :parser-fn  an optional function which transforms events prior to streaming
              into the core."
  ([] (opentsdb-server {}))
  ([opts]
     (let [core  (get opts :core (atom nil))
           host  (get opts :host "127.0.0.1")
           port  (get opts :port 4242)
           stats (metrics/rate+latency)
           server tcp-server
           channel-group (channel-group (str "opentsdb server " host ":" port))
           handler (gen-tcp-handler core
                                    stats
                                    channel-group
                                    opentsdb-handler)
           initializer (initializer (:parser-fn opts) handler)]
       (server (merge opts
                      {:host host
                       :port port
                       :core core
                       :channel-group channel-group
                       :initializer initializer})))))
