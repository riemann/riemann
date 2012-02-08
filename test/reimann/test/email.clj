(ns reimann.test.email
  (:use reimann.email)
  (:use reimann.common)
  (:use clojure.test))

(deftest email-test
         (let [email (mailer {})
               stream (email "aphyr@aphyr.com")]
           (stream {:host "localhost"
                    :service "email test"
                    :state "ok"
                    :description "all clear, uh, situation normal"
                    :metric_f 3.14159
                    :time (unix-time)})))
