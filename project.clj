(defproject riemann "0.2.9"
  :description
"A network event stream processor. Intended for analytics, metrics, and alerting; and to glue various monitoring systems together."
  :url "http://github.com/aphyr/riemann"
;  :warn-on-reflection true
;  :jvm-opts ["-server" "-d64" "-Xms1024m" "-Xmx1024m" "-XX:+UseParNewGC" "-XX:+UseConcMarkSweepGC" "-XX:+CMSParallelRemarkEnabled" "-XX:+AggressiveOpts" "-XX:+UseFastAccessorMethods" "-verbose:gc" "-XX:+PrintGCDetails"]
  :jvm-opts ["-server" "-Xms1024m" "-Xmx1024m" "-XX:+UseParNewGC" "-XX:+UseConcMarkSweepGC" "-XX:+CMSParallelRemarkEnabled" "-XX:+AggressiveOpts" "-XX:+UseFastAccessorMethods" "-XX:+CMSClassUnloadingEnabled"]
  :maintainer {:email "aphyr@aphyr.com"}
  :dependencies [
    [org.clojure/algo.generic "0.1.2"]
    [org.clojure/clojure "1.6.0"]
    [org.clojure/math.numeric-tower "0.0.4"]
    [org.clojure/tools.logging "0.3.1"]
    [org.clojure/tools.nrepl "0.2.7"]
    [org.clojure/core.cache "0.6.4"]
    [org.clojure/java.classpath "0.2.2"]
    [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                       javax.jms/jms
                                       com.sun.jdmk/jmxtools
                                       com.sun.jmx/jmxri]]
    [net.logstash.log4j/jsonevent-layout "1.7"]
    [com.cemerick/pomegranate "0.3.0"]
    [org.spootnik/http-kit "2.1.18.1"]
    [clj-http "1.0.1" :exclusions [org.clojure/tools.reader]]
    [cheshire "5.4.0"]
    [clj-librato "0.0.5"]
    [clj-time "0.9.0"]
    [clj-wallhack "1.0.1"]
    [com.boundary/high-scale-lib "1.0.6"]
    [com.draines/postal "1.11.3"]
    [com.amazonaws/aws-java-sdk "1.9.13" :exclusions [joda-time]]
    [interval-metrics "1.0.0"]
    [io.netty/netty-all "4.0.24.Final"]
    [log4j/apache-log4j-extras "1.2.17"]
;    [org.antlr/antlr "3.2"]
    [clj-antlr "0.2.2"]
    [org.slf4j/slf4j-log4j12 "1.7.10"]
    [riemann-clojure-client "0.3.2"]
    [less-awful-ssl "1.0.0"]
    [slingshot "0.12.1"]
    [clj-campfire "2.2.0"]
    [clj-nsca "0.0.3"]
    [amazonica "0.3.13" :exclusions [joda-time]]
    [capacitor "0.4.2" :exclusions [http-kit]]]
  :plugins [[codox "0.6.1"]
            [lein-rpm "0.0.5"]]
  :profiles {:dev {:jvm-opts ["-XX:-OmitStackTraceInFastThrow"]
;                              "-Dcom.sun.management.jmxremote"
;                              "-XX:+UnlockCommercialFeatures"
;                              "-XX:+FlightRecorder"]
                   :dependencies [[criterium "0.4.3"]]}}
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
                   :cloudwatch :cloudwatch
                   :datadog :datadog
                   :stackdriver :stackdriver
		   :xymon :xymon
                   :shinken :shinken
                   :blueflood :blueflood
                   :opsgenie :opsgenie
                   :boundary :boundary
                   :all (fn [_] true)}
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :java-source-paths ["src/riemann/"]
  :java-source-path "src/riemann/"
;  :aot [riemann.bin]
  :main riemann.bin
  :codox {:output-dir "site/api"
          :src-dir-uri "http://github.com/aphyr/riemann/blob/master/"
          :src-linenum-anchor-prefix "L"
          :defaults {:doc/format :markdown}}
)
