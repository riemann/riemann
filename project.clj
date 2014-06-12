(defproject riemann "0.2.6-SNAPSHOT"
  :description
"A network event stream processor. Intended for analytics, metrics, and alerting; and to glue various monitoring systems together."
  :url "http://github.com/aphyr/riemann"
;  :warn-on-reflection true
;  :jvm-opts ["-server" "-d64" "-Xms1024m" "-Xmx1024m" "-XX:+UseParNewGC" "-XX:+UseConcMarkSweepGC" "-XX:+CMSParallelRemarkEnabled" "-XX:+AggressiveOpts" "-XX:+UseFastAccessorMethods" "-verbose:gc" "-XX:+PrintGCDetails"]
  :jvm-opts ["-server" "-Xms1024m" "-Xmx1024m" "-XX:+UseParNewGC" "-XX:+UseConcMarkSweepGC" "-XX:+CMSParallelRemarkEnabled" "-XX:+AggressiveOpts" "-XX:+UseFastAccessorMethods" "-XX:+CMSClassUnloadingEnabled"]
  :repositories {
    "boundary-site" "http://maven.boundary.com/artifactory/repo"
  }
  :maintainer {:email "aphyr@aphyr.com"}
  :dependencies [
    [org.clojure/algo.generic "0.1.2"]
    [org.clojure/clojure "1.6.0"]
    [org.clojure/math.numeric-tower "0.0.4"]
    [org.clojure/tools.logging "0.2.6"]
    [org.clojure/tools.nrepl "0.2.3"]
    [org.clojure/core.cache "0.6.3"]
    [org.clojure/java.classpath "0.2.2"]
    [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                       javax.jms/jms
                                       com.sun.jdmk/jmxtools
                                       com.sun.jmx/jmxri]]
    [net.logstash.log4j/jsonevent-layout "1.5"]
    [aleph "0.3.2"]
    [clj-http "0.9.1"]
    [cheshire "5.3.1"]
    [clj-librato "0.0.4"]
    [clj-time "0.6.0"]
    [clj-wallhack "1.0.1"]
    [com.boundary/high-scale-lib "1.0.4"]
    [com.draines/postal "1.11.1"]
    [com.amazonaws/aws-java-sdk "1.7.5"]
    [interval-metrics "1.0.0"]
    [io.netty/netty "3.8.0.Final"]
    [log4j/apache-log4j-extras "1.0"]
    [org.antlr/antlr "3.2"]
    [org.slf4j/slf4j-log4j12 "1.7.7"]
    [riemann-clojure-client "0.2.10"]
    [slingshot "0.10.3"]
    [clj-campfire "2.2.0"]
    [less-awful-ssl "0.1.1"]
    [clj-nsca "0.0.3"]
    [capacitor "0.2.2"]
  ]
  :plugins [[codox "0.6.1"]
            [lein-rpm "0.0.5"]]
  :profiles {:dev {:dependencies [[criterium "0.4.3"]]}}
  :test-selectors {:default (fn [x] (not (or (:integration x)
                                             (:time x)
                                             (:bench x))))
                   :integration :integration
                   :email :email
                   :sns :sns
                   :graphite :graphite
                   :influxdb :influxdb
                   :kairosdb :kairosdb
                   :librato :librato
                   :hipchat :hipchat
                   :nagios :nagios
                   :opentsdb :opentsdb
                   :time :time
                   :bench :bench
                   :focus :focus
                   :slack :slack
                   :all (fn [_] true)}
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :java-source-paths ["src/riemann/"]
  :java-source-path "src/riemann/"
;  :aot [riemann.bin]
  :main riemann.bin
  :codox {:output-dir "site/api"}
)
