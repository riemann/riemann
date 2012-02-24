(ns riemann.core
  "Binds together an index, servers, and streams."
  (:use riemann.common)
  (:use clojure.tools.logging)
  (:require riemann.streams)
  (:require [riemann.index :as index]))

(defn core
  "Create a new core."
  []
  {:servers (ref [])
   :streams (ref [])
   :index   (ref nil)
   :reaper  (ref nil)})

(defn periodically-expire
  "Every interval (default 10) seconds, expire states from this core's index
  and stream them to streams. The streamed states have only the host and service
  copied, current time, and state expired."
  [core interval]
  (let [interval (* 1000 (or interval 10))]
    (future (loop []
              (Thread/sleep interval)
              (let [i       (deref (:index core))
                    streams (deref (:streams core))]
                (when i
                  (doseq [state (index/expire i)
                         stream streams]
                    (stream {:host (:host state)
                             :service (:service state)
                             :state "expired"
                             :time (unix-time)}))))
              (recur)))))

(defn start
  "Start the given core. Starts reapers."
  [core]
  (dosync
    (when-not (deref (:reaper core))
      (ref-set (:reaper core) (periodically-expire core 60))))
  (info "Hyperspace core online"))

(defn stop
  "Stops the given core. Cancels reapers, stops servers."
  [core]
  (info "Core stopping")
  ; Stop expiry
  (when-let [r (deref (:reaper core))]
    (future-cancel r))

  ; Stop each server
  (doseq [server (deref (core :servers))]
    (server)))
