(ns riemann.bin
  "Main function."
  (:require riemann.config
            riemann.logging
            riemann.time
            riemann.pubsub)
  (:use clojure.tools.logging
        [clojure.tools.cli :only [cli]]
        [riemann.streams :only [testing-mode]])
  (:gen-class :name riemann.bin))

(def config-file
  "The configuration file loaded by the bin tool" 
  (atom nil))

(def reload-lock (Object.))

(defn reload!
  "Reloads the given configuration file by clearing the task scheduler, shutting
  down the current core, and loading a new one."
  []
  (locking reload-lock
    (try
      (riemann.config/validate-config @config-file)
      (riemann.time/reset-tasks!)
      (riemann.config/clear!)
      (riemann.pubsub/sweep! (:pubsub @riemann.config/core))
      (riemann.config/include @config-file)
      (riemann.config/apply!)
      :reloaded
      (catch Exception e
        (error e "Couldn't reload:")
        e))))

(defn handle-signals
  "Sets up POSIX signal handlers."
  []
  (if (not (.contains (. System getProperty "os.name") "Windows"))
    (sun.misc.Signal/handle
      (sun.misc.Signal. "HUP")
      (proxy [sun.misc.SignalHandler] []
        (handle [sig]
                (info "Caught SIGHUP, reloading")
                (reload!))))))
(defn pid
  "Process identifier, such as it is on the JVM. :-/"
  []
  (let [name (-> (java.lang.management.ManagementFactory/getRuntimeMXBean)
               (.getName))]
    (try
      (get (re-find #"^(\d+).*" name) 1)
      (catch Exception e name))))

(println "loading bin")(flush)
(defn -main
  "Start Riemann. Loads a configuration file from the first of its args."
  [& argv]
  (let [[options args banner] (cli argv
                                   ["-t" "--test" "Run Riemann in testing mode" :flag true :default false]
                                   ["-h" "--help" "Show help" :default false :flag true])]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (riemann.logging/init)
    (binding [testing-mode (:test options)]
      (try
        (info "PID" (pid))
        (reset! config-file (or (first args) "riemann.config"))
        (handle-signals)
        (riemann.time/start!)
        (riemann.config/include @config-file)
        (riemann.config/apply!)
        nil
        (catch Exception e
          (error e "Couldn't start"))))))
