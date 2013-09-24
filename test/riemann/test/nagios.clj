(ns riemann.test.nagios
  (:use riemann.nagios
        clj-nsca.core
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(def test-event
  {:host "host01" :service "test_service" :state "ok" :description "Quick brown fox"})

(def expected
  (let [e test-event]
  (nagios-message :host (:host e) :service (:service e) :level (:state e) :message (:description e))))

(deftest nagios-test
  (is (= (event->nagios test-event)
                        expected)))
