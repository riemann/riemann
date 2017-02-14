(ns riemann.pagerduty-test
  (:require [riemann.pagerduty :as pg]
            [clj-http.client :as client]
            [riemann.test-utils :refer [with-mock]]
            [cheshire.core :as json]
            [clojure.test :refer :all]))

(deftest ^:pagerduty send-event-test
  (with-mock [calls client/post]
    (testing "default paerduty stream"
      (let [s (pg/pagerduty {:service-key "foobarkey"})
            event {:host "foo" :service "bar" :state "critical"}]
        ((:trigger s) event)
        (is (= (into [] (first @calls))
               ["https://events.pagerduty.com/generic/2010-04-15/create_event.json"
                {:body (json/generate-string (merge {:service_key "foobarkey"
                                                     :event_type :trigger}
                                                    (pg/format-event event)))
                 :socket-timeout 5000
                 :conn-timeout 5000
                 :content-type :json
                 :accept :json
                 :throw-entire-message? true}]))
        (reset! calls [])
        ((:acknowledge s) event)
        (is (= (into [] (first @calls))
               ["https://events.pagerduty.com/generic/2010-04-15/create_event.json"
                {:body (json/generate-string (merge {:service_key "foobarkey"
                                                     :event_type :acknowledge}
                                                    (pg/format-event event)))
                 :socket-timeout 5000
                 :conn-timeout 5000
                 :content-type :json
                 :accept :json
                 :throw-entire-message? true}]))
        (reset! calls [])
        ((:resolve s) event)
        (is (= (into [] (first @calls))
               ["https://events.pagerduty.com/generic/2010-04-15/create_event.json"
                {:body (json/generate-string (merge {:service_key "foobarkey"
                                                     :event_type :resolve}
                                                    (pg/format-event event)))
                 :socket-timeout 5000
                 :conn-timeout 5000
                 :content-type :json
                 :accept :json
                 :throw-entire-message? true}]))
        (reset! calls [])))
    (testing "override formatter and options parameters"
      (let [formatter (fn [event] (:foo (str event)))
            s (pg/pagerduty {:service-key "foobarkey"
                             :formatter formatter
                             :options {:proxy-host "127.0.0.1"
                                       :proxy-port 8080}})
            event {:host "foo" :service "bar" :state "critical"}]
        ((:resolve s) event)
        (is (= (into [] (first @calls))
               ["https://events.pagerduty.com/generic/2010-04-15/create_event.json"
                {:body (json/generate-string (merge {:service_key "foobarkey"
                                                     :event_type :resolve}
                                                    (formatter event)))
                 :socket-timeout 5000
                 :conn-timeout 5000
                 :content-type :json
                 :accept :json
                 :proxy-host "127.0.0.1"
                 :proxy-port 8080
                 :throw-entire-message? true}]))
        (reset! calls [])))))

