(ns riemann.core
  "Binds together an index, servers, and streams."
  (:use [riemann.time :only [unix-time]]
        clojure.tools.logging)
  (:require riemann.streams
            [riemann.index :as index]
            [riemann.pubsub :as ps]))

(defn core
  "Create a new core."
  []
  {:servers (atom [])
   :streams (atom [])
   :index   (atom nil)
   :reaper  (atom nil)
   :pubsub  (ps/pubsub-registry)})

(defn periodically-expire
  "Every interval (default 10) seconds, expire states from this core's index
  and stream them to streams. The streamed states have only the host and
  service copied, current time, and state expired. Expired events from the
  index are also published to the \"index\" pubsub channel."
  [core interval]
  (let [interval (* 1000 (or interval 10))]
    (future (loop []
              (Thread/sleep interval)
              (let [i       (deref (:index core))
                    streams (deref (:streams core))]
                (when i
                  (doseq [state (index/expire i)]
                    (let [e {:host (:host state)
                             :service (:service state)
                             :state "expired"
                             :time (unix-time)}]
                      (ps/publish (:pubsub core) "index" e)
                      (doseq [stream streams]
                        (stream e))))))
              (recur)))))

(defn update-index
  "Updates this core's index with an event. Also publishes to the index pubsub
  channel."
  [core event]
  (when (index/update (deref (:index core)) event)
    (ps/publish (:pubsub core) "index" event)))

(defn delete-from-index
  "Updates this core's index with an event."
  [core event]
  (index/delete (deref (:index core)) event))

(defn start
  "Start the given core. Starts reapers."
  [core]
  (swap! (:reaper core)
         (fn [current-reaper]
           (or current-reaper (periodically-expire core 60))))
  (info "Hyperspace core online"))

(defn stop
  "Stops the given core. Cancels reapers, stops servers."
  [core]
  (info "Core stopping")
  ; Stop reaper
  (swap! (:reaper core)
         (fn [reaper]
           (when reaper
             (future-cancel reaper)
             nil)))

  ; Stop each server
  (doseq [server (deref (core :servers))]
    (server))
  (info "Hyperspace core shut down"))
