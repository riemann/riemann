(ns riemann.test.common
  (:use riemann.common)
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

(deftest subject-test
         (let [s #'subject]
           (are [events subject] (= (s events) subject)
                [] ""
                
                [{}] ""
                
                [{:host "foo"}] "foo"
                
                [{:host "foo"} {:host "bar"}] "foo and bar"
                
                [{:host "foo"} {:host "bar"} {:host "baz"}]
                "foo, bar, baz"
                
                [{:host "foo"} {:host "baz"} {:host "bar"} {:host "baz"}]
                "foo, baz, bar"

                [{:host 1} {:host 2} {:host 3} {:host 4} {:host 5}]
                "5 hosts"
                
                [{:host "foo" :state "ok"}] "foo ok"
                
                [{:host "foo" :state "ok"} {:host "bar" :state "ok"}] 
                "foo and bar ok"
               
                [{:host "foo" :state "error"} {:host "bar" :state "ok"}]
                "foo and bar error and ok"
                )))
