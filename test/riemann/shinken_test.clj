(ns riemann.shinken-test
  (:use [riemann.time :only [unix-time]])
  (:use riemann.shinken
        aleph.http
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(deftest ^:shinken shinken-test
  (let [shkn (shinken {})
        time (unix-time)]

    "Test an event with unix time"
    (let [body (promise)
          e {:host "testhost"
             :service "testservice"
             :description "testdsecription"
             :metric 42
             :time (/ 351406934039 250)
             :state "ok"}
          expected_body "time_stamp=1405627736&host_name=testhost&service_description=testservice&return_code=ok&output=42"
          stop-server (start-http-server
                        (wrap-ring-handler
                          (fn [req]
                            (deliver body (slurp (:body req)))
                            {:status 200}))
                        {:port 7760})]
      (shkn e)
      (is (= expected_body @body))
      (stop-server))

    "Test sending to another port"
    (let [body (promise)
          e {:host "testhost"
             :service "testservice"
             :description "testdsecription"
             :metric 42
             :time 1405458798
             :state "ok"}
          expected_body "time_stamp=1405458798&host_name=testhost&service_description=testservice&return_code=ok&output=42"
          stop-server (start-http-server
                        (wrap-ring-handler
                          (fn [req]
                            (deliver body (slurp (:body req)))
                            {:status 200}))
                        {:port 7761})]
      ((shinken {:port 7761}) e)
      (is (= expected_body @body))
      (stop-server))

    "Test with a string as a metric and an integer as the state"
    (let [body (promise)
          e {:host "testhost"
             :service "testservice"
             :description "testdsecription"
             :metric "stringmetric"
             :time 1405458798
             :state 0}
          expected_body "time_stamp=1405458798&host_name=testhost&service_description=testservice&return_code=0&output=stringmetric"
          stop-server (start-http-server
                        (wrap-ring-handler
                          (fn [req]
                            (deliver body (slurp (:body req)))
                            {:status 200}))
                        {:port 7760})]
      (shkn e)
      (is (= expected_body @body))
      (stop-server))))
