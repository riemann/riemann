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
  (:import org.apache.commons.logging.LogFactory)
  (:require wall.hack))

(defn set-level
  "Set the level for the given logger, by string name. Use:
  (set-level \"riemann.client\", Level/DEBUG)"
  ([level]
   (. (Logger/getRootLogger) (setLevel level)))
  ([logger level]
   (. (Logger/getLogger logger) (setLevel level))))

(defmacro suppress
  "Turns off logging for the evaluation of body."
  [loggers & body]
  (let [[logger & more] (flatten [loggers])]
    (if logger
      `(let [old-level# (.getLevel (Logger/getLogger ~logger))]
         (try
           (set-level ~logger Level/FATAL)
           (suppress ~more ~@body)
           (finally
             (set-level ~logger old-level#))))
      `(do ~@body))))

(def riemann-layout 
  "A nice format for log lines."
  (EnhancedPatternLayout. "%p [%d] %t - %c - %m%n%throwable%n"))

(defn init
  "Initialize log4j. You will probably call this from the config file. You can
  call init more than once; its changes are destructive. Options:

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

(defn nice-syntax-error
  "Rewrites clojure.lang.LispReader$ReaderException to have error messages that
  might actually help someone."
  ([e] (nice-syntax-error e "(no file)"))
  ([e file]
   ; Lord help me.
   (let [line (wall.hack/field (class e) :line e)
         msg (.getMessage (or (.getCause e) e))]
    (RuntimeException. (str "Syntax error (" file ":" line ") " msg)))))
