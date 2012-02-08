(defproject reimann "0.0.1-SNAPSHOT"
  :description "reimann: folds events into states"
  :url "http://github.com/aphyr/ustate"
  :repositories {
    "boundary-site" "http://maven.boundary.com/artifactory/repo"
  }
  :dependencies [
    [clojure "1.2.0"]
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
        ; "No directory specified for tarfileset". Hmm.
        :filesets [{:file     "/home/aphyr/ustate/reimann/reimann-0.0.1-SNAPSHOT-standalone.jar"
                    :fullpath "/usr/lib/reimann/reimann.jar"}
                   {:file     "/home/aphyr/ustate/reimann/reimann.config"
                    :fullpath "/etc/reimann/reimann.config"}
                   {:file     "/home/aphyr/ustate/reimann/bin/reimann"
                    :fullpath "/usr/bin/reimann"
                    :filemode "0755"}]
        :depends ""}
  :deb-skip-jar true
)
