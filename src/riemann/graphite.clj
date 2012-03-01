(ns riemann.graphite
  "Forwards events to Graphite."
  (:import [java.net Socket])
  (:import [java.io Writer])
  (:import [java.io OutputStreamWriter])
  (:use [clojure.string :only [split join replace]])
  (:use clojure.tools.logging)
  (:use riemann.common))

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
                      #"([\d\.]+)$"
                      (fn [[_ x]] (str (int (* 100 (read-string x)))))))
      event)))

(defn graphite 
  "Returns a function which accepts an event and sends it to Graphite.
  Silently eats events when graphite is down. Attempts to reconnect
  automatically every five seconds. Use:
  
  (graphite {:host \"graphite.local\" :port 2003})
  
  Set :path (fn [event] some-string) to change the path for each event. Uses
  graphite-path-percentiles by default."
  [opts]
  (let [opts (merge {:host "localhost" 
                     :port 2003
                     :path graphite-path-percentiles} opts)
        sock (ref nil)
        out  (ref nil)
        close (fn []
                ; Close conn
                (when (deref out) (.close (deref out)))
                (when (deref sock) (.close (deref sock)))
                (dosync
                  (ref-set sock nil)
                  (ref-set out nil)))
        open (fn []
               (info (str "Opening connection to " opts))
               (close)
               
                ; Open conn
                (dosync
                  (ref-set sock (Socket. (:host opts) (:port opts)))
                  (ref-set out (OutputStreamWriter.
                                 (.getOutputStream (deref sock))))))
        path (:path opts)]
        
    ; Try to connect immediately
    (try
      (open)
      (catch Exception e
        (warn e (str "Couldn't send to graphite " opts))))

    (fn [event]
      (when (:metric event)
        (let [string (str (join " " [(path event) 
                                     (float (:metric event))
                                     (int (:time event))])
                          "\n")]
          (locking sock
            (when (deref sock)
              (try
                  (.write (deref out) string)
                  (.flush (deref out))

                (catch Exception e
                  (warn e (str "Couldn't send to graphite " opts))
                  (close)

                  (future
                    ; Reconnect in 5 seconds
                    (Thread/sleep 5000)
                    (locking sock (open))))))))))))
