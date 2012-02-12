(ns reimann.test.common
  (:use reimann.common)
  (:use clojure.test))

(deftest subset-test
         (are [a b] (subset? a b)
              []    []
              []    [1 2 3]
              [1]   [1]
              [1]   [2 1 3]
              [1 2] [1 2 3]
              [1 2] [2 3 1])
         
         (are [a b] (not (subset? a b))
              [1]   []
              [1 2] [1]
              [1]   [2]
              [1]   [2 3]
              [1 2] [1 3]
              [1 2] [3 1]))

(deftest overlap-test
         (are [a b] (overlap? a b)
              [1 2] [1]
              [1]   [1]
              [1 2] [2 3]
              [3 2] [1 3]
              [1 3] [3 1])
         
         (are [a b] (not (overlap? a b))
              []     []
              [1]    []
              [1]    [2]
              [3]    [1 2]
              [1 2]  [3 4]))

(deftest disjoint-test
         (are [a b] (disjoint? a b)
              []     []
              [1]    []
              [1]    [2]
              [3]    [1 2]
              [1 2]  [3 4])
         
         (are [a b] (not (disjoint? a b))
              [1 2] [1]
              [1]   [1]
              [1 2] [2 3]
              [3 2] [1 3]
              [1 3] [3 1]))
