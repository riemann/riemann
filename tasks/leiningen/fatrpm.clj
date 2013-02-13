(ns leiningen.fatrpm
  (:refer-clojure :exclude [replace])
  (:use [clojure.java.shell :only [sh]]
        [clojure.java.io :only [file delete-file writer copy]]
        [clojure.string :only [join capitalize trim-newline replace]]
        [leiningen.uberjar :only [uberjar]])
  (:import java.util.Date
           java.text.SimpleDateFormat)
  (:import [org.codehaus.mojo.rpm RPMMojo AbstractRPMMojo Mapping Source SoftlinkSource Scriptlet]
           [org.apache.maven.project MavenProject]
           [org.apache.maven.shared.filtering DefaultMavenFileFilter]
           [org.codehaus.plexus.logging.console ConsoleLogger]))

(def foo (prn "hi"))

(defn cleanup
  [project])

(defn reset
  [project]
  (cleanup project)
  (sh "rm" (str (:root project) "/target/*.rpm")))

(defn get-version
  [project]
  (let [df   (SimpleDateFormat. "yyyyMMdd-HHmmss")]
    (replace (:version project) #"SNAPSHOT" (.format df (Date.)))))

(defn set-mojo! 
  "Set a field on an AbstractRPMMojo object."
  [object name value]
  (let [field (.getDeclaredField AbstractRPMMojo name)]
    (.setAccessible field true)
    (.set field object value))
  object)

(defn array-list
  [list]
  (let [list (java.util.ArrayList.)]
    (doseq [item list] (.add list item))
    list))

(defn source
  "Create a source with a local location and a destination."
  ([] (Source.))
  ([location]
   (doto (Source.)
     (.setLocation (str location))))
  ([location destination]
    (doto (Source.)
      (.setLocation (str location))
      (.setDestination (str destination)))))

(defn mapping
  [m]
  (doto (Mapping.)
    (.setArtifact           (:artifact m))
    (.setConfiguration      (case (:configuration m)
                              true  "true"
                              false "false"
                              nil   "false"
                              (:configuration m))
    (.setDependency         (:dependency m))
    (.setDirectory          (:directory m))
    (.setDirectoryIncluded  (:directory-included? m))
    (.setDocumentation      (:documentation? m))
    (.setFilemode           (:filemode m))
    (.setGroupname          (:groupname m))
    (.setRecurseDirectories (:recurse-directories? m))
    (.setSources            (:sources m))
    (.setUsername           (:username m)))))

(defn mappings
  [project]
  (map (comp mapping 
             (partial merge {:username "riemann"
                             :groupname "riemann"})
       [; JAR
        {:directory "/usr/lib/riemann/"
         :filemode "644"
         :sources [(source (str (file (:root project) 
                                      "target"
                                      (str "riemann-"
                                           (:version project)
                                           "-standalone.jar")))
                           "riemann.jar")]}

        ; Binary
        {:directory "/usr/bin"
         :filemode "0755"
         :sources [(source (file (:root project) "pkg" "deb" "riemann")
                           "riemann")]}

        ; Config
        {:directory "/etc/riemann"
         :filemode "0644"
         :directory-included? true
         :configuration true
         :sources [(source (file (:root project) "pkg" "riemann.config")
                           "riemann.config")]}]))

(defn blank-rpm
  "Create a new RPM file"
  []
  (let [mojo (RPMMojo.)
        fileFilter (DefaultMavenFileFilter.)]
    (set-mojo! mojo "project" (MavenProject.))
    (.enableLogging fileFilter (ConsoleLogger. 0 "Logger"))
    (set-mojo! mojo "mavenFileFilter" fileFilter)))

(defn create-dependency
  [rs]
  (let [hs (java.util.LinkedHashSet.)]
    (doseq [r rs] (.add hs r))
    hs))

(defn make-rpm
  "Create and execute a Mojo RPM."
  [project]
  (let [rpm 
        (doto (blank-rpm)
          (set-mojo! "projversion" (get-version project))
          (set-mojo! "name" (:name project))
          (set-mojo! "summary" (:description project))
          (set-mojo! "workarea" (file (:root project) "target"))
          (set-mojo! "requires" (create-dependency "jre >= 1.6.0"))
          (set-mojo! "mappings" (array-list (mappings project))))]
    (prn "RPM is" rpm)
    (.execute rpm)))

(defn fatrpm
  ([project] (fatrpm project true))
  ([project uberjar?]
   (prn "hi")
   (reset project)
   (when uberjar? (uberjar project))
   (make-rpm project)
   (cleanup project)))
