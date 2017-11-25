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

(defn set-up-fixture
  "A fixture to be used with `clojure.test/deftest`.

   Environmental variable `config` must be set.
   Its value should be a path to riemann config file.

   Example:
   ```
   (ns my-namespace
     (:require [clojure.test :refer :all]
               [riemann.test-runner :as test-runner]))

   (use-fixtures :once test-runner/set-up)

   (deftest some-test
     ; your test
     )
   ```"
  [f]
  (let [env-config (System/getenv "config")]
    (if env-config
      (set-up-for-single-test f env-config)
      (run-test f))))