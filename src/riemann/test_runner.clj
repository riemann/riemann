(ns riemann.test-runner
  (:require [riemann.test :as test]
            [riemann.config :as config]
            [riemann.bin :as bin]
            [riemann.time.controlled :as time.controlled]))

(defn- run-test
  [f]
  (binding [test/*results* (test/fresh-results @test/*taps*)]
    (time.controlled/with-controlled-time!
      (time.controlled/reset-time!)
      (f))))

(defn- set-up-for-single-test
  [f config]
  (do
    (riemann.config/clear!)
    (test/with-test-env
      (when-not (realized? bin/config-file)
        (bin/set-config-file! config))
      (riemann.config/include @bin/config-file)
      (binding [test/*streams* (:streams @config/next-core)]
        (run-test f)))))

(defn set-up
  [f]
  (let [env-config (System/getenv "config")]
    (if env-config
      (set-up-for-single-test f env-config)
      (run-test f))))