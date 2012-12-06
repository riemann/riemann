(ns riemann.logging
  "Configures log4j to log to a file. It's a trap!"
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
  (:import org.apache.commons.logging.LogFactory))

(defn set-level
  "Set the level for the given logger, by string name. Use:
  (set-level \"riemann.client\", Level/DEBUG)"
  [logger level]
  (. (Logger/getLogger logger) (setLevel level)))

(def riemann-layout 
  "A nice format for log lines."
  (EnhancedPatternLayout. "%p [%d] %t - %c - %m%n%throwable%n"))

(defn init
  "Initialize log4j. You will probably call this from the config file. Options:

  :file   The file to log to. If omitted, logs to console only."
  [& { :keys [file] }]
  ; Reset loggers
  (doto (Logger/getRootLogger)
    (.removeAllAppenders)
    (.addAppender (ConsoleAppender. riemann-layout)))

  (when file
    (let [rolling-policy (doto (TimeBasedRollingPolicy.)
                           (.setActiveFileName file)
                           (.setFileNamePattern
                             (str file ".%d{yyyy-MM-dd}.gz"))
                           (.activateOptions))
          log-appender (doto (RollingFileAppender.)
                         (.setRollingPolicy rolling-policy)
                         (.setLayout riemann-layout)
                         (.activateOptions))]
      (.addAppender (Logger/getRootLogger) log-appender)))

  ; Set levels.
  (. (Logger/getRootLogger) (setLevel Level/INFO))

  (set-level "riemann.client" Level/DEBUG)
  (set-level "riemann.server" Level/DEBUG)
  (set-level "riemann.streams" Level/DEBUG)
  (set-level "riemann.graphite" Level/DEBUG))

; Not sure where he intended this to go....
(defn- add-file-appender [loggername filename]
  (.addAppender (Logger/getLogger loggername)
                (doto (FileAppender.)
                  (.setLayout riemann-layout))))
