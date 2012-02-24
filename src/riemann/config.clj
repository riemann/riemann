(ns riemann.config
  "Riemann config files are eval'd in the context of this namespace. Includes
  streams, client, email, logging, and graphite; the common functions used in
  config. Provides a default core and functions ((tcp|udp)-server, streams,
  index) which modify that core."
  (:require [riemann.core])
  (:require [riemann.server])
  (:require riemann.index)
  (:require [riemann.logging :as logging])
  (:require [riemann.folds :as folds])
  (:use clojure.tools.logging)
  (:use riemann.client)
  (:use riemann.streams)
  (:use riemann.email)
  (:use riemann.graphite)
  (:gen-class))

(def ^{:doc "A default core."} core (riemann.core/core))

(defn tcp-server 
  "Add a new TCP server with opts to the default core."
  [& opts]
  (dosync
    (alter (core :servers) conj
      (riemann.server/tcp-server core (apply hash-map opts)))))

(defn udp-server 
  "Add a new UDP server with opts to the default core."
  [& opts]
  (dosync
    (alter (core :servers) conj
      (riemann.server/udp-server core (apply hash-map opts)))))

(defn streams
  "Add any number of streams to the default core." 
  [& things]
  (dosync
    (alter (core :streams) concat things)))

(defn index 
  "Set the index used by this core."
  [& opts]
  (dosync
    (ref-set (core :index) (apply riemann.index/index opts))))

(defn periodically-expire
  "Sets up a reaper for this core. See core API docs."
  ([interval]
    (riemann.core/periodically-expire core interval))
  ([]
   (periodically-expire 10)))

; Start the core
(defn start []
  (riemann.core/start core))

(defn include 
  "Include another config file.

  (include \"foo.clj\")"
  [file]
  (let [file (or file (first *command-line-args*) "riemann.config")]
    (binding [*ns* (find-ns 'riemann.config)]
      (load-string (slurp file)))))
