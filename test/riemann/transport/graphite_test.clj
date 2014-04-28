(ns riemann.transport.graphite-test
  (:use clojure.test
        [riemann.common :only [event]]
        riemann.transport.graphite
        [slingshot.slingshot :only [try+]])
  (:require [riemann.logging :as logging]))

(deftest decode-graphite-line-success-test
  (is (= (event {:service "name", :metric 123.0, :time 456})
         (decode-graphite-line "name 123 456")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name\t456\t789")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name\t 456\t 789")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name\t\t456\t\t789")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name\t\t456 789")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name 456\t789")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name\t456 789")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name  456      789"))))

(deftest decode-graphite-line-failure-test
  (let [err #(try+ (decode-graphite-line %)
                   (catch Object e e))]
    (is (= (err "") "blank line"))
    (is (= (err "name nan 456") "NaN metric"))
    (is (= (err "name metric 456") "invalid metric"))
    (is (= (err "name 123 timestamp") "invalid timestamp"))
    (is (= (err "name with space 123 456") "too many fields"))
    (is (= (err "name\twith\ttab\t123\t456") "too many fields"))
    (is (= (err "name with space\tand\ttab 123\t456") "too many fields"))
    (is (= (err "name\t\t123\t456\t\t\t789") "too many fields"))))
