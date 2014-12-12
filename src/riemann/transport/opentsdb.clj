(ns riemann.transport.opentsdb
  (:import [org.jboss.netty.util CharsetUtil]
           (org.jboss.netty.channel MessageEvent)
           [org.jboss.netty.handler.codec.oneone OneToOneDecoder]
           [org.jboss.netty.handler.codec.string StringDecoder StringEncoder]
           [org.jboss.netty.handler.codec.frame
            DelimiterBasedFrameDecoder
            Delimiters])
  (:require [interval-metrics.core :as metrics])
  (:use [riemann.core :only [stream!]]
        [riemann.codec :only [->Event]]
        [riemann.transport.tcp :only [tcp-server
                                      gen-tcp-handler]]
        [riemann.transport :only [channel-pipeline-factory
                                  channel-group
                                  shared-execution-handler]]
        [interval-metrics.measure :only [measure-latency]]
        [slingshot.slingshot :only [try+ throw+]]
        [clojure.string :only [split
                               join]]
        [clojure.tools.logging :only [warn]]))

(defn decode-opentsdb-line
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

    ; Parse numbers
    (let [metric (try (Double. metric)
                      (catch NumberFormatException e
                        (throw+ "invalid metric")))
          timestamp (try (Long. timestamp)
                         (catch NumberFormatException e
                           (throw+ "invalid timestamp")))
          description service
          service (if-let [tagstr (when-not (empty? tags) (join " " tags))]
                              (str service " " tagstr)
                              service)
          host (when-let [h (-> (filter (fn [tag] (.startsWith tag "host=")) tags)
                                first)]
                         (subs h 5))
          ]

      ; Construct event
      ; (defrecord Event [host service state description metric tags time ttl])
      (->Event host
               service
               nil
               description
               metric
               tags
               timestamp
               nil))))

(defn opentsdb-frame-decoder
  [parser-fn]
  (let [parser-fn (or parser-fn identity)]
    (proxy [OneToOneDecoder] []
      (decode [context channel message]
        (try+
          (if (= "version" message)
            (do
              (. channel
                (write
                  (str "net.opentsdb\n"
                       "Built on ... (riemann-opentsdb)\n"
                       )))
              {:service "version" :time (System/nanoTime)})
            (-> message
                decode-opentsdb-line
                parser-fn))
          (catch Object e
            (throw (RuntimeException.
                     (str "OpenTSDB server parse error (" e "): "
                          (pr-str message))))))))))

(defn opentsdb-handler
  "Given a core and a MessageEvent, applies the message to core."
  [core stats ^MessageEvent e]
  (stream! core (.getMessage e)))

(defn opentsdb-server
  "Start a opentsdb-server. Options:

  :host       \"127.0.0.1\"
  :port       4242
  :parser-fn  an optional function given to decode-opentsdb-line"
  ([] (opentsdb-server {}))
  ([opts]
     (let [core  (get opts :core (atom nil))
           host  (get opts :host "127.0.0.1")
           port  (get opts :port 4242)
           stats (metrics/rate+latency)
           server tcp-server
           channel-group (channel-group (str "opentsdb server " host ":" port))
           opentsdb-message-handler (gen-tcp-handler
                                        core stats channel-group opentsdb-handler)
           pipeline-factory (channel-pipeline-factory
                              frame-decoder  (DelimiterBasedFrameDecoder.
                                               1024
                                               (Delimiters/lineDelimiter))
                              ^:shared string-decoder (StringDecoder.
                                                        CharsetUtil/UTF_8)
                              ^:shared string-encoder (StringEncoder.
                                                        CharsetUtil/UTF_8)
                              ^:shared executor shared-execution-handler
                              ^:shared opentsdb-decoder (opentsdb-frame-decoder
                                                          (:parser-fn opts))
                              ^:shared handler opentsdb-message-handler)]
       (server (merge opts
                          {:host host
                           :port port
                           :core core
                           :channel-group channel-group
                           :pipeline-factory pipeline-factory})))))
