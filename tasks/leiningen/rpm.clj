(ns leiningen.rpm
  (:use [leiningen.uberjar :only [uberjar]]
        [leiningen.fatrpm :only [fatrpm]]))

(defn rpm [project]
  (doto project
    (uberjar)
    (fatrpm false)))