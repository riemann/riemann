(ns riemann.bin
  "Main function."
  (:require riemann.config
            riemann.time)
  (:use clojure.tools.logging)
  (:gen-class))

(defn -main
  "Start Riemann. Loads a configuration file from the first of its args."
  [& argv]
    (try
      (riemann.time/start!)
      (riemann.config/include (first argv))
      (riemann.config/start)
      (catch Exception e
        (error e "Aborting"))))
