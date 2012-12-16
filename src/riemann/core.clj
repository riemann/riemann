(ns riemann.core
  "Binds together an index, servers, and streams."
  (:use [riemann.time :only [unix-time]]
        clojure.tools.logging)
  (:require riemann.streams
            [riemann.service :as service]
            [riemann.index :as index]
            [riemann.pubsub :as ps]))

(defrecord Core
  [streams services index pubsub])

(defn reaper
  "Returns a service which expires states from its core's index every interval
  (default 10) seconds. Expired events are streamed to the core's streams. The
  streamed states have only the host and service copied, current time, and
  state expired. Expired events from the index are also published to the
  \"index\" pubsub channel."
  [interval]
  (let [interval (* 1000 (or interval 10))]
    (service/thread-service
      :reaper interval
      (fn worker [core]
        (Thread/sleep interval)
        (let [i       (:index core)
              streams (:streams core)]
          (when i
            (doseq [state (index/expire i)]
              (let [e {:host (:host state)
                       :service (:service state)
                       :state "expired"
                       :time (unix-time)}]
                (when-let [registry (:pubsub core)]
                  (ps/publish registry "index" e))
                (doseq [stream streams]
                  (stream e))))))))))

(defn core
  "Create a new core."
  []
  (Core. [] [] nil (ps/pubsub-registry)))

(defn transition!
  "A core transition \"merges\" one core into another. Cores are immutable,
  but the stateful resources associated with them aren't. When you call
  (transition! old-core new-core), we:
  
  1. Stop old core services without an equivalent in the new core.

  2. Merge the new core's services with equivalents from the old core.

  3. Reload all services with the merged core.
  
  4. Start all services in the merged core.

  Finally, we return the merged core. old-core and new-core can be discarded."
  [old-core new-core]
  (let [merged-services (map (fn [svc]
                               (or (first (filter #(service/equiv? % svc)
                                                  (:services old-core)))
                                   svc))
                             (:services new-core))
        merged (assoc new-core :services merged-services)]

    ; Stop old services
    (dorun (pmap service/stop! 
                 (remove (set merged-services) (:services old-core))))


    ; Reload merged services
    (dorun (pmap #(service/reload! % merged) merged-services))

    ; Start merged services
    (dorun (pmap service/start! merged-services))
    merged))

(defn start!
  "Start the given core. Reloads and starts all services."
  [core]
  (dorun (pmap #(service/reload! % core) (:services core)))
  (dorun (pmap service/start!            (:services core)))
  (info "Hyperspace core online"))

(defn stop!
  "Stops the given core and all services."
  [core]
  (info "Core stopping")
  (dorun (pmap service/stop! (:services core)))
  (info "Hyperspace core shut down"))

(defn update-index
  "Updates this core's index with an event. Also publishes to the index pubsub
  channel."
  [core event]
  (when (index/update (:index core) event)
    (when-let [registry (:pubsub core)]
      (ps/publish registry "index" event))))

(defn delete-from-index
  "Updates this core's index with an event."
  [core event]
  (index/delete (:index core) event))
