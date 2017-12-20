(ns riemann.test-test
  "Who tests the testers?"
  (:require [riemann.streams :refer :all]
            [riemann.test :refer [tap io with-test-env inject! fresh-results *results* *taps*]]
            [riemann.time.controlled :refer :all]
            [clojure.test :refer :all]))

(defmacro bound
  "Invokes body in a bound fn. Tests may run without a binding context, which
  breaks eval + ns."
  [& body]
  `((bound-fn [] ~@body)))

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
    (bound
      (eval
        '(do
           (ns riemann.test-test)
           (let [downstream (promise)
                 s (rate 5 (tap :cask (partial deliver downstream)))]
             (with-controlled-time!
               (reset-time!)
               (binding [*results* (fresh-results @*taps*)]
                         (is (= (inject! [s]
                                         [{:time 0 :metric 0}
                                          {:time 1 :metric 1}
                                          {:time 2 :metric 2}
                                          {:time 3 :metric 3}
                                          {:time 4 :metric 4}
                                          {:time 5 :metric 5}])
                                {:cask [{:time 5 :metric 2}]}))
                         (is (= @downstream {:time 5 :metric 2}))))))))))

(deftest io-suppression
  (with-test-env
    (bound
      (eval
        '(do
           (ns riemann.test-test)
           (let [downstream (promise)
                 s (sdo (io (partial deliver downstream)))]
             (inject! [s] [{:time 2 :metric 0}])
             (is (nil? (deref downstream 0 nil)))))))))
