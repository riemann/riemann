(ns riemann.pagerduty-test
  (:require [riemann.pagerduty :as pg]
            [riemann.test-utils :refer [with-mock]]
            [riemann.time :refer :all]
            [riemann.time.controlled :refer :all]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.test :refer :all]))

(use-fixtures :once control-time!)
(use-fixtures :each reset-time!)

(deftest ^:pagerduty format-event-v1-test
  (let [event {:host "riemann.io"
               :service "req_per_sec"
               :state "critical"
               :metric 100}]
    (is (= {:incident_key "riemann.io req_per_sec"
            :description "riemann.io req_per_sec is critical (100)"
            :details event}
           (pg/format-event-v1 event)))))

(deftest ^:pagerduty format-event-v2-test
  (let [event {:host "riemann.io"
               :service "req_per_sec"
               :state "critical"
               :metric 100}]
    (testing "with time"
      (is (= {:summary "riemann.io - req_per_sec is critical (100)"
              :source "riemann.io"
              :severity "critical"
              :custom_details (assoc event :time 100)
              :timestamp "1970-01-01T00:00:00.100Z"}
             (pg/format-event-v2 (assoc event :time 100)))))
    (testing "without time"
      (is (= {:summary "riemann.io - req_per_sec is critical (100)"
              :source "riemann.io"
              :timestamp "1970-01-01T00:00:00.000Z"
              :custom_details event
              :severity "critical"}
             (pg/format-event-v2 event))))))

(deftest request-body-v1-test
  (let [service-key "fookey"
        event {:host "riemann.io"
               :service "req_per_sec"
               :state "critical"
               :metric 100}
        formatter pg/format-event-v1
        event-type :trigger]
    (is (= {:service_key "fookey"
            :event_type :trigger
            :incident_key "riemann.io req_per_sec"
            :description "riemann.io req_per_sec is critical (100)"
            :details event}
           (pg/request-body-v1 service-key
                               event-type
                               formatter
                               event)))))

(deftest request-body-v2-test
  (let [service-key "fookey"
        action :trigger
        formatter pg/format-event-v2
        event {:host "riemann.io"
               :service "req_per_sec"
               :state "critical"
               :time 100
               :metric 100}]
    (testing "without :dedup-key"
      (is (= {:routing_key service-key
              :event_action action
              :payload {:summary "riemann.io - req_per_sec is critical (100)"
                        :source "riemann.io"
                        :severity "critical"
                        :timestamp "1970-01-01T00:00:00.100Z"
                        :custom_details event}}
             (pg/request-body-v2 service-key
                                 action
                                 formatter
                                 event))))
    (testing "with :dedup-key"
      (is (= {:routing_key service-key
              :event_action action
              :payload {:summary "riemann.io - req_per_sec is critical (100)"
                        :source "riemann.io"
                        :severity "critical"
                        :timestamp "1970-01-01T00:00:00.100Z"
                        :custom_details (assoc event :dedup-key "riemann-alert")}
              :dedup_key "riemann-alert"}
             (pg/request-body-v2 service-key
                                 action
                                 formatter
                                 (assoc event :dedup-key "riemann-alert")))))))

(deftest ^:pagerduty pagerduty-v1-test
  (with-mock [calls client/post]
    (testing "default pagerduty stream"
      (let [s (pg/pagerduty {:service-key "foobarkey"})
            event {:host "foo" :service "bar" :state "critical"}]
        ((:trigger s) event)
        (is (= (into [] (first @calls))
               ["https://events.pagerduty.com/generic/2010-04-15/create_event.json"
                {:body (json/generate-string (merge {:service_key "foobarkey"
                                                     :event_type :trigger}
                                                    (pg/format-event-v1 event)))
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
                                                    (pg/format-event-v1 event)))
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
                                                    (pg/format-event-v1 event)))
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

(deftest ^:pagerduty pagerduty-v2-test
  (with-mock [calls client/post]
    (testing "default pagerduty stream"
      (let [service-key "foobarkey"
            s (pg/pagerduty {:service-key service-key :version :v2})
            event {:host "foo" :service "bar" :state "critical"}]
        ((:trigger s) event)
        (is (= (into [] (first @calls))
               ["https://events.pagerduty.com/v2/enqueue"
                {:body (json/generate-string {:routing_key service-key
                                              :event_action :trigger
                                              :payload
                                              (pg/format-event-v2 event)})
                 :socket-timeout 5000
                 :conn-timeout 5000
                 :content-type :json
                 :accept :json
                 :throw-entire-message? true}]))
        (reset! calls [])
        ((:acknowledge s) event)
        (is (= (into [] (first @calls))
               ["https://events.pagerduty.com/v2/enqueue"
                {:body (json/generate-string {:routing_key service-key
                                              :event_action :acknowledge
                                              :payload
                                              (pg/format-event-v2 event)})
                 :socket-timeout 5000
                 :conn-timeout 5000
                 :content-type :json
                 :accept :json
                 :throw-entire-message? true}]))
        (reset! calls [])
        ((:resolve s) event)
        (is (= (into [] (first @calls))
               ["https://events.pagerduty.com/v2/enqueue"
                {:body (json/generate-string {:routing_key service-key
                                              :event_action :resolve
                                              :payload
                                              (pg/format-event-v2 event)})
                 :socket-timeout 5000
                 :conn-timeout 5000
                 :content-type :json
                 :accept :json
                 :throw-entire-message? true}]))
        (reset! calls [])))
    (testing "override formatter and options parameters"
      (let [service-key "foobarkey"
            formatter (fn [event] (:foo (str event)))
            s (pg/pagerduty {:service-key service-key
                             :formatter formatter
                             :version :v2
                             :options {:proxy-host "127.0.0.1"
                                       :proxy-port 8080}})
            event {:host "foo" :service "bar" :state "critical"}]
        ((:resolve s) event)
        (is (= (into [] (first @calls))
               ["https://events.pagerduty.com/v2/enqueue"
                {:body (json/generate-string {:routing_key "foobarkey"
                                              :event_action :resolve
                                              :payload
                                              (formatter event)})
                 :socket-timeout 5000
                 :conn-timeout 5000
                 :content-type :json
                 :accept :json
                 :proxy-host "127.0.0.1"
                 :proxy-port 8080
                 :throw-entire-message? true}]))
        (reset! calls [])))))

