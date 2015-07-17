(ns riemann.pushover-test
  (:use [riemann.time :only [unix-time]]
        riemann.pushover
        clojure.test)
  (:require [clj-http.client :as http]
            [riemann.test-utils :refer [with-mock]]
            [riemann.logging :as logging]))

(logging/init)

(deftest ^:pushover pushover-test
  (with-mock [calls clj-http.client/post]
    (let [pshvr (pushover "token" "user")
          time (unix-time)]

      (testing "an event without metric")
      (pshvr {:host "testhost"
              :service "testservice"
              :state "ok"})
      (is (= (last @calls)
             ["https://api.pushover.net/1/messages.json"
              {:form-params {:token "token"
                             :user "user"
                             :title "testhost testservice"
                             :message "testhost testservice is ok ()"}}])))))
