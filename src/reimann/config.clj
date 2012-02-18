(ns reimann.config
  "Reimann config files are eval'd in the context of this namespace. Includes
  streams, client, email, logging, and graphite; the common functions used in
  config. Provides a default core and functions (tcp-server, streams, index)
  which modify that core."
  (:require [reimann.core])
  (:require [reimann.server])
  (:require reimann.index)
  (:require [reimann.logging :as logging])
  (:require [reimann.folds :as folds])
  (:use clojure.tools.logging)
  (:use reimann.client)
  (:use reimann.streams)
  (:use reimann.email)
  (:use reimann.graphite)
  (:gen-class))

(def ^{:doc "A default core."} core (reimann.core/core))

(defn tcp-server 
  "Add a new TCP server with opts to the default core."
  [& opts]
  (dosync
    (alter (core :servers) conj
      (reimann.server/tcp-server core (apply hash-map opts)))))

(defn streams
  "Add any number of streams to the default core." 
  [& things]
  (dosync
    (alter (core :streams) concat things)))

(defn index 
  "Set the index used by this core."
  [& opts]
  (dosync
    (ref-set (core :index) (apply reimann.index/index opts))))

(defn periodically-expire
  "Sets up a reaper for this core. See core API docs."
  [interval]
  (reimann.core/periodically-expire core interval))

; Start the core
(defn start []
  (reimann.core/start core))

(defn include 
  "Include another config file.

  (include \"foo.clj\")"
  [file]
  (let [file (or file (first *command-line-args*) "reimann.config")]
    (binding [*ns* (find-ns 'reimann.config)]
      (load-string (slurp file)))))
