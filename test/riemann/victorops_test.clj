(ns riemann.victorops-test
  (:require [riemann.victorops :as vo]
            [riemann.test-utils :refer [with-mock]]
            [clj-http.client :as client]
            [clojure.test :refer :all]))

(deftest victorops-test
  (with-mock [calls client/post]
    (let [vo (vo/victorops "my-dumb-api-key" "my-dumb-routing-key")]
      ((:info vo) {:host    "my-dumb-host"
                   :service "victorops info notification"
                   :metric   42
                   :time     12345678
                   :state    "info"})
      (is (= 1 (count @calls)))
      (is (= (last @calls)
             ["https://alert.victorops.com/integrations/generic/20131114/alert/my-dumb-api-key/my-dumb-routing-key"
              {:body "{\"message_type\":\"INFO\",\"entity_id\":\"my-dumb-host/victorops info notification\",\"timestamp\":12345678,\"state_start_time\":12345678,\"state_message\":\"my-dumb-host victorops info notification is info (42)\",\"entity_is_host\":false,\"monitoring_tool\":\"riemann\"}"
               :socket-timeout 5000
               :conn-timeout 5000
               :content-type :json
               :accept :json
               :throw-entire-message? true}])))))
