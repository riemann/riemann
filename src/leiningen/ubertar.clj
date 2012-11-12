(ns leiningen.ubertar
  (:use [leiningen.uberjar :only [uberjar]]
        [clojure.java.io :only [copy file]]
        [clojure.contrib.shell-out])
  (:import [org.apache.tools.tar TarOutputStream TarEntry]
           [java.io File FileOutputStream ByteArrayOutputStream]))

(defn entry-name [release-name f]
  (let [prefix (str (System/getProperty "user.dir") File/separator "(pkg)?"
                    File/separator "?")
        prefix (.replaceAll prefix "\\\\" "\\\\\\\\") ; WINDERS!!!!
        stripped (.replaceAll (.getAbsolutePath f) prefix "")]
    (str release-name File/separator stripped)))

(defn- add-file [release-name tar f]
  (when-not (.isDirectory f)
    (let [entry (doto (TarEntry. f)
                  (.setName (entry-name release-name f)))
          baos (ByteArrayOutputStream.)]
      (when (.canExecute f)
        ;; No way to expose unix perms? you've got to be kidding me, java!
        (.setMode entry 0755))
      (copy f baos)
      (.putNextEntry tar entry)
      (.write tar (.toByteArray baos))
      (.closeEntry tar))))

(defn- git-commit
  "Reads the value of HEAD and returns a commit SHA1."
  [git-dir]
  (when (.exists git-dir)
    (let [head (.trim (slurp (str (file git-dir "HEAD"))))]
      {:git-commit (if-let [ref-path (second (re-find #"ref: (\S+)" head))]
                     (.trim (slurp (str (file git-dir ref-path))))
                     head)})))

(defn build-info [project]
  (if-let [build-info (:build-info project)]
    build-info
    (let [hudson (when (System/getenv "BUILD_ID")
                   {:build-id (System/getenv "BUILD_ID")
                    :build-tag (System/getenv "BUILD_TAG")})
          git (git-commit (file (:root project) ".git"))]
      (merge hudson git))))

(defn- add-build-info [project]
  (let [pkg (file (:root project) "pkg")
        _ (.mkdir pkg)
        build-file (file pkg "build.clj")
        build-info (build-info project)]
    (when build-info
      (.deleteOnExit build-file)
      (spit build-file (str build-info "\n")))))

(defn ubertar [project]
  (add-build-info project)
  (let [release-name (str (:name project) "-" (:version project))
        jar-file (uberjar project)
        tar-file (file (:root project) (format "%s.tar" release-name))
        bz2-file (file (:root project) (format "%s.tar.bz2" release-name))]
    (.delete tar-file)
    (with-open [tar (TarOutputStream. (FileOutputStream. tar-file))]
      (doseq [p (file-seq (file (:root project) "pkg"))]
        (add-file release-name tar p))
;      (doseq [j (filter #(re-find #"\.jar$" (.getName %))
;                        (.listFiles (file (:library-path project))))]
;        (add-file release-name tar j))
;      (add-file (str release-name File/separator "lib") tar (file jar-file)))
      (add-file (str release-name File/separator
                     "lib") tar (file jar-file)))

    (.delete bz2-file)
    (sh "bzip2" (str tar-file))
    (println "Wrote" (.getName bz2-file))
    ))
