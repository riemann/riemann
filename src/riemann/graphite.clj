(ns riemann.graphite
  "Forwards events to Graphite."
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [split join replace]]
            [riemann.transport :refer [resolve-host]]
            [riemann.pool :refer [fixed-pool with-pool]]
            [clojure.tools.logging :refer [info]]
            #_[riemann.common :refer [client]])
  (:import [java.net Socket DatagramSocket DatagramPacket InetAddress]
           [java.io OutputStreamWriter])
  #_(:use 
        clojure.tools.logging
        riemann.common
        ))

(defprotocol GraphiteClient
  (open [client]
    "Creates a Graphite client")
  (send-line [client line]
    "Sends a formatted line to Graphite")
  (send-lines [client lines]
    "Sends a list of formatted lines to Graphite")
  (close [client]
    "Cleans up (closes sockets etc.)"))

(defrecord GraphiteTCPClient [^String host ^int port]
  GraphiteClient
  (open [this]
    (let [sock (Socket. host port)]
      (assoc this
             :socket sock
             :out (OutputStreamWriter. (.getOutputStream sock)))))
  (send-line [this line]
    (let [out (:out this)]
      (.write ^OutputStreamWriter out ^String line)
      (.flush ^OutputStreamWriter out)))
  (send-lines [this lines]
    (let [out (:out this)]
      (doseq [line lines]
        (.write ^OutputStreamWriter out ^String line))
      (.flush ^OutputStreamWriter out)))
  (close [this]
    (.close ^OutputStreamWriter (:out this))
    (.close ^Socket (:socket this))))

(defrecord GraphiteUDPClient [^String host ^int port]
  GraphiteClient
  (open [this]
    (assoc this
           :socket (DatagramSocket.)
           :host host
           :port port))
  (send-line [this line]
    (let [bytes (.getBytes ^String line)
          length (count line)
          addr (InetAddress/getByName (:host this))
          datagram (DatagramPacket. bytes length ^InetAddress addr port)]
      (.send ^DatagramSocket (:socket this) datagram)))
  (send-lines [this lines]
    (doseq [line lines]
      (send-line this line)))
  (close [this]
    (.close ^DatagramSocket (:socket this))))

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
             ; hack hack hack hack
             (replace service
                      #"(\d+)\.(\d+)$"
                      (fn [[_ whole frac]] (str (when-not (= "0" whole))
                                                frac))))
      event)))

(defn graphite-path-tags
  "Returns a function which constructs a path for an event.
  Takes the service with spaces converted to dots, followed by the tags
  referenced in `tags` if they exists in the event.

  Example:

  (def graph (graphite {:path (graphite-path-tags [:host :rack])}))

  {:host \"foo\" :service \"api req\" :rack \"n1\"}

  will have this path: api.req;host=foo;rack=n1"
  [tags]
  (fn [event]
    (let [service (replace (:service event) #" " ".")
          tags (filter #(get event %) tags)
          tag-string (join ";" (map #(str (name %) "=" (get event %)) tags))]
      (if (= tag-string "")
        service
        (str service ";" tag-string)))))

(defn graphite-metric
  "convert riemann metric value to graphite"
  [event]
  (let [val (:metric event)]
    (if (integer? val) val (double val))))

(defn graphite
  "Returns a function which accepts an event and sends it to Graphite.
  Silently drops events when graphite is down. Attempts to reconnect
  automatically every five seconds. Use:

  (graphite {:host \"graphite.local\" :port 2003})

  Options:

  - :path       A function which, given an event, returns the string describing
                the path of that event in graphite. graphite-path-percentiles by
                default.
  - :pool-size  The number of connections to keep open. Default 4.
  - :reconnect-interval   How many seconds to wait between attempts to connect.
                          Default 5.
  - :claim-timeout        How many seconds to wait for a graphite connection from
                          the pool. Default 0.1.
  - :block-start          Wait for the pool's initial connections to open
                          before returning.
  - :protocol             Protocol to use. Either :tcp (default) or :udp."
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 2003
                     :protocol :tcp
                     :claim-timeout 0.1
                     :pool-size 4
                     :path graphite-path-percentiles
                     :metric graphite-metric} opts)
        pool (fixed-pool
               (fn []
                 (info "Connecting to " (select-keys opts [:host :port]))
                 (let [host (resolve-host (:host opts))
                       port (:port opts)
                       client (open (condp = (:protocol opts)
                                      :tcp (GraphiteTCPClient. host port)
                                      :udp (GraphiteUDPClient. host port)))]
                   (info "Connected to" host)
                   client))
               (fn [client]
                 (info "Closing connection to "
                       (select-keys opts [:host :port]))
                 (close client))
               (-> opts
                   (select-keys [:block-start])
                   (assoc :size (:pool-size opts))
                   (assoc :regenerate-interval (:reconnect-interval opts))))
        path (:path opts)]
    (fn [events]
      (let [events (->> (if (sequential? events) events (list events))
                        (filter :metric))]
        (when-not (empty? events)
          (with-pool [client pool (:claim-timeout opts)]
            (let [lines (map (fn [event]
                               (str (join " " [(path event)
                                               (graphite-metric event)
                                               (int (:time event))])
                                    "\n"))
                             events)]
              (send-lines client lines))))))))
