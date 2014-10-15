(ns riemann.logging
  "Configures log4j to log to a file. It's a trap!"
  ; With thanks to arohner
  (:import org.apache.log4j.Logger
           org.apache.log4j.Level)
  (:require wall.hack
            org.spootnik.logconfig))

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

(def init
  "Initialize log4j logging from a map.

   The map accepts the following keys as keywords:

   - `:level`: Default level at which to log.
   - `:pattern`: The pattern to use for logging text messages
   - `:console`: Append messages to the console using a simple pattern
      layout
   - `:files`: A list of either strings or maps. strings will create
      text files, maps are expected to contain a `:path` key as well
      as an optional `:json` which when present and true will switch
      the layout to a JSONEventLayout for the logger.
   - `:overrides`: A map of namespace or class-name to log level,
      this will supersede the global level.
   - `:json`: When true, console logging will use a JSON Event layout
   - `:external`: Do not proceed with configuration, this
      is useful when logging configuration is provided
      in a different manner (by supplying a log4j properties
      file through the `log4j.configuration` property for instance.

example:

```clojure
{:console true?
 :level     \"info\"
 :pattern   \"%p [%d] %t - %c - %m%n\"
 :files     [\"/var/log/app.log\"
             {:path \"/var/log/app-json.log\"
              :json true}]
 :overrides {\"some.namespace\" \"debug\"}}
```
  "
  #'org.spootnik.logconfig/start-logging!)


(defn nice-syntax-error
  "Rewrites clojure.lang.LispReader$ReaderException to have error messages that
  might actually help someone."
  ([e] (nice-syntax-error e "(no file)"))
  ([e file]
   ; Lord help me.
   (let [line (wall.hack/field (class e) :line e)
         msg (.getMessage (or (.getCause e) e))]
    (RuntimeException. (str "Syntax error (" file ":" line ") " msg)))))
