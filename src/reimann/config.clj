(ns reimann.config
  (:require [reimann.core])
  (:require [reimann.server])
  (:require reimann.index)
  (:use reimann.client)
  (:use reimann.streams)
  (:use reimann.email)
  (:use reimann.graphite)
  (:gen-class))

; A stateful DSL for expressing reimann configuration.
(def core (reimann.core/core))

; Add a TCP server
(defn tcp-server [& opts]
  (dosync
    (alter (core :servers) conj
      (reimann.server/tcp-server core (apply hash-map opts)))))

; Add streams
(defn streams [& things]
  (dosync
    (alter (core :streams) concat things)))

; Create an index
(defn index [& opts]
  (dosync
    (ref-set (core :index) (apply reimann.index/index opts))))

; Start the core
(defn start []
  (reimann.core/start core))

; Eval the config file in this context
(defn include [file]
  (let [file (or file (first *command-line-args*) "reimann.config")]
    (binding [*ns* (find-ns 'reimann.config)]
      (load-string (slurp file)))))
