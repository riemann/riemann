(ns riemann.logging-test
  (:require [riemann.logging :as logging]
            [clojure.java.io :as io]
            [clojure.test :refer :all])
  (:import (ch.qos.logback.classic Logger Level)
           (ch.qos.logback.core Appender ConsoleAppender)
           (java.util Iterator)))

(logging/init)

(deftest test-logging_init_default_level_info
  "ROOT logger should be configured by default to level INFO"
  (logging/init)
  (is (= (Level/INFO) (.getLevel (cast Logger (logging/get-logger))))))

(deftest test-logging_init_default_level_debug
  "client, server, streams and graphite logger should be configured by default to level DEBUG"
  (logging/init)
  (is (= (Level/DEBUG) (.getLevel (cast Logger (logging/get-logger "riemann.client")))))
  (is (= (Level/DEBUG) (.getLevel (cast Logger (logging/get-logger "riemann.server")))))
  (is (= (Level/DEBUG) (.getLevel (cast Logger (logging/get-logger "riemann.streams")))))
  (is (= (Level/DEBUG) (.getLevel (cast Logger (logging/get-logger "riemann.graphite"))))))

(deftest test-logging_init_default_appender
  "ROOT logger should be configured with a ConsoleAppender"
  (logging/init)
  (is (= ConsoleAppender (.getClass (cast Appender (.next (cast Iterator (.iteratorForAppenders (cast Logger (logging/get-logger))))))))))

(deftest test-logging_init_file
  "Logging system should be configured using a file"
  (let [logback-file-url (io/resource "data/logging/logback_warn.xml")]
    (System/setProperty "logback.configurationFile" (.toString logback-file-url))
    (logging/init)
    (is (= (Level/WARN) (.getLevel (cast Logger (logging/get-logger)))))))


(defn test-ns-hook []
  (test-logging_init_default_level_info)
  (test-logging_init_default_level_debug)
  (test-logging_init_default_appender)
  (test-logging_init_file))
