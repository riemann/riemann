(ns riemann.pushover-test
  (:require [riemann.test-utils :refer [with-mock]]
            [riemann.time :refer [unix-time]]
            [riemann.logging :as logging]
            [riemann.pushover :refer :all]
            [clj-http.client :as http]
            [clojure.test :refer :all]))

(logging/init)

(defn- my-custom-formatter [event]
  {:title (str (:state event) " - " (:service event) "@" (:projectid event))
   :message (str "<b>"(:targetid event) "</b>\n" (:description event))})

(deftest ^:pushover pushover-test
  (with-mock [calls clj-http.client/post]
    (let [pshvr (pushover "token" "user")
          pshvr-custom (pushover "token" "user" {:formatter my-custom-formatter
                                                 :html 1})
          time (unix-time)]

      (testing "an event without metric"
        (pshvr {:host "testhost"
                :service "testservice"
                :state "ok"})
        (is (= (last @calls)
               ["https://api.pushover.net/1/messages.json"
                {:form-params {:token "token"
                               :user "user"
                               :title "testhost testservice"
                               :message "testhost testservice is ok ()"}}])))

      (testing "an event with opts"
        (pshvr-custom {:host "testhost"
                       :service "testservice"
                       :state "critical"
                       :projectid "myproject"
                       :targetid "prod"
                       :description "request rate is critical"})
        (is (= (last @calls)
               ["https://api.pushover.net/1/messages.json"
                {:form-params {:token "token"
                               :user "user"
                               :title "critical - testservice@myproject"
                               :message "<b>prod</b>\nrequest rate is critical"
                               :html 1}}]))))))
