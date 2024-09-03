(ns riemann.bin
  "Main function."
  (:require [riemann.config :as config]
            [riemann.logging :as logging]
            riemann.time
            [riemann.test :as test]
            [clojure.test :as clj-test]
            [clojure.test.junit :refer [with-junit-output]]
            riemann.pubsub
            [cemerick.pomegranate :as pom]
            [clojure.java.io :as io]
            [clojure.tools.logging :refer [info error]])
  (:import (clojure.lang DynamicClassLoader))
  (:gen-class :name riemann.bin))

(def config-file
  "The configuration file loaded by the bin tool"
  (promise))

(defn add-config-dir-to-classpath
  [config-file]
  (let [dir (-> config-file
                io/file
                .getCanonicalFile
                .getParent)]
    (pom/add-classpath dir)))

(defn set-config-file!
  "Sets the config file used by Riemann. Adds the config file's enclosing
  directory to the classpath as well."
  [file]
  (info "Loading" (-> file io/file .getCanonicalPath))
  (assert (deliver config-file file)
          (str "Config file already set to " (pr-str @config-file)
               "--can't change it to " (pr-str file)))
  (add-config-dir-to-classpath file))

(def reload-lock (Object.))

(defn reload!
  "Reloads the given configuration file by clearing the task scheduler, shutting
  down the current core, and loading a new one."
  []
  (locking reload-lock
    (try
      (riemann.config/validate-config @config-file)
      (riemann.time/reset-tasks!)
      (riemann.config/clear!)
      (riemann.pubsub/sweep! (:pubsub @riemann.config/core))
      (riemann.config/include @config-file)
      (riemann.config/apply!)
      :reloaded
      (catch Exception e
        (error e "Couldn't reload:")
        e))))

(defn ensure-dynamic-classloader
  []
  (let [thread (Thread/currentThread)
        context-class-loader (.getContextClassLoader thread)
        compiler-class-loader (.getClassLoader clojure.lang.Compiler)]
    (when-not (instance? DynamicClassLoader context-class-loader)
      (.setContextClassLoader
        thread (DynamicClassLoader. (or context-class-loader
                                        compiler-class-loader))))))

(defn handle-signals
  "Sets up POSIX signal handlers."
  []
  (when-not (.contains (. System getProperty "os.name") "Windows")
    (sun.misc.Signal/handle
     (sun.misc.Signal. "HUP")
     (proxy [sun.misc.SignalHandler] []
       (handle [sig]
         (info "Caught SIGHUP, reloading")
         (ensure-dynamic-classloader)
         (add-config-dir-to-classpath @config-file)
         (reload!))))))

(defn pom-version
  "Return version from Maven POM file."
  []
  (let [pom "META-INF/maven/riemann/riemann/pom.properties"
        props (doto (java.util.Properties.)
                (.load (-> pom io/resource io/input-stream)))]
    (.getProperty props "version")))

(defn version
  "Return version from Leiningen environment or embeddd POM properties."
  []
  (or (System/getProperty "riemann.version")
      (pom-version)))

(defn pid
  "Process identifier, such as it is on the JVM. :-/"
  []
  (let [name (-> (java.lang.management.ManagementFactory/getRuntimeMXBean)
                 (.getName))]
    (try
      (get (re-find #"^(\d+).*" name) 1)
      (catch Exception _ name))))

(defn run-tests-with-format
  "Run all tests matching `test-name-pattern`.
  If the `test.output.format` property is set to `junit`, the test output will be
  JUnit compatible."
  [test-name-pattern]
  (if (= (System/getProperty "test.output.format")
         "junit")
    (with-junit-output
      (clj-test/run-all-tests test-name-pattern))
    (clj-test/run-all-tests test-name-pattern)))

(defn run-tests
  "Run all tests matching `test-name-pattern`.
  The test output will be saved in the `test.output.file` value if this property is set."
  [test-name-pattern]
  (if-let [file-path (System/getProperty "test.output.file")]
    (with-open [w (java.io.FileWriter. file-path)]
      (binding [clj-test/*test-out* w]
        (run-tests-with-format test-name-pattern)))
    (run-tests-with-format test-name-pattern)))

(defn run-app!
  "Start Riemann with the given setup function. The setup function is responsible
  for executing code that you would normally put into your riemann.config file,
  e.g. setting up streams."
  [setup]
  (info "PID" (pid))
  (handle-signals)
  (riemann.time/start!)
  (setup)
  (riemann.config/apply!)
  nil)

(defn -main
  "Start Riemann. Loads a configuration file from the first of its args."
  ([]
   (-main "riemann.config"))
  ([config]
   (-main "start" config))
  ([command config & [test-name]]
   (logging/init)
   (ensure-dynamic-classloader)
   (case command
     "start" (try
               (set-config-file! config)
               (run-app! #(riemann.config/include @config-file))
               (catch Exception e
                 (error e "Couldn't start")))

     "test" (try
              (test/with-test-env
                (set-config-file! config)
                (riemann.config/include @config-file)
                (reset! riemann.config/core @riemann.config/next-core)
                (binding [test/*core* @config/core]
                  (let [test-name-pattern (if test-name (re-pattern test-name) #".*-test")
                        results (run-tests test-name-pattern)]
                    (if (and (zero? (:error results))
                             (zero? (:fail results)))
                      (System/exit 0)
                      (System/exit 1)))))
              (catch Exception e
                (error e "Couldn't run tests")))

     "version" (try
                 (println (version))
                 (catch Exception e
                   (error e "Couldn't read version"))))))
