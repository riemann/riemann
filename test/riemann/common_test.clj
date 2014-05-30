(ns riemann.common-test
  (:use riemann.common)
  (:use clojure.test))

(deftest iso8601->unix-test
         (let [times (map iso8601->unix
                          ["2013-04-15T18:06:58-07:00"
                          "2013-04-15T18:06:58.123+11:00"
                           "2013-04-15T18:06:58Z"
                           "2013-04-15"])]
           (is (= times [1366074418
                         1366009618
                         1366049218
                         1365984000]))))

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

(deftest count-string-bytes-test
  (is (= (count-string-bytes "") 0))
  (is (= (count-string-bytes "a") 1))
  (is (= (count-string-bytes "é") 2))
  (is (= (count-string-bytes "あ") 3))
  (is (= (count-string-bytes "𠜎") 4))
  (is (= (count-string-bytes "あいう") 9)))

(deftest count-character-bytes-test
  (is (= (count-character-bytes \a) 1))
  (is (= (count-character-bytes \é) 2))
  (is (= (count-character-bytes \あ) 3)))

(deftest truncate-test
  (is (= (truncate "あいう" -1) ""))
  (is (= (truncate "あいう" 0) ""))
  (is (= (truncate "あいう" 1) "あ"))
  (is (= (truncate "あいう" 3) "あいう"))
  (is (= (truncate "あいう" 4) "あいう")))

(deftest truncate-bytes-test
  (is (= (truncate-bytes "あいう" -1) ""))
  (is (= (truncate-bytes "あいう" 0) ""))
  (is (= (truncate-bytes "あいう" 1) ""))
  (is (= (truncate-bytes "あいう" 3) "あ"))
  (is (= (truncate-bytes "あいう" 4) "あ"))
  (is (= (truncate-bytes "あいう" 9) "あいう"))
  (is (= (truncate-bytes "あいう" 10) "あいう")))
