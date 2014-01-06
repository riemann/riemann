(ns riemann.time-test
  (:use riemann.time
        [riemann.common :exclude [unix-time linear-time]]
        clojure.math.numeric-tower
        clojure.test
        clojure.tools.logging)
  (:require [riemann.logging :as logging]))

(riemann.logging/init)

(defn reset-time!
  [f]
  (stop!)
  (reset-tasks!)
  (start!)
  (f)
  (stop!)
  (reset-tasks!))
(use-fixtures :each reset-time!)

(deftest next-tick-test
         (are [anchor dt now next] (= (next-tick anchor dt now) next)
              0 1 0 1
              0 2 0 2
              1 1 0 1
              2 1 0 1
              0 2 0 2
              0 2 1 2
              0 2 2 4
              2 2 2 4
              4 2 2 4
              1 2 1 3
              1 2 2 3
              1 2 3 5))

(deftest ^:time clock-test
         (is (approx-equal (/ (System/currentTimeMillis) 1000)
                           (unix-time))))

(deftest ^:time once-test
         "Run a function once, to verify that the threadpool works at all."
         (let [t0 (unix-time)
               results (atom [])]
           (after! 0.1 #(swap! results conj (- (unix-time) t0)))
           (Thread/sleep 300)
           (is (<= 0.085 (first @results) 0.115))))

; LMAO if this test becomes hilariously unstable and/or exhibits genuine
; heisenbugs for any unit of time smaller than 250ms.
(deftest ^:time defer-cancel-test
         (let [x1 (atom 0)
               x2 (atom 0)
               t1 (every! 1 (fn [] (swap! x1 inc)))
               t2 (every! 1 1 #(swap! x2 inc))]
           (Thread/sleep 500)
           (is (= 1 @x1))
           (is (= 0 @x2))

           (Thread/sleep 1000)
           (is (= 2 @x1))
           (is (= 1 @x2))

           ; Defer
           (defer t1 1.5)
           (Thread/sleep 1000)
           (is (= 2 @x1))
           (is (= 2 @x2))

           (Thread/sleep 1000)
           (is (= 3 @x1))
           (is (= 3 @x2))

           ; Cancel
           (cancel t2)
           (Thread/sleep 1000)
           (is (= 4 @x1))
           (is (= 3 @x2))))

(deftest ^:time exception-recovery-test
         (let [x (atom 0)]
           (every! 0.1 (fn [] (swap! x inc) (/ 1 0)))
           (Thread/sleep 150)
           (is (= 2 @x))))

(defn mapvals
  [f kv]
  (into {} (map (fn [[k v]] [k (f v)]) kv)))

(defn pairs
  [coll]
  (partition 2 1 coll))

(defn differences
  [coll]
  (map (fn [[x y]] (- y x)) (pairs coll)))

(deftest ^:time periodic-test
         "Run one function periodically."
         (let [results (atom [])]
           ; For a wide variety of intervals, start periodic jobs to record
           ; the time.
           (doseq [interval (range 1/10 5 1/10)]
             (every! interval #(swap! results conj [interval (unix-time)])))

           (Thread/sleep 20000)
           (stop!)

           (let [groups (mapvals (fn [vs] (map second vs))
                                 (group-by first @results))
                 differences (mapvals differences groups)]
             (doseq [[interval deltas] differences]
               ; First delta will be slightly smaller because the scheduler
               ; computed an absolute time in the *past*
               (is (<= -0.025 (- (first deltas) interval) 0))

               (let [deltas (drop 1 deltas)]
                 ; Remaining deltas should be accurate to within 5ms.
                 (is (every? (fn [delta]
                               (< -0.05 (- delta interval) 0.05)) deltas))
                 ; and moreover, there should be no cumulative drift.
                 (is (< -0.005 
                        (- (/ (reduce + deltas) (count deltas)) interval)
                        0.005)))))))
