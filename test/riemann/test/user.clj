(ns riemann.test.user
  "Userland macros for testing snippets of Riemann config."
  (:use
   riemann.streams
   riemann.client
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
  (doseq [ev events]
    (riemann.core/stream! @riemann.config/core ev)))

(defn search-index [query]
  (riemann.index/search (:index @riemann.config/core)
                        (riemann.query/ast query)))
