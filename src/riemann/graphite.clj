(ns riemann.graphite
  "Forwards events to Graphite."
  (:refer-clojure :exclude [replace])
  (:import
   (java.net Socket)
   (java.io Writer
            OutputStreamWriter)
   (org.jboss.netty.channel Channels)
   (org.jboss.netty.handler.codec.frame DelimiterBasedFrameDecoder
                                        Delimiters)
   (org.jboss.netty.handler.codec.oneone OneToOneDecoder)
   (org.jboss.netty.handler.codec.string StringDecoder StringEncoder)
   (org.jboss.netty.util CharsetUtil))
  (:use [clojure.string :only [split join replace]]
        clojure.tools.logging
        riemann.pool
        riemann.common))


(defn graphite-path-basic
  "Constructs a path for an event. Takes the hostname fqdn, reversed,
  followed by the service, with spaces converted to dots."
  [event]
  (let [service (:service event)
        host (:host event)
        split-service (if service (split service #" ") [])
        split-host (if host (split host #"\.") [])]
     (join "." (concat (reverse split-host) split-service))))

(defn graphite-path-percentiles
  "Like graphite-service-basic, but also converts trailing decimals like 0.95
  to 95."
  [event]
  (graphite-path-basic
    (if-let [service (:service event)]
      (assoc event :service
             (replace service
                      #"(\d+\.\d+)$"
                      (fn [[_ x]] (str (int (* 100 (read-string x)))))))
      event)))

(defn graphite
  "Returns a function which accepts an event and sends it to Graphite.
  Silently drops events when graphite is down. Attempts to reconnect
  automatically every five seconds. Use:

  (graphite {:host \"graphite.local\" :port 2003})

  Options:

  :path       A function which, given an event, returns the string describing
              the path of that event in graphite. graphite-path-percentiles by
              default.

  :pool-size  The number of connections to keep open.

  :reconnect-interval   How many seconds to wait between attempts to connect.
                        Default 5.

  :claim-timeout        How many seconds to wait for a graphite connection from
                        the pool. Default 0.1.

  :block-start          Wait for the pool's initial connections to open
                        before returning."
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 2003
                     :path graphite-path-percentiles} opts)
        pool (fixed-pool
               (fn open []
                 (info "Connecting to " (select-keys opts [:host :port]))
                 (let [sock (Socket. (:host opts) (:port opts))
                       out  (OutputStreamWriter. (.getOutputStream sock))]
                   (info "Connected")
                   [sock out]))
               (fn close [[sock out]]
                 (info "Closing connection to "
                       (select-keys opts [:host :port]))
                 (.close out)
                 (.close sock))
               {:size                 (:pool-size opts)
                :block-start          (:block-start opts)
                :regenerate-interval  (:reconnect-interval opts)})
        path (:path opts)]

    (fn [event]
      (when (:metric event)
        (with-pool [[sock out] pool (:claim-timeout opts)]
                   (let [string (str (join " " [(path event)
                                                (float (:metric event))
                                                (int (:time event))])
                                     "\n")]
                     (.write ^OutputStreamWriter out string)
                     (.flush ^OutputStreamWriter out)))))))


