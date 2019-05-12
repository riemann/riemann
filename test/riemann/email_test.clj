(ns riemann.email-test
  (:require [riemann.email :refer :all]
            [riemann.logging :refer [suppress]]
            [riemann.time :refer [unix-time]]
            [clojure.test :refer :all]))

(riemann.logging/init)

(deftest override-formatting-test
         (let [a (promise)]
           (with-redefs [postal.core/send-message #(deliver a [%1 %2])]
             (email-event {} {:body (fn [events]
                                      (apply str "body "
                                             (map :service events)))
                              :subject (fn [events]
                                         (apply str "subject "
                                                (map :service events)))}
                          {:service "foo"}))
           (is (= @a [{} {:subject "subject foo"
                          :body    "body foo"}]))))

(deftest email-test-erroring
  (testing "sending an email integration test"
    (let [email (mailer {:from "riemann-test"})]
      (is (thrown? java.lang.AssertionError
                   (email [:a :b])))
      (is (thrown? java.lang.AssertionError
                   (email {:host "localhost"
                           :service "email test"
                           :state "ok"
                           :description "all clear, uh, situation normal"
                           :metric 3.14159
                           :time (unix-time)}))))))

(deftest email-test-list-input
  (testing "sending an email to a list of recipients"
    (let [p (promise)]
      (with-redefs [riemann.email/email-event (fn [_ m _] (deliver p (:to m)))]
        (let [email ((mailer) ["a@a.a" "b@b.b"])]
          (email {:service "foo"}))
        (is (= (deref  p 500 nil) ["a@a.a" "b@b.b"]))))))

(deftest ^:email ^:integration email-test
  (testing "sending an email integration test"
         (let [email (mailer {:from "riemann-test"})
               stream (email "aphyr@aphyr.com")]
           (stream {:host "localhost"
                    :service "email test"
                    :state "ok"
                    :description "all clear, uh, situation normal"
                    :metric 3.14159
                    :time (unix-time)}))))
