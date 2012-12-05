(ns riemann.bin
  "Main function."
  (:require riemann.config
            riemann.time)
  (:use clojure.tools.logging)
  (:gen-class))

(defn reload
  "Reloads the given configuration file by clearing the task scheduler, shutting
  down the current core, and loading a new one."
  [config-file]
  (try
    (riemann.time/reset-tasks!)
    (riemann.config/stop)
    (riemann.config/reset)
    (riemann.config/include config-file)
    (riemann.config/start)
    (catch Exception e
      (error "Couldn't reload" e))))

(defn handle-signals
  "Sets up POSIX signal handlers."
  [config-file]
  (sun.misc.Signal/handle
    (sun.misc.Signal. "HUP")
    (proxy [sun.misc.SignalHandler] []
      (handle [sig]
              (info "Caught SIGHUP, reloading")
              (reload config-file)))))

(defn -main
  "Start Riemann. Loads a configuration file from the first of its args."
  [& argv]
    (try
      (let [config-file (first argv)]
        (handle-signals config-file)
        (riemann.time/start!)
        (riemann.config/include config-file)
        (riemann.config/start))
      (catch Exception e
        (error "Couldn't start" e))))
