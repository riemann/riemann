(ns riemann.transport.graphite
  (:import [org.jboss.netty.util CharsetUtil]
           [org.jboss.netty.handler.codec.oneone OneToOneDecoder]
           [org.jboss.netty.handler.codec.string StringDecoder StringEncoder]
           [org.jboss.netty.handler.codec.frame
            DelimiterBasedFrameDecoder
            Delimiters])
  (:use [riemann.transport.tcp :only [tcp-server
                                      gen-tcp-handler]]
        [riemann.transport :only [channel-pipeline-factory
                                  channel-group
                                  shared-execution-handler]]
        [clojure.string :only [split]]))

(defn decode-graphite-line
  "Decode a line coming from graphite.
  Graphite uses a simple scheme where each metric is given as a CRLF delimited
  line, space split with three items:

  * The metric name
  * The metric value (optionally NaN)
  * The timestamp

  By default, decode-graphite-line will yield a simple metric with just
  a service metric and timestamp, a parser-fn can be given to it, which
  will yield a map to merge onto the result. This can be used when
  graphite metrics have known patterns that you wish to extract more
  information (host, refined service name, tags) from"
  [line parser-fn]
  (when-let [[service metric timestamp] (split line #" ")]
    (when (not= metric "nan") ;; discard nan values
      (try
        (let [res {:service service
                   :metric (Float. metric)
                   :time (Long. timestamp)}]
          (if parser-fn (merge res (parser-fn res)) res))
        (catch Exception e {:ok :true :service "exception"})))))

(defn graphite-frame-decoder
  "A closure which yields a graphite frame-decoder. Taking an argument
   which will be given to decode-graphite-line (hence the closure)"
  [parser-fn]
  (fn []
    (proxy [OneToOneDecoder] []
      (decode [context channel message]
        (decode-graphite-line message parser-fn)))))

(defn graphite-handler
  "Given a core and a MessageEvent, applies the message to core."
  [core e]
  (doseq [stream (:streams core)]
    (stream (.getMessage e))))

(defn graphite-server
  "Start a graphite-server. Options:

  :host       \"127.0.0.1\"
  :port       2003
  :parser-fn  an optional function given to decode-graphite-line"
  ([] (graphite-server {}))
  ([opts]
     (let [core (get opts :core (atom nil))
           host (get opts :host "127.0.0.1")
           port (get opts :port 2003)
           channel-group (channel-group (str "graphite server " host ":" port))
           pipeline-factory (channel-pipeline-factory
                              frame-decoder  (DelimiterBasedFrameDecoder. 
                                               1024
                                               (Delimiters/lineDelimiter))
                              ^:shared string-decoder (StringDecoder. 
                                                        CharsetUtil/UTF_8)
                              ^:shared string-encoder (StringEncoder. 
                                                        CharsetUtil/UTF_8)
                              ^:shared executor shared-execution-handler
                              ^:shared graphite-decoder (graphite-frame-decoder
                                                          (:parser-fn opts))
                              ^:shared handler (gen-tcp-handler core
                                                       channel-group
                                                       graphite-handler))]

       (tcp-server (merge opts
                          {:host host
                           :port port
                           :core core
                           :channel-group channel-group
                           :pipeline-factory pipeline-factory})))))
