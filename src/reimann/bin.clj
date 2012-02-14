(ns reimann.bin
  (:require reimann.config)
  (:use clojure.tools.logging)
  (:gen-class))

(defn -main [& argv]
  (try
    (reimann.config/include (first argv))
    (catch Exception e
      (error e "Aborting"))))
