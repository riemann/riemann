(ns riemann.test-test
  "Who tests the testers?"
  (:use clojure.test
        riemann.test
        riemann.streams))

(deftest only-one-tap-per-context
  (let [err (try
              (with-test-env
                (eval
                  `(do (tap :foo prn)
                       (tap :bar prn)
                       (tap :foo nil))))
              (catch RuntimeException e
                (.getMessage e)))]
    (is (re-find #"Tap :foo \(.+?:\) already defined at :" err))))

(deftest tap-captures-events
  (with-test-env
    (eval
      '(do
         (use 'riemann.test 'riemann.streams 'clojure.test)
         (let [downstream (promise)
               s (rate 5 (tap :cask (partial deliver downstream)))]
           (is (= (run! [s]
                             [{:time 0 :metric 0}
                              {:time 1 :metric 1}
                              {:time 2 :metric 2}
                              {:time 3 :metric 3}
                              {:time 4 :metric 4}
                              {:time 5 :metric 5}])
                  {:cask [{:time 5 :metric 2}]}))
           (is (= @downstream {:time 5 :metric 2})))))))

(deftest io-suppression
  (with-test-env
    (eval
      '(do
         (use 'riemann.test 'riemann.streams 'clojure.test)
         (let [downstream (promise)
               s (sdo (io (partial deliver downstream)))]
           (run! [s] [{:time 2 :metric 0}])
           (is (nil? (deref downstream 0 nil))))))))
