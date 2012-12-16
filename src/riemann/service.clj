(ns riemann.service
  "Lifecycle protocol for stateful services bound to a core.")

(defprotocol Service
  "Services are components of a core with a managed lifecycle. They're used for
  stateful things like connection pools, network servers, and background
  threads."
  (reload! [service core] 
          "Informs the service of a change in core.")
  (start! [service] 
          "Starts a service. Must be idempotent.")
  (stop!  [service] 
         "Stops a service. Must be idempotent.")
  (equiv? [service1 service2] 
          "Used to identify which services can remain running through a core
          transition, like reloading. If the old service is equivalent to the
          new service, the old service may be preserved and used by the new
          core. Otherwise, the old service may be shut down and replaced by
          the new."))

(defrecord ThreadService [name equiv-key f core running thread]
  Service
  (reload! [this new-core]
           (reset! core new-core))

  (equiv? [this other]
          (and
            (instance? ThreadService other)
            (= name (:name other))
            (= equiv-key (:equiv-key other))))

  (start! [this]
          (locking this
            (when-not @running
              (reset! running true)
              (reset! thread (Thread. (fn thread-service-runner []
                                        (while @running
                                          (f @core)))))
              (.start @thread))))

  (stop! [this]
         (locking this
           (when @running
             (reset! running false)
             ; Wait for exit
             (while (.isAlive @thread)
               (Thread/sleep 5))))))

(defn thread-service 
  "Returns a ThreadService which will call (f core) repeatedly when started.
  Will only stop between calls to f. Start and stop are blocking operations.
  Equivalent to other ThreadServices with the same name and equivalence key--
  if not provided, defaults nil."
  ([name f]
   (thread-service name nil f))
  ([name equiv-key f]
   (ThreadService. name equiv-key f (atom nil) (atom false) (atom nil))))
