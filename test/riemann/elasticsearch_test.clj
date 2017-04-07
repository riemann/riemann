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

(deftest ^:elasticsearch gen-request-bulk-body-reduce-test
  (is (= "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\"}}\n{\"field1\":\"value1\"}\n"
         (gen-request-bulk-body-reduce "" {:es-action "index"
                              :es-metadata {:_index "test"
                                            :_type "type1"
                                            :_id "1"}
                              :es-source {:field1 "value1"}})))
  (is (= "{\"delete\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"2\"}}\n"
         (gen-request-bulk-body-reduce "" {:es-action "delete"
                              :es-metadata {:_index "test"
                                            :_type "type1"
                                            :_id "2"}}))))

(deftest ^:elasticsearch gen-request-bulk-body-test
  (is (= "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\"}}\n{\"field1\":\"value1\"}\n"
         (gen-request-bulk-body [{:es-action "index"
                          :es-metadata {:_index "test"
                                        :_type "type1"
                                        :_id "1"}
                          :es-source {:field1 "value1"}}])))
  (is (= "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\"}}\n{\"field1\":\"value1\"}\n{\"delete\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"2\"}}\n"
         (gen-request-bulk-body [{:es-action "index"
                          :es-metadata {:_index "test"
                                        :_type "type1"
                                        :_id "1"}
                          :es-source {:field1 "value1"}}
                         {:es-action "delete"
                          :es-metadata {:_index "test"
                                        :_type "type1"
                                        :_id "2"}}])))
    (is (= "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\"}}\n{\"field1\":\"value1\"}\n{\"delete\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"2\"}}\n{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\"}}\n{\"field1\":\"value1\"}\n"
         (gen-request-bulk-body [{:es-action "index"
                          :es-metadata {:_index "test"
                                        :_type "type1"
                                        :_id "1"}
                          :es-source {:field1 "value1"}}
                         {:es-action "delete"
                          :es-metadata {:_index "test"
                                        :_type "type1"
                                        :_id "2"}}
                         {:es-action "index"
                          :es-metadata {:_index "test"
                                        :_type "type1"
                                        :_id "1"}
                          :es-source {:field1 "value1"}}]))))

(deftest ^:elasticsearch elasticsearch-bulk-test
  (with-mock [calls clj-http.client/post]
    (testing "without auth"
      (let [elastic (elasticsearch-bulk {})]
        (elastic {:es-action "index"
                  :es-metadata {:_index "test"
                                :_type "type1"
                                :_id "1"}
                  :es-source {:field1 "value1"}})
        (is (= (last @calls)
               ["http://127.0.0.1:9200/_bulk"
                {:body "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\"}}\n{\"field1\":\"value1\"}\n"
                 :content-type "application/x-ndjson"
                 :conn-timeout 5000
                 :socket-timeout 5000
                 :throw-entire-message? true}]))
        (elastic [{:es-action "index"
                   :es-metadata {:_index "test"
                                 :_type "type1"
                                 :_id "1"}
                   :es-source {:field1 "value1"}}
                  {:es-action "delete"
                   :es-metadata {:_index "test"
                                 :_type "type1"
                                 :_id "2"}}
                  {:es-action "index"
                   :es-metadata {:_index "test"
                                 :_type "type1"
                                 :_id "1"}
                   :es-source {:field1 "value1"}}])
        (is (= (last @calls)
               ["http://127.0.0.1:9200/_bulk"
                {:body "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\"}}\n{\"field1\":\"value1\"}\n{\"delete\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"2\"}}\n{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\"}}\n{\"field1\":\"value1\"}\n"
                 :content-type "application/x-ndjson"
                 :conn-timeout 5000
                 :socket-timeout 5000
                 :throw-entire-message? true}]))))
    (testing "with auth"
      (let [elastic (elasticsearch-bulk {:username "elastic"
                                         :password "changeme"})]
        (elastic {:es-action "index"
                  :es-metadata {:_index "test"
                                :_type "type1"
                                :_id "1"}
                  :es-source {:field1 "value1"}})
        (is (= (last @calls)
               ["http://127.0.0.1:9200/_bulk"
                {:body "{\"index\":{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\"1\"}}\n{\"field1\":\"value1\"}\n"
                 :content-type "application/x-ndjson"
                 :conn-timeout 5000
                 :socket-timeout 5000
                 :basic-auth ["elastic" "changeme"]
                 :throw-entire-message? true}]))))
    (testing "with formatter"
      (let [formatter (default-bulk-formatter {:es-index "riemann"
                                               :type "foo"
                                               :es-action "index"
                                               :index-suffix "-yyyy.MM.dd"})
            elastic (elasticsearch-bulk {:username "elastic"
                                         :password "changeme"
                                         :formatter formatter
                                         :http-options {:conn-timeout 1000}
                                         :es-endpoint "http://127.0.0.1:9300"})]
        (elastic {:host "foo"
                  :metric 10
                  :time 1491567382
                  :tags ["t1"]
                  :es-id "3"
                  :ttl 30})
        (is (= (last @calls)
               ["http://127.0.0.1:9300/_bulk"
                {:body "{\"index\":{\"_index\":\"riemann-2017.04.07\",\"_type\":\"foo\",\"_id\":\"3\"}}\n{\"host\":\"foo\",\"metric\":10,\"tags\":[\"t1\"],\"ttl\":30,\"@timestamp\":\"2017-04-07T12:16:22.000Z\"}\n"
                 :content-type "application/x-ndjson"
                 :conn-timeout 1000
                 :socket-timeout 5000
                 :basic-auth ["elastic" "changeme"]
                 :throw-entire-message? true}]))))))

