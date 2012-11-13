(ns leiningen.fatdeb
  (:use [clojure.java.shell :only [sh]]
        [clojure.java.io :only [file delete-file writer copy]]
        [clojure.string :only [join capitalize trim-newline]]
        [leiningen.uberjar :only [uberjar]]))

(defn delete-file-recursively
    "Delete file f. If it's a directory, recursively delete all its contents.
    Raise an exception if any deletion fails unless silently is true."
    [f & [silently]]
    (System/gc) ; This sometimes helps release files for deletion on windows.
    (let [f (file f)]
          (if (.isDirectory f)
                  (doseq [child (.listFiles f)]
                            (delete-file-recursively child silently)))
          (delete-file f silently)))

(defn deb-dir
  "Debian package working directory."
  [project]
  (file (:root project) "target/deb/riemann"))

(defn cleanup
  [project]
  ; Delete working dir.
  (when (.exists (deb-dir project))
    (delete-file-recursively (deb-dir project))))

(defn reset
  [project]
  (cleanup project)
  (sh "rm" (str (:root project) "/target/*.deb")))

(defn control
  "Control file"
  [project]
  (join "\n"
        (map (fn [[k v]] (str (capitalize (name k)) ": " v))
             {:package (:name project)
              :version (:version project)
              :section "base"
              :priority "optional"
              :architecture "all"
              :depends "bash"
              :maintainer (:email (:maintainer project))
              :description (:description project)})))

(defn write
  "Write string to file, plus newline"
  [file string]
  (with-open [w (writer file)]
    (.write w (str (trim-newline string) "\n"))))

(defn make-deb-dir
  "Creates the debian package structure in a new directory."
  [project]
  (let [dir (deb-dir project)]
    (.mkdirs dir)

    ; Meta
    (.mkdirs (file dir "DEBIAN"))
    (write (file dir "DEBIAN" "control") (control project))
    (write (file dir "DEBIAN" "conffiles") 
           (join "\n" ["/etc/riemann/riemann.config"]))

    ; Jar
    (.mkdirs (file dir "usr" "lib" "riemann"))
    (copy (file (:root project) "target" 
                (str "riemann-" (:version project) "-standalone.jar"))
          (file dir "usr" "lib" "riemann" "riemann.jar"))

    ; Binary
    (.mkdirs (file dir "usr" "bin"))
    (copy (file (:root project) "pkg" "deb" "riemann")
          (file dir "usr" "bin" "riemann"))
    (.setExecutable (file dir "usr" "bin" "riemann") true false)

    ; Config
    (.mkdirs (file dir "etc" "riemann"))
    (copy (file (:root project) "pkg" "riemann.config")
          (file dir "etc" "riemann" "riemann.config"))

    dir))

(defn dpkg
  "Convert given package directory to a .deb."
  [project deb-dir]
  (print (:err (sh "dpkg" "--build" 
                   (str deb-dir) 
                   (str (file (:root project) "target")))))
  (let [deb-file (file (:root project) "target" (str (:name project) "_"
                                                     (:version project) "_"
                                                     "all" ".deb"))]
    (write (str deb-file ".md5")
           (:out (sh "md5sum" (str deb-file))))))
                                       

(defn fatdeb
  ([project] (fatdeb project true))
  ([project uberjar?]
   (reset project)
   (when uberjar? (uberjar project))
   (dpkg project (make-deb-dir project))
   (cleanup project)))
