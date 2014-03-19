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
           (net.logstash.log4j JSONEventLayoutV1 JSONEventLayout)
           (org.apache.log4j.spi RootLogger))
  (:import (org.apache.log4j.rolling TimeBasedRollingPolicy
                                     SizeBasedTriggeringPolicy
                                     FixedWindowRollingPolicy
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

(def ^{:doc "available logging patterns"}
  layouts
  {:riemann       (EnhancedPatternLayout. "%p [%d] %t - %c - %m%n%throwable")
   :json-event    (JSONEventLayout.)
   :json-event-v1 (JSONEventLayoutV1.)})

(defn get-layout
  "Fetch a logging layout by name"
  [layout-name]
  (get layouts (or layout-name :riemann)))

(defn init
  "Initialize log4j. You will probably call this from the config file. You can
  call init more than once; its changes are destructive. Options:

  :console         Determine if logging should happen on the console
  :console-layout  On the off-chance that someone runs riemann within runit,
                   keep the option of specifying the layout
  :file            The file to log to. If omitted, logs to console only. If
                   provided log to that file using the default layout
  :files           A list of files to log to. If provided, a seq is expected
                   containing maps with a :path and an optional :layout key
                   which can be any of: :riemann, :json-event :json-eventv1
  :logsize-rotate  If size(bytes) is specified rotate based on that size
                   otherwise use default time based.
  Example:

  (init {:console false :file \"/var/log/riemann.log\"})
    or
  (init {:console false :file \"/var/log/riemann.log\" :logsize-rotate 1000000000})"
  [& opts]
  ;; Reset loggers
  (let [{:keys [file
                files
                console-layout
                logsize-rotate]
         :as opts} (if (and (= 1 (count opts))
                            (map? (first opts)))
                     (first opts)
                     (apply array-map opts))
         console (get opts :console true)
         logger (doto (Logger/getRootLogger) (.removeAllAppenders))]

    (when console
      (.addAppender logger (ConsoleAppender. (get-layout console-layout))))

    (when file
      (if logsize-rotate
        (let [rolling-policy (doto (FixedWindowRollingPolicy.)
                               (.setActiveFileName file)
                               (.setMaxIndex 5)
                               (.setFileNamePattern
                                (str file "%d{yyyy-MM-dd}.%i.gz"))
                               (.activateOptions))
              triggering-policy (doto (SizeBasedTriggeringPolicy.)
                                  (.setMaxFileSize logsize-rotate)
                                  (.activateOptions))
              log-appender (doto (RollingFileAppender.)
                             (.setRollingPolicy rolling-policy)
                             (.setTriggeringPolicy triggering-policy)
                             (.setLayout (get-layout :riemann))
                             (.activateOptions))]
          (.addAppender logger log-appender))

        (let [rolling-policy (doto (TimeBasedRollingPolicy.)
                               (.setActiveFileName file)
                               (.setFileNamePattern
                                (str file ".%d{yyyy-MM-dd}.gz"))
                               (.activateOptions))
              log-appender (doto (RollingFileAppender.)
                             (.setRollingPolicy rolling-policy)
                             (.setLayout (get-layout :riemann))
                             (.activateOptions))]
          (.addAppender logger log-appender))))

    (when files
      (doseq [{:keys [path layout]} files
              :let [layout (get-layout layout)]]
        (if logsize-rotate
          (let [rolling-policy (doto (FixedWindowRollingPolicy.)
                                 (.setActiveFileName file)
                                 (.setMaxIndex 5)
                                 (.setFileNamePattern
                                  (str file "%d{yyyy-MM-dd}.%i.gz"))
                                 (.activateOptions))
                triggering-policy (doto (SizeBasedTriggeringPolicy.)
                                    (.setMaxFileSize logsize-rotate)
                                    (.activateOptions))
                log-appender (doto (RollingFileAppender.)
                               (.setRollingPolicy rolling-policy)
                               (.setTriggeringPolicy triggering-policy)
                               (.setLayout (get-layout :riemann))
                               (.activateOptions))]
            (.addAppender logger log-appender))

          (let [rolling-policy (doto (TimeBasedRollingPolicy.)
                                 (.setActiveFileName path)
                                 (.setFileNamePattern
                                  (str path ".%d{yyyy-MM-dd}.gz"))
                                 (.activateOptions))
                log-appender (doto (RollingFileAppender.)
                               (.setRollingPolicy rolling-policy)
                               (.setLayout layout)
                               (.activateOptions))]
            (.addAppender logger log-appender)))))

      ;; Set levels.
      (.setLevel logger Level/INFO)

    (set-level "riemann.client" Level/DEBUG)
    (set-level "riemann.server" Level/DEBUG)
    (set-level "riemann.streams" Level/DEBUG)
    (set-level "riemann.graphite" Level/DEBUG)))


(defn nice-syntax-error
  "Rewrites clojure.lang.LispReader$ReaderException to have error messages that
  might actually help someone."
  ([e] (nice-syntax-error e "(no file)"))
  ([e file]
   ; Lord help me.
   (let [line (wall.hack/field (class e) :line e)
         msg (.getMessage (or (.getCause e) e))]
    (RuntimeException. (str "Syntax error (" file ":" line ") " msg)))))
