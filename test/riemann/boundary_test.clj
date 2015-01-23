(ns riemann.boundary-test
  (:use riemann.boundary
        [riemann.time :only [unix-time]]
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:boundary ^:integration boundary-test
  (let [b @#'riemann.boundary/boundarify
        p @#'riemann.boundary/packer-upper
        time (unix-time)]

    (testing "boundarify correctness"
      (is (function? (b)))
      (is (function? (b "org")))
      (is (= "FOO" ((b) "foo")))
      (is (= "FOO_BAR" ((b) "foo bar")))
      (is (= "FOO" ((b) "foo@")))
      (is (= "FOOBAR" ((b) "foo@bar")))
      (is (= "ORG_FOO" ((b "org") "foo")))
      (is (thrown? RuntimeException ((b) "!#@")))
      (is (thrown? RuntimeException ((b "org") "!#@"))))

    (testing "packer-upper correctness"
      (is (function? (p nil nil)))
      (is (function? (p "metric" nil)))
      (is (function? (p nil "organization")))
      (is (function? (p "metric" "organization")))
      (is (= [["host" "SERVICE" 0 time]]
             ((p nil nil) [{:host "host" :service "service"
                            :metric 0 :time time}])))
      (is (= [["host" "DEST" 0 time]]
             ((p "dest" nil) [{:host "host" :service "service"
                               :metric 0 :time time}])))
      (is (= [["host" "ORG_SERVICE" 0 time]]
             ((p nil "org") [{:host "host" :service "service"
                              :metric 0 :time time}])))
      (is (= [["host" "ORG_DEST" 0 time]]
             ((p "dest" "org") [{:host "host" :service "service"
                                 :metric 0 :time time}])))
      (is (= [["host1" "SERVICE1" 0 time] ["host2" "SERVICE2" 0 time]]
             ((p nil nil) [{:host "host1" :service "service1"
                                 :metric 0 :time time}
                                {:host "host2" :service "service2"
                                 :metric 0 :time time}]))))

    (testing "boundary (no authentication)"
      (is (function? (boundary "eml" "tkn")))
      (is (function? ((boundary "eml" "tkn"))))
      (is (function? ((boundary "eml" "tkn") :metric-id "metric")))
      (is (function? ((boundary "eml" "tkn") :organization "org")))
      (is (function? ((boundary "eml" "tkn")
                      :metric-id "metric" :organization "org")))
      (is (function? ((boundary "eml" "tkn")
                      :organization "org" :metric-id "metric"))))))
