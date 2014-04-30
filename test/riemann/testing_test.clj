(ns riemann.testing-test
  (:use riemann.testing
        clojure.test
        riemann.time.controlled
        riemann.time
        riemann.streams
        [riemann.config :only [next-core streams]]))

(use-fixtures :once control-time!)
(use-fixtures :each reset-time!)

(simple-test simple-case
             (probe :foo)
             [1 2 3 4]
             (is (= (:foo @probe-values)
                    [1 2 3 4])))

(simple-test reset-works
             (probe :foo)
             [1 2 3 4]
             (is (= (:foo @probe-values)
                    [1 2 3 4])))
(simple-test multiple
             (probe :foo
                    (probe :bar))
             [1 2 3 4]
             (is (= (:foo @probe-values)
                    [1 2 3 4]))
             (is (= (:bar @probe-values)
                    [1 2 3 4])))

(time-test simple-time
           (stable 10 :x
                   (probe :foo))
           [{:x 0 :time 0} 1
            {:x 1 :time 1} 10
            {:x 2 :time 11} 1]
           (is (= (:foo @probe-values)
                  [{:x 1 :time 1}])))


(streams
 (where (not (service #"^riemann"))
        (probe :not-riemann)))

(config-test where-test next-core
             [{:service "database"}
              {:service "riemann"}]
             (is (= (:not-riemann @probe-values)
                    [{:service "database"}])))