(deftest ^:elasticsearch default-bulk-formatter-test
  (let [formatter (default-bulk-formatter {:es-index "riemann"
                                           :type "foo"
                                           :es-action "index"
                                           :index-suffix "-yyyy.MM.dd"})]
    (is (= (formatter {:host "foo"
                       :metric 10
                       :service "bar"
                       :tags ["t1"]
                       :ttl 30
                       :time 1491567382
                       :foo "bar"})
           {:es-action "index"
            :es-metadata {:_index "riemann-2017.04.07"
                          :_type "foo"}
            :es-source {:host "foo"
                        :metric 10
                        :service "bar"
                        :tags ["t1"]
                        :ttl 30
                        :foo "bar"
                        (keyword "@timestamp") "2017-04-07T12:16:22.000Z"
                        }}))
    (is (= (formatter {:host "foo"
                       :metric 10
                       :service "bar"
                       :tags ["t1"]
                       :ttl 30
                       :es-id "10"
                       :es-index "test"
                       :type "bar"
                       :es-action "create"
                       :index-suffix "-yyyy.MM"
                       :time 1491567382
                       :foo "bar"})
           {:es-action "create"
            :es-metadata {:_index "test-2017.04"
                          :_type "bar"
                          :_id "10"}
            :es-source {:host "foo"
                        :metric 10
                        :service "bar"
                        :tags ["t1"]
                        :ttl 30
                        :foo "bar"
                        (keyword "@timestamp") "2017-04-07T12:16:22.000Z"
                        }}))))

(deftest ^:integration ^:elasticsearch elasticsearch-bulk-integration-test
  (testing "Without formatter"
    (let [elastic (elasticsearch-bulk {:username "elastic"
                                       :password "changeme"})]
      (elastic {:es-action "index"
                :es-metadata {:_index "test"
                              :_type "type1"
                              :_id "1"}
                :es-source {:field1 "value1"}})
      (elastic {:es-action "index"
                :es-metadata {:_index "test"
                              :_type "type1"
                              :_id "2"}
                :es-source {:field1 "value2"}})
      (elastic {:es-action "index"
                :es-metadata {:_index "test"
                              :_type "type2"}
                :es-source {:field1 "value3"}})
      (elastic {:es-action "update"
                :es-metadata {:_index "test"
                              :_type "type1"
                              :_id "1"}
                :es-source {:doc {:field1 "new_value"}}})
      (elastic {:es-action "create"
                :es-metadata {:_index "test"
                              :_type "type1"
                              :_id "5"}
                :es-source {:field4 "value4"}})
      (elastic {:es-action "create"
                :es-metadata {:_index "test"
                              :_type "type2"
                              :_id "6"}
                :es-source {:field4 "value4"}})
      (elastic {:es-action "delete"
                :es-metadata {:_index "test"
                              :_type "type1"
                              :_id "1"}})))
  (testing "Using default formatter"
    (let [formatter (default-bulk-formatter {:es-index "riemann"
                                             :type "foo"
                                             :es-action "index"
                                             :index-suffix "-yyyy.MM.dd"})
          elastic (elasticsearch-bulk {:username "elastic"
                                       :password "changeme"
                                       :formatter formatter})]
      (elastic {:host "fooformatter"
                :metric 10
                :service "bar"
                :tags ["t1"]
                :ttl 30
                :time 1491567382
                :foo "bar"}))))

(deftest ^:integration ^:elasticsearch elasticsearch-integration-test
  (let [elastic (elasticsearch {:username "elastic"
                                :password "changeme"})]
    (elastic {:host "foo"
              :service "bar"
              :metric "100"
              :time (riemann.time/unix-time)
              :state "critical"
              :tags ["t1" "t2"]})))
