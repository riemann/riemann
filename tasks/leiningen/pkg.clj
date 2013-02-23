(ns leiningen.pkg
  (:use [leiningen.uberjar :only [uberjar]]
        [leiningen.fatdeb :only [fatdeb]]
        [leiningen.fatrpm :only [fatrpm]]
        [leiningen.tar :only [tar]]))

(defn pkg [project]
  (doto project
    (uberjar)
    (tar false)
    (fatrpm false)
    (fatdeb false)))
