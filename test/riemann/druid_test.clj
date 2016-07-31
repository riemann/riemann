(ns riemann.druid-test
  (:require [clj-http.client :as client]
            [clojure.test :refer :all]
            [riemann.druid :as druid]
            [riemann.logging :as logging]
            [riemann.test-utils :refer [with-mock]]))

(logging/init)

(deftest ^:druid druid-test
  (with-mock [calls client/post]
    (let [d (druid/druid {:host "localhost"})]

      (testing "an event with metric")
      (d {:host    "testhost"
          :service "testservice"
          :metric  42
          :time    123456789
          :state   "ok"})
      (is (= 1 (count @calls)))
      (is (= (vec (last @calls))
             ["http://localhost:8200/v1/post/riemann"
              {:body "[{\"host\":\"testhost\",\"service\":\"testservice\",\"state\":\"ok\",\"timestamp\":\"1973-11-29T21:33:09.000Z\",\"tags\":null,\"description\":null,\"value\":42}]"
               :socket-timeout 5000
               :conn-timeout 5000
               :content-type :json
               :throw-entire-message? true}])))))
