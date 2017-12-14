(ns riemann.graphite-test
  (:require [riemann.common :refer [event]]
            [riemann.client :as client]
            [riemann.core :refer [transition! core stop! wrap-index]]
            [riemann.graphite :refer :all]
            [riemann.index :as index]
            [riemann.logging :as logging]
            [riemann.time :refer [unix-time]]
            [riemann.transport.tcp :refer [tcp-server]]
            [riemann.transport.graphite :refer [graphite-server]]
            [clojure.test :refer :all]
            [clojure.tools.logging :refer :all]))

(logging/init)

(deftest graphite-server-test
  (logging/suppress
   ["riemann.transport" "riemann.core" "riemann.pubsub" "riemann.graphite"]
   (let [s1       (graphite-server)
         s2       (tcp-server)
         index    (wrap-index (index/index))
         core     (transition!
                   (core)
                   {:index    index
                    :services [s1 s2]
                    :streams  [index]})
         sendout! (graphite {:path graphite-path-basic})
         client   (client/tcp-client)]
     (try
       (sendout! {:service "service1" :metric 1.0 :time 0})
       (sendout! {:service "service2" :metric 1.0 :time 0})
       (Thread/sleep 100)
       (let [[r1 r2] @(client/query client "true")]
         (is (and (#{"service1" "service2"} (:service r1))
                  (= 1.0 (:metric r1))))
         (is (and (#{"service1" "service2"} (:service r2))
                  (= 1.0 (:metric r2)))))
       (finally
         (client/close! client)
         (stop! core))))))

(deftest parse-error-test
  (logging/suppress
    ["riemann.transport" "riemann.core" "riemann.pubsub" "riemann.graphite"]
    (let [server    (graphite-server)
          trap      (promise)
          core      (transition! (core)
                                 {:services [server]
                                  :streams [(partial deliver trap)]})
          client    (open (->GraphiteTCPClient "localhost" 2003))]
      (try
        (send-line client "too many spaces 1.23 456\n")
        (send-line client "valid 1.34 456\n")
        (is (= (deref trap 1000 :timeout)
               (event {:service "valid" :metric 1.34 :time 456})))
        (finally
          (close client)
          (stop! core))))))

(deftest percentiles
         (is (= (graphite-path-percentiles
                  {:service "foo bar"})
                "foo.bar"))
         (is (= (graphite-path-percentiles
                  {:service "foo bar 1"})
                "foo.bar.1"))
         (is (= (graphite-path-percentiles
                  {:service "foo bar 99"})
                "foo.bar.99"))
         (is (= (graphite-path-percentiles
                  {:service "foo bar 0.99"})
                "foo.bar.99"))
         (is (= (graphite-path-percentiles
                  {:service "foo bar 0.999"})
                "foo.bar.999")))

(deftest graphite-metric-test
  (is (= (graphite-metric
           {:metric 1000})
         1000))
  (is (= (graphite-metric
           {:metric -2})
         -2))
  (is (= (graphite-metric
           {:metric 8500000001})
         8500000001))
  (is (= (graphite-metric
           {:metric 2/3})
         (double 2/3)))
  (is (= (graphite-metric
           {:metric 3.14159})
         (double 3.14159))))

(deftest ^:graphite ^:integration graphite-test
         (let [g (graphite {:block-start true})]
           (g {:host "riemann.local"
               :service "graphite test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric -2
               :time (unix-time)}))

         (let [g (graphite {:block-start true})]
           (g {:service "graphite test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [g (graphite {:block-start true})]
           (g {:host "no-service.riemann.local"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 4
               :time (unix-time)})))
