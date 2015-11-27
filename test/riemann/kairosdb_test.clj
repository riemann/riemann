(ns riemann.kairosdb-test
  (:use riemann.kairosdb
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:kairosdb ^:integration kairosdb-send-test
  (let [k (kairosdb {:block-start true})]
    (k {:host "riemann.local"
        :service "kairosdb test"
        :state "ok"
        :description "all clear, uh, situation normal"
        :metric -2
        :time (unix-time)}))

  (let [k (kairosdb {:block-start true})]
    (k {:service "kairosdb test"
        :state "ok"
        :description "all clear, uh, situation normal"
        :metric 3.14159
        :time (unix-time)}))

  (let [k (kairosdb {:block-start true})]
    (k {:host "no-service.riemann.local"
        :state "ok"
        :description "Missing service, not transmitted"
        :metric 4
        :time (unix-time)}))

  (let [k (kairosdb {:block-start true
                     :protocol :http
                     :port 8080})]
    (k {:host "no-service.riemann.local"
        :state "ok"
        :description "Missing service, not transmitted"
        :metric 5
        :time (unix-time)})))

(deftest ^:kairosdb kairosdb-batching-test
  (let [events [{:host "no-service.riemann.local"
                 :service "kairosdb test"
                 :state "ok"
                 :description "Missing service, not transmitted"
                 :metric 4
                 :time (unix-time)}
                {:host "riemann.local"
                 :service "kairosdb test"
                 :state "ok"
                 :description "Situation nominal"
                 :metric 5
                 :time (unix-time)}]
        output (atom [])
        mock-client (reify KairosDBClient
                      (open [this] this)
                      (close [this] this)
                      (send-metrics [this metrics] (swap! output conj metrics)))]
    (with-redefs [riemann.kairosdb/make-kairosdb-client (fn [_ _ _] mock-client)]
      (let [k (kairosdb {:protocol :tcp
                         :batch true
                         :batch-opts {:n 2 :dt 60}})]
        (doseq [event events]
          (k event)))
      (is (= (map count @output) [2])))))
