(ns riemann.user-test
  "Userland macros for testing snippets of Riemann config."
  (:use
   riemann.streams
   riemann.email
   riemann.sns
   [riemann.time :only [unix-time linear-time once! every!]])
  (:require
   riemann.streams
   riemann.config
   riemann.core
   riemann.index
   riemann.query))

(defmacro configure-core [& conf]
  "Load the given Riemann conf into the current core and reset the index.
FOR TESTING PURPOSES ONLY."
  `(binding [*ns* (find-ns 'riemann.config)]
    (eval '(do
              (~'riemann.time/reset-tasks!)
              (~'clear!)
              (~'pubsub/sweep! (:pubsub @~'core))
              (~'logging/init)
              (~'instrumentation {:enabled? false})
              (~'periodically-expire)
              (~'streams ~@conf)
              (when-let [idx# (:index @~'core)]
                (~'riemann.index/clear idx#))
              (~'apply!)))))

(defn stream-events [& events]
  "Run the given events through the current config."
  (doseq [ev events]
    (riemann.core/stream! @riemann.config/core ev)))

(defn search-index [query]
  "Query the current state of the Riemann index."
  (riemann.index/search (:index @riemann.config/core)
                        (riemann.query/ast query)))
