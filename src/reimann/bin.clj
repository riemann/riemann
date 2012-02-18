(ns reimann.bin
  "Main function."
  (:require reimann.config)
  (:use clojure.tools.logging)
  (:gen-class))

(defn -main
  "Start Reimann. Loads a configuration file from the first of its args."
  [& argv]
    (try
      (reimann.config/include (first argv))
      (reimann.config/start)
      (catch Exception e
        (error e "Aborting"))))
