(ns riemann.boundary-test
  (:require [riemann.boundary :refer :all]
            [riemann.logging :as logging]
            [riemann.test-utils :refer [with-mock]]
            [riemann.time :refer [unix-time]]
            [clj-http.client :as client]
            [clojure.test :refer :all]))

(logging/init)

(deftest ^:boundary boundarify-test
  (let [b @#'riemann.boundary/boundarify]
    (testing "boundarify correctness"
      (is (= "FOO"     (b "foo")))
      (is (= "FOO_BAR" (b "foo bar")))
      (is (= "FOO"     (b "foo@")))
      (is (= "FOOBAR"  (b "foo@bar")))
      (is (= "ORG_FOO" (b "foo" "org")))
      (is (thrown? RuntimeException (b "!#@")))
      (is (thrown? RuntimeException (b "!#@" "org"))))))

(deftest ^:boundary packer-upper-test
  (let [p @#'riemann.boundary/packer-upper
        time (unix-time)]
    (testing "packer-upper correctness"
      (is (function? (p {:metric-id nil :org nil})))
      (is (function? (p {:metric-id "metric" :org nil})))
      (is (function? (p {:metric-id nil :org "org"})))
      (is (function? (p {:metric-id "metric" :org "org"})))
      (is (= [["host" "SERVICE" 0 time]]
             ((p {:metric-id nil :org nil})
              [{:host "host" :service "service"
                :metric 0 :time time}])))
      (is (= [["host" "DEST" 0 time]]
             ((p {:metric-id "dest" :org nil})
              [{:host "host" :service "service"
                :metric 0 :time time}])))
      (is (= [["host" "ORG_SERVICE" 0 time]]
             ((p {:metric-id nil :org "org"})
              [{:host "host" :service "service"
                :metric 0 :time time}])))
      (is (= [["host" "ORG_DEST" 0 time]]
             ((p {:metric-id "dest" :org "org"})
              [{:host "host" :service "service"
                :metric 0 :time time}])))
      (is (= [["host1" "SERVICE1" 0 time] ["host2" "SERVICE2" 0 time]]
             ((p {})
              [{:host "host1" :service "service1"
                :metric 0 :time time}
               {:host "host2" :service "service2"
                :metric 0 :time time}]))))))

(deftest ^:boundary boundary-test
  (testing "boundary"
    (is (function? ((boundary "eml" "tkn") {})))
    (is (function? ((boundary "eml" "tkn") {})))
    (is (function? ((boundary "eml" "tkn") {:metric-id "metric"})))
    (is (function? ((boundary "eml" "tkn") {:org "org"})))
    (is (function? ((boundary "eml" "tkn")
                    {:metric-id "metric" :org "org"})))
    (is (function? ((boundary "eml" "tkn")
                    {:org "org" :metric-id "metric"}))))

  (testing "boundary (mocked)"
    (with-mock [calls client/post]
      (let [bdry (boundary "eml" "tkn")
            default {:scheme :https
                     :basic-auth ["eml" "tkn"]
                     :headers {"Content-Type" "application/json"}}]

        ((bdry) [{:host "host" :service "service"
                  :metric 0 :time 123}])
        (is (= 1 (count @calls)))
        (is (= (last @calls)
               ["https://premium-api.boundary.com/v1/measurements"
                (merge default {:body "[[\"host\",\"SERVICE\",0,123]]"})]))

        ((bdry {:org "org"}) [{:host "host" :service "service"
                               :metric 0 :time 123}])
        (is (= (last @calls)
               ["https://premium-api.boundary.com/v1/measurements"
                (merge default {:body "[[\"host\",\"ORG_SERVICE\",0,123]]"})]))

        ((bdry {:metric-id "metric"}) [{:host "host" :service "service"
                                        :metric 0 :time 123}])
        (is (= (last @calls)
               ["https://premium-api.boundary.com/v1/measurements"
                (merge default {:body "[[\"host\",\"METRIC\",0,123]]"})]))

        ((bdry {:metric-id "metric" :org "org"})
         [{:host "host" :service "service"
           :metric 0 :time 123}])
        (is (= (last @calls)
               ["https://premium-api.boundary.com/v1/measurements"
                (merge default {:body "[[\"host\",\"ORG_METRIC\",0,123]]"})]))

        ((bdry {:org "org"}) [{:host "host1" :service "service1"
                               :metric 0 :time 123}
                              {:host "host2" :service "service2"
                               :metric 0 :time 123}])
        (is (= (last @calls)
               ["https://premium-api.boundary.com/v1/measurements"
                (merge default {:body
                                (str "[[\"host1\",\"ORG_SERVICE1\",0,123],"
                                     "[\"host2\",\"ORG_SERVICE2\",0,123]]")})]))

        ((bdry {:metric-id "metric" :org "org"})
         [{:host "host1" :service "service1"
           :metric 0 :time 123}
          {:host "host2" :service "service2"
           :metric 0 :time 123}])
        (is (= (last @calls)
               ["https://premium-api.boundary.com/v1/measurements"
                (merge default {:body (str "[[\"host1\",\"ORG_METRIC\",0,123],"
                                           "[\"host2\",\"ORG_METRIC\",0,123]]")})]))

        ((bdry {:async true}) [{:host "host" :service "service"
                                :metric 0 :time 123}])
        (is (= (last @calls)
               ["https://premium-api.boundary.com/v1/measurementsAsync"
                (merge default {:body "[[\"host\",\"SERVICE\",0,123]]"})]))))))
