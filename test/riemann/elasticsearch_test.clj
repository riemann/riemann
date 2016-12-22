(ns riemann.elasticsearch-test
  (:use riemann.elasticsearch
        clojure.test)
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [riemann.test-utils :refer [with-mock]]
            [riemann.logging :as logging]))

(logging/init)

(def ^:const input-event  {:description "test description"
                           :host "testhost"
                           :service "testservice"
                           :metric 1337
                           :state "leet"
                           ; quarter second past 1451606400 to include Ratio timestamps
                           :time 5806425601/4})
(def ^:const output-event {:host "testhost"
                           :service "testservice"
                           :metric 1337
                           :state "leet"
                           :tags nil
                           "@timestamp" "2016-01-01T00:00:00.250Z"})

(deftest ^:elasticsearch elasticsearch-default-test
  (with-mock [calls clj-http.client/post]
    (let [elastic (elasticsearch {})
          json-event (json/generate-string output-event)]

      (testing "correct event reformatting and default post"
        (elastic input-event)
        (is (= (last @calls)
               ["http://127.0.0.1:9200/riemann-2016.01.01/event"
                {:body json-event
                 :content-type :json
                 :conn-timeout 5000
                 :socket-timeout 5000
                 :throw-entire-message? true}]))))))

(deftest ^:elasticsearch elasticsearch-opts-test
  (with-mock [calls clj-http.client/post]
    (let [elastic (elasticsearch {:es-endpoint "https://example-elastic.com"
                                  :es-index "test-riemann"
                                  :index-suffix "-yyyy.MM"
                                  :type "test-type"})
          json-event (json/generate-string output-event)]

      (testing "correct index/type formatting with custom elasticsearch opts"
        (elastic input-event)
        (is (= (last @calls)
               ["https://example-elastic.com/test-riemann-2016.01/test-type"
                {:body json-event
                 :content-type :json
                 :conn-timeout 5000
                 :socket-timeout 5000
                 :throw-entire-message? true}]))))))

(deftest ^:elasticsearch elasticsearch-event-formatter-test
  (with-mock [calls clj-http.client/post]
    (let [formatter identity
          elastic (elasticsearch {} formatter)
          json-event (json/generate-string input-event)]

      (testing "custom formatter use"
        (elastic input-event)
        (is (= (last @calls)
               ["http://127.0.0.1:9200/riemann-2016.01.01/event"
                {:body json-event
                 :content-type :json
                 :conn-timeout 5000
                 :socket-timeout 5000
                 :throw-entire-message? true}]))))))

(deftest ^:elasticsearch elasticsearch-valid-credentials-test
  (with-mock [calls clj-http.client/post]
    (let [elastic (elasticsearch {:es-endpoint "https://example-elastic.com"
                                  :es-index "test-riemann"
                                  :index-suffix "-yyyy.MM"
                                  :type "test-type"
                                  :username "foo"
                                  :password "bar"})
          json-event (json/generate-string output-event)]

      (testing "correct basic-auth constructed from valid credentials"
        (elastic input-event)
        (is (= (last @calls)
               ["https://example-elastic.com/test-riemann-2016.01/test-type"
                {:body json-event
                 :content-type :json
                 :conn-timeout 5000
                 :socket-timeout 5000
                 :throw-entire-message? true
                 :basic-auth ["foo" "bar"]}]))))))

(deftest ^:elasticsearch elasticsearch-invalid-credentials-test
  (with-mock [calls clj-http.client/post]
    (let [elastic (elasticsearch {:es-endpoint "https://example-elastic.com"
                                  :es-index "test-riemann"
                                  :index-suffix "-yyyy.MM"
                                  :type "test-type"
                                  :username "foo"}) ; password is missing
          json-event (json/generate-string output-event)]

      (testing "invalid credentials are ignored"
        (elastic input-event)
        (is (= (last @calls)
               ["https://example-elastic.com/test-riemann-2016.01/test-type"
                {:body json-event
                 :content-type :json
                 :conn-timeout 5000
                 :socket-timeout 5000
                 :throw-entire-message? true}]))))))
