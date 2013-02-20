(ns riemann.test.folds
  (:use riemann.folds
        riemann.time
        clojure.test)
  (:require incanter.stats))

(use-fixtures :once control-time!)
(use-fixtures :each reset-time!)

(deftest difference-test
         (are [es e] (= (subtract es) e)
              [{:metric 1 :a 2}]
              {:metric 1 :a 2}

              [{:metric 1 :a true}
               {:metric 2 :b true}]
              {:metric -1 :a true}

              [{:metric 10} {:metric -7} {:metric 2}]
              {:metric 15}))
