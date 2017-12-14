(ns riemann.instrumentation-test
  (:require [riemann.instrumentation :refer :all]
            [riemann.logging :as logging]
            [riemann.time :refer [unix-time]]
            [riemann.time.controlled :refer :all]
            [clojure.test :refer :all]))

(logging/init)
(use-fixtures :each reset-time!)
(use-fixtures :once control-time!)

(deftest measure-latency-test
         (let [r (rate+latency {:service "meow"
                                :meow true}
                               [0 3/5 1.0])]
           (dotimes [i 100]
             (measure-latency r (Thread/sleep 1)))

           (advance! 5)
           (let [es (events r)]
             ; Should have merged from the original event
             (is (every? true? (map :meow es)))

             ; Should emit a rate and quantiles
             (is (= ["riemann meow rate"
                     "riemann meow latency 0"
                     "riemann meow latency 3/5"
                     "riemann meow latency 1.0"]
                    (map :service es)))

             ; Uses unix-time
             (is (every? (partial = 5) (map :time es)))

             ; Has float metrics
             (is (every? float? (map :metric es)))

             (let [quantiles (rest (map :metric es))]
             ; Quantiles are sorted
               (is (= quantiles (sort quantiles)))

               ; Quantiles are roughly in ms
               (is (every? (fn [x] (< 1 x 20)) quantiles))))))
