(defn dirname [path]
    (.getParent (java.io.File. path)))

(defn expand-path [path]
    (.getCanonicalPath (java.io.File. path)))

(defn relative-path [path]
    (expand-path (str (dirname *file*) "/" path)))

(defproject riemann "0.1.4"
  :description 
"A network event stream processor. Intended for analytics, metrics, and alerting; and to glue various monitoring systems together."
  :url "http://github.com/aphyr/riemann"
;  :warn-on-reflection true
;  :jvm-opts ["-server" "-d64" "-Xms1024m" "-Xmx1024m" "-XX:+UseParNewGC" "-XX:+UseConcMarkSweepGC" "-XX:+CMSParallelRemarkEnabled" "-XX:+AggressiveOpts" "-XX:+UseFastAccessorMethods" "-verbose:gc" "-XX:+PrintGCDetails"]
  :jvm-opts ["-server" "-Xms1024m" "-Xmx1024m" "-XX:+UseParNewGC" "-XX:+UseConcMarkSweepGC" "-XX:+CMSParallelRemarkEnabled" "-XX:+AggressiveOpts" "-XX:+UseFastAccessorMethods"]
  :repositories {
    "boundary-site" "http://maven.boundary.com/artifactory/repo"
  }
  :maintainer {:email "aphyr@aphyr.com"}
  :dependencies [
    [org.clojure/algo.generic "0.1.0"]
    [org.clojure/clojure "1.4.0"]
    [org.clojure/math.numeric-tower "0.0.1"]
    [org.clojure/tools.logging "0.2.3"]
    [log4j/log4j "1.2.16" :exclusions [javax.mail/mail
                                       javax.jms/jms
                                       com.sun.jdmk/jmxtools
                                       com.sun.jmx/jmxri]]
    [aleph "0.2.1-beta2"]
    [clj-http "0.4.1"]
    [cheshire "5.0.0"]
    [clj-librato "0.0.2"]
    [clj-time "0.4.3"]
    [com.boundary/high-scale-lib "1.0.3"]
    [com.draines/postal "1.8.0"]
    [incanter/incanter-charts "1.3.0"]
    [io.netty/netty "3.3.0.Final"]
    [log4j/apache-log4j-extras "1.0"]
    [org.antlr/antlr "3.2"]
    [org.slf4j/slf4j-log4j12 "1.6.4"]
    [riemann-clojure-client "0.0.6"]
    [slingshot "0.10.2"]
  ]
  :plugins [[codox "0.6.1"]]
  :test-selectors {:default (fn [x] (not (or (:integration x)
                                             (:time x)
                                             (:bench x))))
                   :integration :integration
                   :email :email
                   :graphite :graphite
                   :librato :librato
                   :time :time
                   :bench :bench
                   :focus :focus
                   :all (fn [_] true)}
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :java-source-paths ["src/riemann/"]
  :java-source-path "src/riemann/"
  :aot [riemann.bin]
  :main riemann.bin
  :codox {:output-dir "site/api"}
)
