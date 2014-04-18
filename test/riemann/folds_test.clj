(ns riemann.folds-test
  (:refer-clojure :exclude [count])
  (:use [riemann.common :only [event]]
        riemann.folds
        riemann.time
        riemann.time.controlled
        clojure.test))

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

(deftest mean-test
    (is (= {:metric 4} (mean [{:metric 2} {:metric 4} {:metric 6}])))
    (is (= {:metric 4} (mean [{:metric 2} {:metric 4} {:metric nil} {:metric 6}])))
    (is (= {:metric 4} (mean [{:metric 2} {:metric 4} {:metric nil} {:metric 6} nil])))
    (is (= nil (mean [])))
    (testing "avoid divide by zero when only events with nil metrics"
      (is (= nil (mean [{:metric nil}])))))

(deftest std-dev-test
    (is (= 147.0 (Math/floor (:metric (std-dev [{:metric 600} {:metric 470} {:metric 170} {:metric 430} {:metric 300}])))))
    (is (= 147.0 (Math/floor (:metric (std-dev [{:metric 600} {:metric nil} {:metric 470} {:metric 170} {:metric 430} {:metric 300}]))))))

(deftest count-test
  (advance! 1)
  (let [synthetic-event (count nil)
        c-with-time (count [{:metric 5 :time 5} {:metric 3 :time 3}])
        c2 (count [{:metric 2} {:metric 3}])
        c3 (count [{:metric 2} {:metric 3 :state "expired"} {:metric 4 :ttl 1 :time -3}])]
    (is (= synthetic-event (event {:metric 0})))
    (is (= 1 (:time synthetic-event)))
    (is (= c-with-time {:metric 2 :time 5}))
    (is (= c2 {:metric 2}))
    (is (= c3 {:metric 3}))))

(deftest minimum-test
  (is (= nil          (minimum nil)))
  (is (= nil          (minimum [{:metric nil}])))
  (is (= {:metric 2}  (minimum [{:metric 2}])))
  (is (= {:metric -4} (minimum [{:metric 2} {:metric 5} {:metric -4}
                                {:metric 0}]))))
(deftest maximum-test
  (is (= nil          (maximum nil)))
  (is (= nil          (maximum [{:metric nil}])))
  (is (= {:metric 2}  (maximum [{:metric 2}])))
  (is (= {:metric 5}  (maximum [{:metric 2} {:metric 5} {:metric -4}
                                {:metric 0}]))))
