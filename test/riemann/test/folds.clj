(ns riemann.test.folds
  (:use riemann.folds
        riemann.time
        riemann.time.controlled
        clojure.test)
  (:require incanter.stats))

(use-fixtures :once control-time!)
(use-fixtures :each reset-time!)

(deftest difference-test
         (are [es e] (= (difference es) e)
              [{:metric 1 :a 2}]
              {:metric 1 :a 2}

              [{:metric 1 :a true} {:metric 2 :b true}]
              {:metric -1 :a true}

              [{:metric 10} {:metric -7} {:metric 2}]
              {:metric 15}))

(deftest quotient-test
         (are [es e] (= (quotient es) e)
              [{:metric 1 :a true}]
              {:metric 1 :a true}

              [{:metric 1 :a true} {:metric 2 :b true}]
              {:metric 1/2 :a true}

              [{:metric 10} {:metric -7} {:metric 2}]
              {:metric (/ 10 -7 2)}))

(deftest product-test
         (are [es e] (= (product es) e)
              [{:metric 1 :a true}]
              {:metric 1 :a true}
              
              [{:metric 1 :a true} {:metric 2 :b true}]
              {:metric 2 :a true}

              [{:metric 10} {:metric -7} {:metric 2}]
              {:metric -140}))
