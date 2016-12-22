(ns riemann.irc-test
  (:use riemann.irc
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(def test-irc-message
   "Riemann alert on host01 - test_service is OK - Description: Quick brown fox")

(def test-event
  {:host "host01" :service "test_service" :state "ok" :description "Quick brown fox"})

; test that formatting works
(deftest irc-formatting
  (is (= (irc-message test-event)
                        test-irc-message)))
