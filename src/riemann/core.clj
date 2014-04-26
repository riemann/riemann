(ns riemann.core
  "Binds together an index, servers, and streams."
  (:use [riemann.time :only [unix-time]]
        [riemann.common :only [deprecated localhost event]]
        clojure.tools.logging
        [riemann.instrumentation :only [Instrumented]])
  (:require riemann.streams
            [riemann.service :as service :refer [Service ServiceEquiv]]
            [riemann.index :as index :refer [Index]]
            [riemann.pubsub :as ps]
            [riemann.instrumentation :as instrumentation]
            clojure.set))

(defn stream!
  "Applies an event to the streams in this core."
  [core event]
  (instrumentation/measure-latency (:streaming-metric core)
    (doseq [stream (:streams core)]
      (stream event))))

(defn core-services
  "All services in a core--both the :services list and named services like the
  index."
  [core]
  (remove nil?
          (concat [(:index core)
                   (:pubsub core)]
                (:services core))))

(defn instrumentation-service
  "Returns a service which samples instrumented services in its core every
  interval seconds, and sends their events to the core itself."
  [opts]
  (let [interval (long (* 1000 (get opts :interval 10)))
        enabled? (get opts :enabled? true)]
    (service/thread-service
      ::instrumentation [interval enabled?]
      (fn measure [core]
        (Thread/sleep interval)

        (try
          ; Take events from core and instrumented services
          (let [base (event {:host (localhost)
                             ; Default TTL of 2 intervals, and convert ms to s.
                             :ttl  (long (/ interval 500))})
                events (mapcat instrumentation/events
                            (concat
                              [core
                               ; lol circular deps
                               (deref (find-var 'riemann.transport/instrumentation))]
                              (filter instrumentation/instrumented?
                                      (core-services core))))]

            (if enabled?
              ; Stream each event through this core
              (doseq [event events]
                (stream! core (merge base event)))
              ; Ensure we consume all events, to avoid overflowing stats
              (dorun events)))

          (catch Exception e
            (warn e "instrumentation service caught")))))))

(defrecord Core
  [streams services index pubsub streaming-metric]

  Instrumented
  (events [this]
          (instrumentation/events streaming-metric)))

(defn core
  "Create a new core."
  []
  (Core. []
         [(instrumentation-service {})]
         nil
         (ps/pubsub-registry)
         (instrumentation/rate+latency {:service "streams"
                                        :tags ["riemann"]})))

(defn conj-service
  "Adds a service to a core. Throws if any existing services would conflict. If
  force? is passed, dissoc's any conflicting services."
  ([core service] (conj-service core service false))
  ([core service force?]
   (if force?
     ; Remove conflicts and conj service
     (assoc core :services
            (conj (remove #(service/conflict? service %)
                          (:services core))
                  service))

     ; Throw if conflicts arise
     (let [conflicts (filter #(service/conflict? service %)
                             (core-services core))]
       (when-not (empty? conflicts)
         (throw (IllegalArgumentException.
                  (binding [*print-level* 3]
                    (str "won't conj service: " (pr-str service)
                         " would conflict with " (pr-str conflicts))))))
       (update-in core [:services] conj service)))))

(defn merge-cores
  "Merge cores old-core and new-core into a new core comprised of services from
  :new-core or their equivalents from :old-core where possible."
  [old-core new-core]
  (let [merged-services (map (fn [svc]
                               (or (first (filter #(service/equiv? % svc)
                                                  (:services old-core)))
                                   svc))
                             (:services new-core))
        merged (-> new-core
                 map->Core
                 (assoc :streaming-metric (or (:streaming-metric new-core)
                                              (:streaming-metric old-core)))
                 (assoc :index (when (:index new-core)
                                 (if (service/equiv? (:index new-core)
                                                     (:index old-core))
                                   (:index old-core)
                                   (:index new-core))))
                 (assoc :pubsub (when (:pubsub new-core)
                                  (if (service/equiv? (:pubsub new-core)
                                                      (:pubsub old-core))
                                    (:pubsub old-core)
                                    (:pubsub new-core))))
                 (assoc :services merged-services))]
    merged))

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
  (let [merged (merge-cores old-core new-core)
        old-services (set (core-services old-core))
        merged-services (set (core-services merged))]

    ; Stop old services
    (dorun (pmap service/stop!
                 (clojure.set/difference old-services merged-services)))

    ; Reload merged services
    (dorun (pmap #(service/reload! % merged) merged-services))

    ; Start merged services
    (dorun (pmap service/start! merged-services))

    (info "Hyperspace core online")
    merged))

(defn start!
  "Start the given core. Reloads and starts all services."
  [core]
  (let [services (core-services core)]
    (dorun (pmap #(service/reload! % core) services))
    (dorun (pmap service/start!            services)))
  (info "Hyperspace core online"))

(defn stop!
  "Stops the given core and all services."
  [core]
  (info "Core stopping")
  (dorun (pmap service/stop! (core-services core)))
  (info "Hyperspace core shut down"))

(defn update-index
  "Updates this core's index with an event."
  [core event]
  (deprecated "update-index is redundant; wrap-index provides pubsub
              integration now."
              ((:index core) event)))

(defn wrap-index
  "Yield a wrapper to an index, exposing the same protocols as well
   as IFn which will index an event. If a second argument is present
   it should implement the PubSub interface and will be notified
   when events are updated in the index."
  ([source]
     (wrap-index source nil))
  ([source registry]
     (reify
       Object
       (equals [this other]
         (= source other))
       Index
       (clear [this]
         (index/clear source))
       (delete [this event]
         (index/delete source event))
       (delete-exactly [this event]
         (index/delete-exactly source event))
       (expire [this]
         (index/expire source))
       (search [this query-ast]
         (index/search source query-ast))
       (update [this event]
         (when-not (:time event)
           (throw (ex-info "cannot index event with no time"
                           {:event event})))
         (index/update source event)
         (when registry
           (ps/publish! registry "index" event)))
       (lookup [this host service]
         (index/lookup source host service))

       clojure.lang.Seqable
       (seq [this]
         (seq source))

       ServiceEquiv
       (equiv? [this other]
         (service/equiv? source other))

       Service
       (conflict? [this other]
         (service/conflict? source other))
       (reload! [this new-core]
         (service/reload! source new-core))
       (start! [this]
         (service/start! source))
       (stop! [this]
         (service/stop! source))

       clojure.lang.IFn
       (invoke [this event]
         (index/update this event)))))


(defn delete-from-index
  "Deletes similar events from the index. By default, deletes events with the
  same host and service. If a field, or a list of fields, is given, deletes any
  events with matching values for all of those fields.

  ; Delete all events in the index with the same host
  (delete-from-index index :host event)

  ; Delete all events in the index with the same host and state.
  (delete-from-index index [:host :state] event)"
  ([core event]
   (index/delete (:index core) event))
  ([core fields event]
   (let [match-fn (if (coll? fields) (apply juxt fields) fields)
         match (match-fn event)
         index (:index core)]
       (doseq [event (filter #(= match (match-fn %)) index)]
         (index/delete-exactly index event)))))

(defn reaper
  "Returns a service which expires states from its core's index every interval
  (default 10) seconds. Expired events are streamed to the core's streams. The
  streamed states have only the host and service copied, current time, and
  state expired. Expired events from the index are also published to the
  \"index\" pubsub channel.

  Options:

  :keep-keys A list of event keys which should be preserved from the indexed
             event in the expired event. Defaults to [:host :service], which
             means that when an event expires, its :host and :service are
             copied to a new event, but no other keys are preserved.

             The state of an expired event is always \"expired\", and its time
             is always the time that the event expired."
  ([] (reaper 10))
  ([interval] (reaper interval {}))
  ([interval opts]
   (let [interval  (* 1000 (or interval 10))
         keep-keys (get opts :keep-keys [:host :service])]
     (service/thread-service
       ::reaper [interval keep-keys]
       (fn worker [core]
         (Thread/sleep interval)

         (when-let [i (:index core)]
           (doseq [state (index/expire i)]
             (try
               (let [e (-> (select-keys state keep-keys)
                         (merge {:state "expired"
                                 :time (unix-time)}))]
                 (when-let [registry (:pubsub core)]
                   (ps/publish! registry "index" e))
                 (stream! core e))
               (catch Exception e
                 (warn e "Caught exception while processing expired events"))))))))))
