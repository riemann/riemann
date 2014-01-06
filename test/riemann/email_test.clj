(ns riemann.email-test
  (:use [riemann.time :only [unix-time]]
        riemann.email
        [riemann.logging :only [suppress]]
        clojure.test))

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

(deftest ^:email ^:integration email-test
         (let [email (mailer {:from "riemann-test"})
               stream (email "aphyr@aphyr.com")]
           (stream {:host "localhost"
                    :service "email test"
                    :state "ok"
                    :description "all clear, uh, situation normal"
                    :metric 3.14159
                    :time (unix-time)})))
