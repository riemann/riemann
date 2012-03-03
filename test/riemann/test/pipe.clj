(ns riemann.test.pipe
  (:use [riemann.streams])
  (:use [riemann.common])
  (:use [clojure.test])
  (:use [clojure.pprint]))

(deftest pipe-test
         (pprint (macroexpand `(pipe (alpha :foo) [(a) (b)] [c (d)]))))
