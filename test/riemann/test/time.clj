(ns riemann.test.time
  (:use riemann.time
        [riemann.common :exclude [unix-time]]
        clojure.math.numeric-tower
        clojure.test)
  (:require [riemann.logging :as logging]))

(riemann.logging/init)

(deftest clock-test
         (is (approx-equal (/ (System/currentTimeMillis) 1000)
                           (unix-time))))

(deftest once-test
         "Run a function once, to verify that the threadpool works at all."
         (let [t0 (unix-time)
               results (atom [])]
           (start!)
           (after! 0.1 #(swap! results conj (- (unix-time) t0)))
           (Thread/sleep 300)
           (stop!)
           (is (approx-equal (first @results) 1/10 0.02))))
