(ns riemann.config
  "Riemann config files are eval'd in the context of this namespace. Includes
  streams, client, email, logging, and graphite; the common functions used in
  config. Provides a default core and functions ((tcp|udp)-server, streams,
  index) which modify that core."
  (:require [riemann.core :as core]
            [riemann.transport.tcp        :as tcp]
            [riemann.transport.udp        :as udp]
            [riemann.transport.websockets :as websockets]
            [riemann.transport.graphite   :as graphite]
            [riemann.repl]
            [riemann.index]
            [riemann.logging :as logging]
            [riemann.folds :as folds]
            [riemann.pubsub :as pubsub]
            [riemann.graphite :as graphite-client]
            [clojure.tools.nrepl.server :as repl])
  (:use clojure.tools.logging
        riemann.client
        riemann.email
        [riemann.pagerduty :only [pagerduty]]
        [riemann.librato :only [librato-metrics]]
        [riemann.streams])
  (:gen-class))

(def core "The currently running core."
  (atom (core/core)))
(def next-core "The core which will replace the current core."
  (atom (core/core)))

(def graphite #'graphite-client/graphite)

(defn repl-server
  "Starts a new REPL server with opts."
  [& opts]
  (riemann.repl/start-server (apply hash-map opts)))

(defn add-service!
  "Adds a service to the next core."
  [service]
  (locking core
    (swap! next-core assoc :services
           (conj (:services @next-core) service))))

(defn tcp-server
  "Add a new TCP server with opts to the default core."
  [& opts]
  (add-service! (tcp/tcp-server (apply hash-map opts))))

(defn graphite-server
  "Add a new Graphite TCP server with opts to the default core."
  [& opts]
  (add-service! (graphite/graphite-server (apply hash-map opts))))

(defn udp-server
  "Add a new UDP server with opts to the default core."
  [& opts]
  (add-service! (udp/udp-server (apply hash-map opts))))

(defn ws-server
  "Add a new websockets server with opts to the default core."
  [& opts]
  (add-service! (websockets/ws-server (apply hash-map opts))))

(defn streams
  "Add any number of streams to the default core."
  [& things]
  (locking core
    (swap! next-core assoc :streams
           (concat (:streams @next-core) things))))

(defn index
  "Set the index used by this core. Returns the index."
  [& opts]
  (let [index (apply riemann.index/index opts)]
    (locking core 
      (swap! next-core assoc :index index))
    index))

(defn update-index
  "Updates the given index with all events received. Also publishes to the
  index pubsub channel."
  [index]
  (fn update [event] (core/update-index @core event)))

(defn delete-from-index
  "Deletes any events that pass through from the index. By default, deletes
  events with the same host and service. If a field, or a list of fields, is
  given, deletes any events with matching values for all of those fields.

  ; Delete all events in the index with the same host
  (delete-from-index :host event)
  
  ; Delete all events in the index with the same host and state.
  (delete-from-index [:host :state] event)"
  ([]
   (fn delete [event] (core/delete-from-index @core event)))
  ([fields]
   (fn delete [event] (core/delete-from-index @core fields event))))

(defn periodically-expire
  "Sets up a reaper for this core. See core API docs."
  ([]
   (periodically-expire 10))
  ([interval]
   (add-service! (core/reaper interval))))

(defn publish
  "Returns a stream which publishes events to the given channel. Uses this
  core's pubsub registry."
  [channel]
  (fn [event]
    (pubsub/publish (:pubsub @core) channel event)))

(defn subscribe
  "Subscribes to the given channel with f, which will receive events. Uses this
  core's pubsub registry."
  [channel f]
  (pubsub/subscribe (:pubsub @next-core) channel f))

(defn clear!
  "Resets the next core."
  []
  (locking core
    (reset! next-core (core/core))))

(defn apply!
  "Applies pending changes to the core. Transitions the current core to the
  next one, and resets the next core."
  []
  (locking core
    (swap! core core/transition! @next-core)
    (clear!)))

(defn start!
  "Start the current core."
  []
  (core/start! @core))

(defn stop!
  "Stop the current core."
  []
  (core/stop! @core))

(defn read-strings
  "Returns a sequence of forms read from string."
  ([string]
   (read-strings []
                 (-> string (java.io.StringReader.)
                   (clojure.lang.LineNumberingPushbackReader.))))
  ([forms reader]
   (let [form (clojure.lang.LispReader/read reader false ::EOF false)]
     (if (= ::EOF form)
       forms
       (recur (conj forms form) reader)))))

(defn validate-config
  "Check that a config file has valid syntax."
  [file]
  (try
    (read-strings (slurp file))
    (catch clojure.lang.LispReader$ReaderException e
      (throw (logging/nice-syntax-error e file)))))

(defn include
  "Include another config file.

  (include \"foo.clj\")"
  [file]
  (binding [*ns* (find-ns 'riemann.config)]
    (validate-config file)
    (load-string (slurp file))))
