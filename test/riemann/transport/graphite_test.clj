(ns riemann.transport.graphite-test
  (:use clojure.test
        riemann.transport.graphite)
  (:require [riemann.logging :as logging]))

(deftest decode-graphite-line-success-test
  (let [parser-fun nil]
    (is (= {:service "name", :metric 123.0, :time 456}
           (decode-graphite-line "name 123 456" parser-fun)))))

(deftest decode-graphite-line-failure-test
  (logging/suppress ["riemann.transport.graphite"]
  (let [parser-fun nil]
    (is (= nil (decode-graphite-line "line" parser-fun)))
    (is (= nil (decode-graphite-line "name nan 456" parser-fun)))
    (is (= nil (decode-graphite-line "name metric timestamp" parser-fun)))
    (is (= nil (decode-graphite-line "name with space 123 456" parser-fun))))))

(deftest parser-fn-test
  (let [parser-fun (fn [line] {:service "something_else"})]
    (is (= {:service "something_else", :metric 123.0, :time 456}
           (decode-graphite-line "something 123 456" parser-fun)))))
