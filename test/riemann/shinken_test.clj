(ns riemann.shinken-test
  (:require [riemann.logging :as logging]
            [riemann.test-utils :refer [with-mock]]
            [riemann.time :refer [unix-time]]
            [riemann.shinken :refer :all]
            [clj-http.client :as http]
            [clojure.test :refer :all]))

(logging/init)

(deftest ^:shinken shinken-test
  (with-mock [calls clj-http.client/post]
    (let [shkn (shinken {})
          time (unix-time)]

      (testing "an event with unix time"
        (shkn {:host "testhost"
                 :service "testservice"
                 :description "testdsecription"
                 :metric 42
                 :time (/ 351406934039 250)
                 :state "ok"})
        (is (= (last @calls)
               ["http://127.0.0.1:7760/push_check_result"
                {:basic-auth ["admin" "admin"]
                 :form-params {:time_stamp 1405627736,
                               :host_name "testhost"
                               :service_description "testservice"
                               :return_code "ok"
                               :output 42}}])))

      (testing "sending to another port"
        ((shinken {:host "verne" :port 7761})
         {:host "testhost"
          :service "testservice"
          :description "testdsecription"
          :metric 42
          :time 1405458798
          :state "ok"})
         (is (= (last @calls)
                ["http://127.0.0.1:7761/push_check_result"
                 {:basic-auth ["admin" "admin"]
                  :form-params {:time_stamp 1405458798
                                :host_name "testhost"
                                :service_description "testservice"
                                :return_code "ok"
                                :output 42}}])))

      (testing "a string as a metric and an integer as the state"
        (shkn {:host "testhost"
               :service "testservice"
               :description "testdsecription"
               :metric "stringmetric"
               :time 1405458798
               :state 0})
        (is (= (last @calls)
               ["http://127.0.0.1:7760/push_check_result"
                {:basic-auth ["admin" "admin"]
                 :form-params {:time_stamp 1405458798
                               :host_name "testhost"
                               :service_description "testservice"
                               :return_code 0
                               :output "stringmetric"}}]))))))
