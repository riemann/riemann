(ns riemann.test.folds
  (:use riemann.folds
        riemann.time
        riemann.time.controlled
        clojure.test)
  (:require incanter.stats))

(use-fixtures :once control-time!)
(use-fixtures :each reset-time!)

(deftest sorted-sample-test
         (are [es e] (= (sorted-sample-extract es [0 0.5 1]) e)
              []
              []

              [{:metric nil}]
              []

              [{:metric 1}]
              [{:metric 1} {:metric 1} {:metric 1}]

              [{:metric 2} {:metric 1}]
              [{:metric 1} {:metric 2} {:metric 2}]

              [{:metric 3} {:metric 1} {:metric 2}]
              [{:metric 1} {:metric 2} {:metric 3}]
              
              [{:metric 6} {:metric 1} {:metric 2} {:metric 1} {:metric 1}]
              [{:metric 1} {:metric 1} {:metric 6}]))

(defn test-fold-common
  [fold operator]
  (are [es] (= (fold es) 
               (assoc (first es)
                      :metric
                      (reduce operator (map :metric es))))
       [{:metric 1 :a true}]
       [{:metric 1 :a true} {:metric 2 :b true}]
       [{:metric 1 :a true} {:metric 2 :b true} {:metric 7 :c true}]
       [{:metric 1 :a true} {:metric 2 :b true} {:metric 7 :c true}
        {:metric 7 :d true}]))

(defn test-fold
  "Tests a fold which is equivalent to operator."
  [fold operator]
  (is (nil? (fold [])))
  (is (nil? (fold [nil])))
  (is (= {:service "foo"
          :metric (operator)}
         (fold [{:service "foo" :metric nil} {}])))
  (are [es] (= (fold es)
              (assoc (first es)
                     :metric
                     (reduce operator
                             (keep :metric es))))
       [{:a true}]
       [{:a true :metric 2} nil]
       [{:a true :metric 2} nil {:b true :metric 7} {:c true}])
  (test-fold-common fold operator))

(defn test-fold-all
  "Tests a fold which is equivalent to an operator where the first argument
  is mandatory."
  [fold operator]
  (is (nil? (fold [])))
  (is (nil? (fold [nil {:service "foo"}])))
  (is (= {:service "foo"
          :metric nil 
          :description "An event or metric was nil."}
         (fold [{:service "foo"} {:metric 2}])))
  (is (= {:service "foo"
          :metric nil
          :description "An event or metric was nil."}
         (fold [{:service "foo" :metric 2}
                nil])))
  (test-fold-common fold operator))

(deftest sum-test
         (test-fold sum +))

(deftest product-test
         (test-fold product *))

(deftest difference-test
         (test-fold-all difference -))

(deftest quotient-test
         (test-fold-all quotient /)

         (testing "exceptions"
                  (is (= (quotient [{:service "hi" :metric 1} 
                                   {:metric 2} 
                                   {:metric 0}])
                         {:service "hi"
                          :metric nil
                          :description "Can't divide by zero"}))
                  (is (= (quotient [{:service "hi" :metric 2} {:service "bar"}])
                         {:service "hi"
                          :metric nil
                          :description "An event or metric was nil."}))))

(deftest quotient-sloppy-test
         (is (= (quotient-sloppy [{:metric 2} {:metric -3}])
                {:metric -2/3}))
         (is (= (quotient-sloppy [{:metric 0 :a true} {:metric 0}])
                {:metric 0 :a true})))

