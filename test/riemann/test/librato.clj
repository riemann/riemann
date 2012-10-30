(ns riemann.test.librato
  (:use riemann.librato
        riemann.common
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
  [gauge]
  (-> (metric user api-key (:name gauge)
              {:end-time (:measure-time gauge)
               :count 1
               :resolution 1})
    :measurements
    (get (or (:source gauge) "unassigned"))
    (first)))

(deftest ^:librato ^:integration librato-metrics-test
         (let [l (librato-metrics user api-key)]
           (testing "gauge with source"
                    (let [e {:host "a" :service "b" :metric (rand) 
                             :time (unix-time)}
                          r ((:gauge l) e)
                          m (get-metric (event->gauge e))]
                      (is m)
                      (is (= (:metric e) (:value m)))
                      (is (= (round (:time e)) (:measure-time m)))))

           (testing "gauge without source"
                    (let [e {:service "sourceless" :metric (rand) 
                             :time (unix-time)}
                          r ((:gauge l) e)
                          m (get-metric (event->gauge e))]
                      (is m)
                      (is (= (:metric e) (:value m)))
                      (is (= (round (:time e)) (:measure-time m)))))

           (testing "annotation"
                    (let [e {:service "ann test" 
                             :state "down" 
                             :host "testing1.tx"
                             :description (str "test " (rand)) 
                             :time (unix-time)}
                          r ((:annotation l) e)
                          a (annotation user api-key "ann.test" (:id r))]
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
                          a (annotation user api-key "ann" (:id r))]
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
                          a (annotation user api-key "ann" (:id r1))]
                      (is a)
                      (is (= "ann outage") (:title a))
                      (is (= "flaky") (:source a))
                      (is (= "something bad happened" (:description a)))
                      (is (= (:time e) (:start-time a)))
                      (is (= (+ 5 (:time e)) (:end-time a)))))))
