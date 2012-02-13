(ns reimann.logging
  ; With thanks to arohner
  (:import (org.apache.log4j 
             Logger
             BasicConfigurator
             EnhancedPatternLayout
             Level
             ConsoleAppender
             FileAppender
             SimpleLayout)
           (org.apache.log4j.spi RootLogger))
  (:import (org.apache.log4j.rolling TimeBasedRollingPolicy
                                     RollingFileAppender))
  (:import org.apache.commons.logging.LogFactory)
  (:use [clojure.contrib.string :only (as-str)]))

(defn set-level [logger level]
  (. (Logger/getLogger logger) (setLevel level)))

(def reimann-layout (EnhancedPatternLayout. "%p [%d] %t - %c - %m%n"))

(defn init 
  ([] (init {}))
  ([opts]
    (let [filename (or (:file opts) "reimann.log")
          rolling-policy (doto (TimeBasedRollingPolicy.)
                           (.setActiveFileName filename)
                           (.setFileNamePattern 
                             (str filename ".%d{yyyy-MM-dd}.gz"))
                           (.activateOptions))
          log-appender (doto (RollingFileAppender.)
                         (.setRollingPolicy rolling-policy)
                         (.setLayout reimann-layout)
                         (.activateOptions))]
      (doto (Logger/getRootLogger)
        (.removeAllAppenders)
        (.addAppender log-appender)
        (.addAppender (ConsoleAppender. reimann-layout))))
    (. (Logger/getRootLogger) (setLevel Level/INFO))

    (set-level "reimann.client" Level/DEBUG)
    (set-level "reimann.server" Level/DEBUG)
    (set-level "reimann.streams" Level/DEBUG)
    (set-level "reimann.graphite" Level/DEBUG)))

; Not sure where he intended this to go....
(defn add-file-appender [loggername filename]
  (.addAppender (Logger/getLogger loggername)
                (doto (FileAppender.)
                  (.setLayout reimann-layout))))
