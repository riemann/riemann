(ns riemann.test-test
  "Who tests the testers?"
  (:require [riemann.test :refer [tap
                                  io
                                  with-test-env
                                  inject!
                                  query-index
                                  clear-index
                                  lookup-index
                                  expire-index]]
            [riemann.time.controlled :refer [control-time!
                                             reset-time!]]
            [clojure.test :refer :all]
            [riemann.streams :refer :all]))

(use-fixtures :once control-time!)
(use-fixtures :each reset-time!)

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
             (is (= (inject! [s]
                             [{:time 0 :metric 0}
                              {:time 1 :metric 1}
                              {:time 2 :metric 2}
                              {:time 3 :metric 3}
                              {:time 4 :metric 4}
                              {:time 5 :metric 5}])
                    {:cask [{:time 5 :metric 2}]}))
             (is (= @downstream {:time 5 :metric 2}))))))))

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

(deftest clear-index-test
  (with-test-env
    (bound
      (eval
        '(do
           (ns riemann.test-test)
           (let [s (riemann.config/index)]
             (inject! [s]
                      [{:host "foo" :service "a" :time 0 :metric 0 :ttl 60}
                       {:host "foo" :service "b" :time 1 :metric 1 :ttl 60}
                       {:host "foo" :service "c" :time 2 :metric 2 :ttl 60}])
             (is (= (count (query-index "true")) 3))
             (clear-index)
             (is (= (count (query-index "true")) 0))))))))

(deftest query-index-test
  (with-test-env
    (bound
      (eval
        '(do
           (ns riemann.test-test)
           (let [s (riemann.config/index)]
             (inject! [s]
                      [{:host "foo" :service "a" :time 0 :metric 0 :ttl 60}
                       {:host "foo" :service "b" :time 1 :metric 1 :ttl 60}
                       {:host "foo" :service "c" :time 2 :metric 2 :ttl 60}])
             (is (= (query-index "true")
                    [{:host "foo" :service "a" :time 0 :metric 0 :ttl 60}
                     {:host "foo" :service "b" :time 1 :metric 1 :ttl 60}
                     {:host "foo" :service "c" :time 2 :metric 2 :ttl 60}]))
             (is (= (query-index "service = \"a\"")
                    [{:host "foo" :service "a" :time 0 :metric 0 :ttl 60}]))
             (clear-index)
             (inject! [s]
                      [{:host "z" :service "b" :time 1 :metric 0 :ttl 60}
                       {:host "b" :service "a" :time 2 :metric 1 :ttl 60}
                       {:host "z" :service "b" :time 3 :metric 1 :ttl 60}
                       {:host "z" :service "a" :time 5 :metric 1 :ttl 60}
                       {:host "b" :service "b" :time 2 :metric 1 :ttl 60}
                       {:host "b" :service "b" :time 3 :metric 1 :ttl 60}])
             (is (= (query-index "true")
                    [{:host "b" :service "a" :time 2 :metric 1 :ttl 60}
                     {:host "b" :service "b" :time 3 :metric 1 :ttl 60}
                     {:host "z" :service "a" :time 5 :metric 1 :ttl 60}
                     {:host "z" :service "b" :time 3 :metric 1 :ttl 60}]))))))))

(deftest lookup-index-test
  (with-test-env
    (bound
      (eval
        '(do
           (ns riemann.test-test)
           (let [s (riemann.config/index)]
             (inject! [s]
                      [{:host "foo" :service "a" :time 0 :metric 0 :ttl 60}
                       {:host "foo" :service "b" :time 1 :metric 1 :ttl 60}
                       {:host "foo" :service "c" :time 2 :metric 2 :ttl 60}])
             (is (= (count (query-index "true")) 3))
             (is (= (lookup-index "foo" "a")
                    {:host "foo" :service "a" :time 0 :metric 0 :ttl 60}))
             (is (= (lookup-index "foo" "b")
                    {:host "foo" :service "b" :time 1 :metric 1 :ttl 60}))
             (is (= (lookup-index "foo" "c")
                    {:host "foo" :service "c" :time 2 :metric 2 :ttl 60}))))))))

(deftest expire-index-test
  (with-test-env
    (bound
      (eval
        '(do
           (ns riemann.test-test)
           (let [s (riemann.config/index)]
             (inject! [s]
                      [{:host "foo" :service "a" :time 0 :metric 0 :ttl 60}
                       {:host "foo" :service "b" :time 0 :metric 1 :ttl 60}
                       {:host "foo" :service "c" :time 0 :metric 2 :ttl 60}
                       {:host "foo" :service "d" :time 61 :metric 3 :ttl 60}])
             (is (= (count (query-index "true")) 4))
             (is (= (expire-index)
                    [{:host "foo" :service "a" :time 61 :state "expired"}
                     {:host "foo" :service "b" :time 61 :state "expired"}
                     {:host "foo" :service "c" :time 61 :state "expired"}]))
             (is (= (count (query-index "true")) 1))
             (is (= (lookup-index "foo" "d")
                    {:host "foo" :service "d" :time 61 :metric 3 :ttl 60}))
             (inject! [s]
                      [{:host "foo" :service "a" :time 60 :metric 0 :ttl 60}
                       {:host "foo" :service "b" :time 60 :metric 1 :ttl 60}
                       {:host "foo" :service "c" :time 60 :metric 2 :ttl 60}
                       {:host "foo" :service "d" :time 121 :metric 3 :ttl 60}])
             (is (= (count (query-index "true")) 4))
             (is (= (expire-index {:keep-keys [:host :service :metric]})
                    [{:host "foo" :service "a" :metric 0 :time 121 :state "expired"}
                     {:host "foo" :service "b" :metric 1 :time 121 :state "expired"}
                     {:host "foo" :service "c" :metric 2 :time 121 :state "expired"}]))))))))

(deftest index-captures-events
  (with-test-env
    (bound
      (eval
        '(do
           (ns riemann.test-test)
           (let [s (riemann.config/index)]
             (inject! [s]
                      [{:host "foo" :service "a" :time 0 :metric 0 :ttl 60}
                       {:host "foo" :service "b" :time 1 :metric 1 :ttl 60}
                       {:host "foo" :service "c" :time 2 :metric 2 :ttl 60}])
             (is (= (query-index "true")
                    [{:host "foo" :service "a" :time 0 :metric 0 :ttl 60}
                     {:host "foo" :service "b" :time 1 :metric 1 :ttl 60}
                     {:host "foo" :service "c" :time 2 :metric 2 :ttl 60}]))
             (inject! [s]
                      [{:host "foo" :service "a" :time 5 :metric 4 :ttl 60}
                       {:host "foo" :service "d" :time 1 :metric 1 :ttl 60}])
             (is (= (query-index "true")
                    [{:host "foo" :service "a" :time 5 :metric 4 :ttl 60}
                     {:host "foo" :service "b" :time 1 :metric 1 :ttl 60}
                     {:host "foo" :service "c" :time 2 :metric 2 :ttl 60}
                     {:host "foo" :service "d" :time 1 :metric 1 :ttl 60}]))))))))

