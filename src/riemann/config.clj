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
        [riemann.streams :exclude [update-index delete-from-index]])
  (:gen-class))

(def ^{:doc "A default core."} core (core/core))

(def graphite #'graphite-client/graphite)

(defn repl-server
  "Adds a new REPL server with opts to the default core."
  [& opts]
  (riemann.repl/start-server (apply hash-map opts)))

(defn tcp-server
  "Add a new TCP server with opts to the default core."
  [& opts]
  (swap! (core :servers) conj
         (tcp/tcp-server core (apply hash-map opts))))

(defn graphite-server
  "Add a new Graphite TCP server with opts to the default core."
  [& opts]
  (dosync
   (swap! (core :servers) conj
          (graphite/graphite-server core (apply hash-map opts)))))

(defn udp-server
  "Add a new UDP server with opts to the default core."
  [& opts]
  (swap! (core :servers) conj
         (udp/udp-server core (apply hash-map opts))))

(defn ws-server
  "Add a new websockets server with opts to the default core."
  [& opts]
  (swap!
    (core :servers) conj
    (websockets/ws-server core (apply hash-map opts))))

(defn streams
  "Add any number of streams to the default core."
  [& things]
  (swap! (core :streams) concat things))

(defn index
  "Set the index used by this core."
  [& opts]
  (reset! (core :index) (apply riemann.index/index opts)))

(defn update-index
  "Updates the given index with all events received. Also publishes to the
  index pubsub channel."
  [index]
  (fn [event] (core/update-index core event)))

(defn delete-from-index
  "Deletes any events that pass through from the index"
  [index]
  (fn [event] (core/delete-from-index core event)))

(defn periodically-expire
  "Sets up a reaper for this core. See core API docs."
  ([interval]
   (core/periodically-expire core interval))
  ([]
   (periodically-expire 10)))

(defn pubsub
  "Returns this core's pubsub registry."
  []
  (:pubsub core))

(defn publish
  "Returns a stream which publishes events to this the given channel. Uses this
  core's pubsub registry."
  [channel]
  (fn [event]
    (pubsub/publish (:pubsub core) channel event)))

(defn subscribe
  "Subscribes to the given channel with f, which will receive events. Uses this
  core's pubsub registry."
  [channel f]
  (pubsub/subscribe (:pubsub core) channel f))

(defn start
  "Start the core."
  []
  (core/start core))

(defn stop 
  "Stop the core."
  []
  (core/stop core))

(defn reset
  "Reset the core."
  []
  (def core (core/core)))

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
