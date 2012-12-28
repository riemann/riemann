;(ns leiningen.build-parser
;  (:use [clojure.contrib.shell-out]
;        [clojure.java.io :only [copy file]]
;        ))
;
;(defn build-parser [project]
;  (prn (sh "java" "-cp" "lib/*" "org.antlr.Tool" "src/riemann/Query.g"
;      :dir (:root project)))
;~  (.delete (file (:root project) "Query.tokens")))
