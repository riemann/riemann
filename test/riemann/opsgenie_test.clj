(ns riemann.opsgenie-test
  (:require [riemann.logging :as logging]
            [riemann.opsgenie :refer :all]
            [riemann.test-utils :refer [with-mock]]
            [cheshire.core :as json]
            [clojure.test :refer :all]
            [clj-http.util :refer [url-encode]]))

(def service-key (System/getenv "OPSGENIE_SERVICE_KEY"))

(when-not service-key
  (println "export OPSGENIE_SERVICE_KEY=\"...\" to run these tests."))

(logging/init)

(deftest ^:opsgenie ^:integration test-trigger
  (let [og (opsgenie {:api-key service-key})]
    ((:trigger og) {:host "localhost"
                    :service "opsgenie notification"
                    :description "Testing triggering event"
                    :metric 20
                    :state "error"})))

(deftest ^:opsgenie ^:integration test-resolve
  (let [og (opsgenie {:api-key service-key})]
    ((:resolve og) {:host "localhost"
                    :service "opsgenie notification"
                    :description "Testing resolving event"
                    :metric 42
                    :state "ok"})))

(deftest ^:opsgenie test-opsgenie
  (testing "default body fn"
    (let [og (opsgenie {:api-key "my_api_key"})
          event {:host "localhost"
                 :service "opsgenie notification"
                 :description "Testing triggering event"
                 :metric 20
                 :state "error"}]
      (with-mock [calls clj-http.client/post]
        ((:trigger og) event)
        (is (= (first (first @calls)) "https://api.opsgenie.com/v2/alerts"))
        (is (= (second (first @calls))
               {:body (json/generate-string (default-body event))
                :headers {"Authorization" "GenieKey my_api_key"}
                :socket-timeout 5000
                :conn-timeout 5000
                :content-type :json
                :accept :json
                :throw-entire-message? true}))
        ((:resolve og) event)
        (is (= (first (second @calls))
               (str "https://api.opsgenie.com/v2/alerts/"
                    (url-encode (str (api-alias event)))
                    "/close?identifierType=alias")))
        (is (= (second (second @calls))
               {:body (json/generate-string {:user "Riemann"})
                :headers {"Authorization" "GenieKey my_api_key"}
                :socket-timeout 5000
                :conn-timeout 5000
                :content-type :json
                :accept :json
                :throw-entire-message? true})))))
  (testing "custom body fn"
    (let [body-fn (fn [e] {:message (:host e)
                           :alias (:service e)})
          og (opsgenie {:api-key "my_api_key"
                        :body-fn body-fn})
          event {:host "localhost"
                 :service "opsgenie notification"}]
      (with-mock [calls clj-http.client/post]
        ((:trigger og) event)
        (is (= (first (first @calls)) "https://api.opsgenie.com/v2/alerts"))
        (is (= (second (first @calls))
               {:body (json/generate-string (body-fn event))
                :headers {"Authorization" "GenieKey my_api_key"}
                :socket-timeout 5000
                :conn-timeout 5000
                :content-type :json
                :accept :json
                :throw-entire-message? true}))
        ((:resolve og) event)
        (is (= (first (second @calls))
               (str "https://api.opsgenie.com/v2/alerts/"
                    (url-encode (:alias (body-fn event)))
                    "/close?identifierType=alias")))
        (is (= (second (second @calls))
               {:body (json/generate-string {:user "Riemann"})
                :headers {"Authorization" "GenieKey my_api_key"}
                :socket-timeout 5000
                :conn-timeout 5000
                :content-type :json
                :accept :json
                :throw-entire-message? true}))))))

