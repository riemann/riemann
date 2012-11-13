(ns leiningen.pkg
  (:use [leiningen.uberjar :only [uberjar]]
        [leiningen.fatdeb :only [fatdeb]]
        [leiningen.tar :only [tar]]))

(defn pkg [project]
  (doto project
    (uberjar)
    (tar false)
    (fatdeb false)))
