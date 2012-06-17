(ns leiningen.package
  (:use [clojure.contrib.shell-out]
        [leiningen.ubertar :only [ubertar]]
        [leiningen.jar :only [jar]]
        [leiningen.deb :only [deb]]
        [leiningen.pom :only [pom]]
        [clojure.java.io :only [file]]
        [org.satta.glob :only [glob]]
        ))

(defn package [project]
  ; Clojars pom
  (jar project)
  (pom project)

  ; Tarball
  (ubertar project)

  ; Delete existing debs
  (doseq [f (glob (str (:root project) "/*.deb"))]
    (.delete f))

  ; Build deb
  (deb project)
  
  ; Rename deb
  (let [f (first (glob (str (:root project) "/*.deb")))]
    (.renameTo f (file (:root project) 
                  (str (:name project) "_" (:version project) ".deb")))))
