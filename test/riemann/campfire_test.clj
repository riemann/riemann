(ns riemann.campfire-test
  (:use riemann.campfire
        clojure.test)
  (:require [riemann.logging :as logging]))

(logging/init)

(def campfire_api_token "test_token")
(def campfire_domain "example.com")

(def campfire-settings
  {:api-token campfire_api_token
   :ssl true
   :sub-domain campfire_domain})

(def test-campfire_message
   "Riemann alert on host01 - test_service is OK - Description: Quick brown fox")

(def test-event
  {:host "host01" :service "test_service" :state "ok" :description "Quick brown fox"})

; test that settings and formatting work
(deftest camp-settings
  (is (= (cf-settings
                      campfire_api_token true campfire_domain) campfire-settings))
  (is (= (campfire_message test-event)
                        test-campfire_message)))
