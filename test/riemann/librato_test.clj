(ns riemann.librato-test
  (:use riemann.librato
        [riemann.time :only [unix-time]]
        clj-librato.metrics
        clojure.math.numeric-tower
        clojure.test)
  (:require [riemann.logging :as logging]))

(def user   (System/getenv "LIBRATO_METRICS_USER"))
(def api-key (System/getenv "LIBRATO_METRICS_API_KEY"))

(when-not user
    (println "export LIBRATO_METRICS_USER=\"...\" to run these tests."))
(when-not api-key
    (println "export LIBRATO_METRICS_API_KEY=\"...\" to run these tests."))

(logging/init)

(defn get-metric
  "Get a metric for a gauge or counter."
  [gauge options]
  (-> (metric user api-key (:name gauge)
              {:end-time (:measure-time gauge)
               :count 1
               :resolution 1}
              options)
    :measurements
    (get (or (:source gauge) "unassigned"))
    (first)))

(deftest ^:librato ^:integration librato-metrics-test
         (let [l (librato-metrics user api-key)
               http-options (:riemann.librato/http-options l)]
           (testing "gauge with source"
                    (let [e {:host "a" :service "b" :metric (rand)
                             :time (unix-time)}
                          r ((:gauge l) e)
                          m (get-metric (event->gauge e) http-options)]
                      (is m)
                      (is (= (:metric e) (:value m)))
                      (is (= (round (:time e)) (:measure-time m)))))

           (testing "multiple gauges with source"
                    (let [e0 {:host "a" :service "b" :metric (rand)
                             :time (unix-time)}
                          e1 {:host "c" :service "d" :metric (rand)
                             :time (unix-time)}
                          r ((:gauge l) [e0 e1])]
                      (for [event [e0 e1]
                            :let [m (get-metric (event->gauge event)
                                                http-options)]]
                        (do
                          (is m)
                          (is (= (:metric event) (:value m)))
                          (is (= (round (:time event)) (:measure-time m)))))))

           (testing "gauge without source"
                    (let [e {:service "sourceless" :metric (rand)
                             :time (unix-time)}
                          r ((:gauge l) e)
                          m (get-metric (event->gauge e) http-options)]
                      (is m)
                      (is (= (:metric e) (:value m)))
                      (is (= (round (:time e)) (:measure-time m)))))

           (testing "counter with source"
                    (let [e {:host "p" :service "q" :metric (rand-int 2048)
                             :time (unix-time)}
                          r ((:counter l) e)
                          m (get-metric (event->counter e) http-options)]
                      (is m)
                      (is (= (:metric e) (:value m)))
                      (is (= (round (:time e)) (:measure-time m)))))

           (testing "multiple counters with source"
                    (let [e0 {:host "p" :service "q" :metric (rand-int 2048)
                             :time (unix-time)}
                          e1 {:host "x" :service "y" :metric (rand-int 2048)
                             :time (unix-time)}
                          r ((:counter l) [e0 e1])]
                      (for [event [e0 e1]
                            :let [m (get-metric (event->counter event)
                                                http-options)]]
                        (do
                          (is m)
                          (is (= (:metric event) (:value m)))
                          (is (= (round (:time event)) (:measure-time m)))))))

           (testing "annotation"
                    (let [e {:service "ann test"
                             :state "down"
                             :host "testing1.tx"
                             :description (str "test " (rand))
                             :time (unix-time)}
                          r ((:annotation l) e)
                          a (annotation user api-key "ann.test" (:id r)
                                        http-options)]
                      (is a)
                      (is (= "ann test down" (:title a)))
                      (is (= "testing1.tx" (:source a)))
                      (is (= (round (:time e)) (:start-time a)))
                      (is (nil? (:end-time a)))
                      (is (= (:description e) (:description a)))))

           (testing "annotation without source"
                    (let [e {:service "ann"
                             :state "down"
                             :description (str "test " (rand))
                             :time (unix-time)}
                          r ((:annotation l) e)
                          a (annotation user api-key "ann" (:id r)
                                        http-options)]
                      (is a)
                      (is (= "ann down" (:title a)))
                      (is (= "unassigned" (:source a)))
                      (is (= (round (:time e)) (:start-time a)))
                      (is (nil? (:end-time a)))
                      (is (= (:description e) (:description a)))))

           (testing "annotation start"
                    (let [e {:service "ann"
                             :host "flaky"
                             :state "outage"
                             :description "something bad happened"
                             :time (round (unix-time))}
                          r1 ((:start-annotation l) e)
                          r2 ((:end-annotation l)
                                {:service "ann"
                                 :host "flaky"
                                 :state "ok"
                                 :description "all fine"
                                 :time (+ 5 (:time e))})
                          a (annotation user api-key "ann" (:id r1)
                                        http-options)]
                      (is a)
                      (is (= "ann outage") (:title a))
                      (is (= "flaky") (:source a))
                      (is (= "something bad happened" (:description a)))
                      (is (= (:time e) (:start-time a)))
                      (is (= (+ 5 (:time e)) (:end-time a)))))))
