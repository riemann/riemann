(ns riemann.bin
  "Main function."
  (:require riemann.config
            riemann.logging
            riemann.time)
  (:use clojure.tools.logging)
  (:gen-class))

(def config-file
  "The configuration file loaded by the bin tool" 
  (atom nil))

(defn reload!
  "Reloads the given configuration file by clearing the task scheduler, shutting
  down the current core, and loading a new one."
  []
  (try
    (riemann.config/validate-config @config-file)
    (riemann.time/reset-tasks!)
    (riemann.config/clear!)
    (riemann.config/include @config-file)
    (riemann.config/apply!)
    :reloaded
    (catch Exception e
      (error e "Couldn't reload:")
      e)))

(defn handle-signals
  "Sets up POSIX signal handlers."
  []
  (sun.misc.Signal/handle
    (sun.misc.Signal. "HUP")
    (proxy [sun.misc.SignalHandler] []
      (handle [sig]
              (info "Caught SIGHUP, reloading")
              (reload!)))))
(defn pid
  "Process identifier, such as it is on the JVM. :-/"
  []
  (-> (java.lang.management.ManagementFactory/getRuntimeMXBean) (.getName)))

(defn -main
  "Start Riemann. Loads a configuration file from the first of its args."
  [& argv]
  (riemann.logging/init)
  (try
    (info "PID" (pid))
    (reset! config-file (or (first argv) "riemann.config"))
    (handle-signals)
    (riemann.time/start!)
    (riemann.config/include @config-file)
    (riemann.config/apply!)
    nil
    (catch Exception e
      (error e "Couldn't start"))))
