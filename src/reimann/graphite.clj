(ns reimann.graphite
  "Forwards events to Graphite."
  (:import [java.net Socket])
  (:import [java.io Writer])
  (:import [java.io OutputStreamWriter])
  (:use [clojure.string :only [split join]])
  (:use clojure.tools.logging)
  (:use reimann.common))

(defn graphite 
  "Returns a function which accepts an event and sends it to Graphite.
  Constructs service names by taking the host (reversed, e.g. \"web.pdx\" ->
  \"lax.web\"), and service (with spaces converted to dots). Silently eats
  events when graphite is down. Attempts to reconnect automatically every five
  seconds. Use:
  
  (graphite {:host \"graphite.local\" :port 2003})"
  [opts]
  (let [opts (merge {:host "localhost" :port 2003} opts)
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
        
        ; Transform events into names.
        ; :host "foo.com"
        ; :service "thing 1"
        ; -> "com.foo.thing.1"
        name (fn [event]
               (let [service (:service event)
                     host (:host event)
                     split-service (if service (split #" " service) [])
                     split-host (if host (split #"\." host) [])]
                  (join "." (concat (reverse split-host) split-service))))
        ]

    ; Try to connect immediately
    (try
      (open)
      (catch Exception e
        (warn e (str "Couldn't send to graphite " opts))))

    (fn [event]
      (when (:metric event)
        (let [string (str (join " " [(name event) 
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
