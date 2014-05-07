(ns riemann.testing
  "Functions and Macros for testing Riemann configs"
  (:use clojure.test
        riemann.streams)
  (:require [riemann.streams-test :refer [run-stream run-stream-intervals]]))

(defmacro simple-test [name stream data & assertions]
  `(deftest ^:usertest ~name
     (reset! probe-values {})
     (binding [testing-mode true]
       (run-stream ~stream ~data)
       (let []
         (testing "expected values"
           ~@assertions)))))

(defmacro time-test [name stream data & assertions]
  `(deftest ^:usertest ~name
     (reset! probe-values {})
     (binding [testing-mode true]
       (run-stream-intervals ~stream ~data)
       (let []
         (testing "expected values"
           ~@assertions)))))

(defmacro config-test [name core data & assertions]
  `(deftest ~name
     (reset! probe-values {})
     (binding [testing-mode true]
       (doseq [stream# (get (deref ~core) :streams)
               input# ~data]
         (stream# input#))
       (testing "expected values"
         ~@assertions))))
