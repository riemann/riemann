(ns riemann.nagios-test
  (:use riemann.nagios
        clj-nsca.core
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(def test-event
  {:host "host01" :service "test_service" :state "ok" :description "Quick brown fox"})

(def expected
  (let [e test-event]
    (nagios-message (:host e) (:state e) (:service e) (:description e))))

(deftest test-event-to-nagios
  (testing "Transform event to Nagios message"
    (is (= expected (event->nagios test-event))))
  (testing "Malformed events are rejected"
    (is (thrown? IllegalArgumentException (event->nagios (merge test-event {:state "borken"}))))))

(deftest test-stream
  (testing "Events get transformed and are sent"
  (let [message (atom nil)]
    (with-redefs [clj-nsca.core/send-message (fn [_ msg] (reset! message msg))]
      ((nagios {}) test-event))
    (is (= expected @message)))))
