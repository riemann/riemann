(ns leiningen.build)

(defn build [project & args]
  "Build everything! Yay!"
  (leiningen.compile/compile project)
  (leiningen.protobuf/compile project)
  (leiningen.javac/javac project))
