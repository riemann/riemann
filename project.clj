(defn dirname [path]
    (.getParent (java.io.File. path)))

(defn expand-path [path]
    (.getCanonicalPath (java.io.File. path)))

(defn relative-path [path]
    (expand-path (str (dirname *file*) "/" path)))

(defproject reimann "0.0.3-SNAPSHOT"
  :description 
"A network event stream processor. Intended for analytics, metrics, and
alerting; and to glue various monitoring systems together."
  :url "http://github.com/aphyr/reimann"
  :repositories {
    "boundary-site" "http://maven.boundary.com/artifactory/repo"
  }
  :dependencies [
    [clojure "1.2.0"]
    [org.clojure/tools.logging "0.2.3"]
    [log4j/log4j "1.2.16" :exclusions [javax.mail/mail
                                       javax.jms/jms
                                       com.sun.jdmk/jmxtools
                                       com.sun.jmx/jmxri]]
    [log4j/apache-log4j-extras "1.0"]
    [org.jboss.netty/netty "3.2.5.Final"]
    [aleph "0.2.0"]
    [protobuf "0.6.0-beta5"]
    [org.antlr/antlr "3.2"]
    [com.boundary/high-scale-lib "1.0.3"]
    [clj-time "0.3.4"]
    [com.draines/postal "1.7-SNAPSHOT"]
  ]
  :dev-dependencies [
    [lein-deb "1.0.0-SNAPSHOT"]
    [protobuf "0.6.0-beta5"]
    [lein-autodoc "0.9.0"]
    [codox "0.4.0"]
  ]
  :test-selectors {:default (complement :integration)
                   :integration :integration
                   :all (fn [_] true)}
  :java-source-path "src/reimann/"
  :aot [reimann.bin]
  :main reimann.bin
  ; Work around a bug where protobufs get nuked.
  :disable-implicit-clean true
  :deb {:maintainer {:name "Kyle Kingsbury"
                     :email "aphyr@aphyr.com"}
        ; I wish I could use relative paths here, but lein-deb complains
        ; "No directory specified for tarfileset", and lein macros need them
        ; to be strings. Arrrgh.
        :filesets [{:file     "/home/aphyr/reimann/reimann-0.0.3-SNAPSHOT-standalone.jar"
                    :fullpath "/usr/lib/reimann/reimann.jar"}
                   {:file     "/home/aphyr/reimann/reimann.config"
                    :fullpath "/etc/reimann/reimann.config"}
                   {:file     "/home/aphyr/reimann/bin/reimann"
                    :fullpath "/usr/bin/reimann"
                    :filemode "0755"}]
        :depends ""}
  :deb-skip-jar true
)
