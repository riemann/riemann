(defproject riemann "0.2.3-SNAPSHOT"
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
    [org.clojure/clojure "1.5.0"]
    [org.clojure/math.numeric-tower "0.0.1"]
    [org.clojure/tools.logging "0.2.3"]
    [org.clojure/tools.nrepl "0.2.2"]
    [clojure-complete "0.2.2"]
    [log4j/log4j "1.2.16" :exclusions [javax.mail/mail
                                       javax.jms/jms
                                       com.sun.jdmk/jmxtools
                                       com.sun.jmx/jmxri]]
    [aleph "0.3.0-beta16"]
    [clj-http "0.4.1"]
    [cheshire "5.0.0"]
    [clj-librato "0.0.2"]
    [clj-time "0.4.3"]
    [clj-wallhack "1.0"]
    [com.boundary/high-scale-lib "1.0.3"]
    [com.draines/postal "1.9.0"]
    [com.amazonaws/aws-java-sdk "1.4.1"]
    [incanter/incanter-core "1.4.1"]
    [interval-metrics "0.0.1"] 
    [io.netty/netty "3.6.1.Final"]
    [log4j/apache-log4j-extras "1.0"]
    [org.antlr/antlr "3.2"]
    [org.slf4j/slf4j-log4j12 "1.6.4"]
    [riemann-clojure-client "0.2.6"]
    [slingshot "0.10.2"]
    [clj-campfire "2.1.0"]
    [less-awful-ssl "0.1.0"]
  ]
  :plugins [[codox "0.6.1"]
            [lein-rpm "0.0.5"]]
  :test-selectors {:default (fn [x] (not (or (:integration x)
                                             (:time x)
                                             (:bench x))))
                   :integration :integration
                   :email :email
                   :sns :sns
                   :graphite :graphite
                   :librato :librato
                   :time :time
                   :bench :bench
                   :focus :focus
                   :all (fn [_] true)}
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :java-source-paths ["src/riemann/"]
  :java-source-path "src/riemann/"
;  :aot [riemann.bin]
  :main riemann.bin
  :codox {:output-dir "site/api"}
)
