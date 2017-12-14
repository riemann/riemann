(ns riemann.datadog-test
  (:require [riemann.datadog :refer :all]
            [riemann.logging :as logging]
            [riemann.time :refer [unix-time]]
            [clojure.test :refer :all]))

(def datadog-key (or (System/getenv "DATADOG_API_KEY")
                     (do (println "export DATADOG_API_KEY=\"...\" to run these tests.")
                         "datadog-test-key")))

(logging/init)

(deftest ^:datadog ^:integration datadog-test
  (let [k (datadog {:api-key datadog-key})]
    (k {:host        "riemann.local"
        :service     "datadog test"
        :state       "ok"
        :description "all clear, uh, situation normal"
        :metric      -2
        :time        (unix-time)}))

  (let [k (datadog {:api-key datadog-key})]
    (k {:service     "datadog test"
        :state       "ok"
        :description "all clear, uh, situation normal"
        :metric      3.14159
        :time        (unix-time)}))

  (let [k (datadog {:api-key datadog-key})]
    (k {:host        "no-service.riemann.local"
        :state       "ok"
        :description "Missing service, not transmitted"
        :metric      4
        :time        (unix-time)}))

  (let [k (datadog {:api-key datadog-key})]
    (k [{:host        "no-service.riemann.local"
         :state       "ok"
         :description "Missing service, not transmitted"
         :metric      4
         :time        (unix-time)},
        {:host        "no-service.riemann.local"
         :state       "ok"
         :description "Missing service, not transmitted"
         :metric      4
         :time        (unix-time)}])
    (k [{:host        "no-service.riemann.local"
         :service     "datadog test"
         :state       "ok"
         :description "all clear, uh, situation normal"
         :metric      4
         :time        (unix-time)},
        {:host        "batch-test.riemann.local"
         :service     "datadog test"
         :state       "ok"
         :description "all clear, uh, situation normal"
         :metric      4
         :time        (unix-time)}
        {:host        "batch-test.riemann.local" ;; look for this service and host
         :service     "datadog test"             ;; tag in datadog's "metric summary"
         :state       "ok"
         :description "no metric"
         :time        (unix-time)}])))
