(ns riemann.test.time.controlled
  (:use riemann.time.controlled
        riemann.time
        [riemann.common :except [unix-time]]
        clojure.math.numeric-tower
        clojure.test)
  (:require [riemann.logging :as logging]))

(deftest clock-test
         (reset-time!)
         (is (= (unix-time-controlled) 0))
         (advance! -1)
         (is (= (unix-time-controlled) 0))
         (advance! 4.5)
         (is (= (unix-time-controlled) 4.5))
         (reset-time!)
         (is (= (unix-time-controlled) 0)))

(deftest ^:focus once-test
         (reset-time!)
         (let [x (atom 0)
               once1 (once! #(swap! x inc) 1)
               once2 (once! #(swap! x inc) 2)
               once3 (once! #(swap! x inc) 3)]

           (advance! 0.5)
           (is (= @x 0))

           (advance! 2)
           (is (= @x 2))

           (cancel once3)
           (advance! 3)
           (is (= @x 2))))

(deftest every-test
         (reset-time!)
         (let [x (atom 0)
               bump #(swap! x inc)
               task (every! bump 2 1)]

           (is (= @x 0))

           (advance! 1)
           (is (= @x 0))

           (advance! 2)
           (is (= @x 1))

           (advance! 3)
           (is (= @x 2))

           (advance! 4)
           (is (= @x 3))

           ; Double-down
           (defer task 1)
           (is (= @x 3))
           (advance! 5)
           (is (= @x 8))

           ; Into the future!
           (defer task 9)
           (advance! 8)
           (is (= @x 8))
           (advance! 9)
           (is (= @x 9))
           (advance! 10)
           (is (= @x 10))))
