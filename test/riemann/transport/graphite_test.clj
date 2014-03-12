(ns riemann.transport.graphite-test
  (:use clojure.test
        [riemann.common :only [event]]
        riemann.transport.graphite
        [slingshot.slingshot :only [try+]])
  (:require [riemann.logging :as logging]))

(deftest decode-graphite-line-success-test
  (is (= (event {:service "name", :metric 123.0, :time 456})
         (decode-graphite-line "name 123 456"))))

(deftest decode-graphite-line-failure-test
  (let [err #(try+ (decode-graphite-line %)
                   (catch Object e e))]
    (is (= (err "") "blank line"))
    (is (= (err "name nan 456") "NaN metric"))
    (is (= (err "name metric 456") "invalid metric"))
    (is (= (err "name 123 timestamp") "invalid timestamp"))
    (is (= (err "name with space 123 456") "too many fields"))))
