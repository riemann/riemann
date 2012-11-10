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

(def riemann-layout (EnhancedPatternLayout. "%p [%d] %t - %c - %m%n%throwable%n"))

(defn init
  "Initialize log4j. You will probably call this from the config file. Options:

  :file   The file to log to.
          Use \"/dev/null\" on *nix or \"NUL:\" on Windows to log to stdout only."
  [& { :keys [file] }]
  (let [filename (or file "riemann.log")
        rolling-policy (doto (TimeBasedRollingPolicy.)
                         (.setActiveFileName filename)
                         (.setFileNamePattern
                           (str filename ".%d{yyyy-MM-dd}.gz"))
                         (.activateOptions))
        log-appender (doto (RollingFileAppender.)
                       (.setRollingPolicy rolling-policy)
                       (.setLayout riemann-layout)
                       (.activateOptions))]
    (doto (Logger/getRootLogger)
      (.removeAllAppenders)
      (.addAppender log-appender)
      (.addAppender (ConsoleAppender. riemann-layout))))
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
