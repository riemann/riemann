(ns reimann.bin
  (:require reimann.config)
  (:gen-class))

(defn -main [& argv]
  (reimann.config/include (first argv)))
