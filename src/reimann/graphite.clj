(ns reimann.graphite
  (:import [java.net Socket])
  (:import [java.io Writer])
  (:import [java.io OutputStreamWriter])
  (:use [clojure.contrib.string :only [split join]])
  (:use clojure.contrib.logging)
  (:use reimann.common))

(defn graphite [opts]
  "Returns a function which accepts an event and sends it to Graphite. Silently eats events when graphite is down."
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
        (log :warn (str "Couldn't send to graphite " opts) e)))

    (fn [event]
      (let [string (str (join " " [(name event) 
                                   (:metric_f event) 
                                   (int (:time event))])
                        "\n")]
        (locking sock
          (when (deref sock)
            (try
                (.write (deref out) string)
                (.flush (deref out))

              (catch Exception e
                (log :warn (str "Couldn't send to graphite " opts) e)
                (close)

                (future
                  ; Reconnect in 5 seconds
                  (Thread/sleep 5000)
                  (locking sock (open)))))))))))
