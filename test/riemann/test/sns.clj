(ns riemann.test.sns
  (:use [riemann.time :only [unix-time]]
        [riemann.common :only [time-at count-string-bytes]]
        riemann.sns
        clojure.test))

(def access-key-id     (System/getenv "AWS_ACCESS_KEY_ID"))
(def secret-access-key (System/getenv "AWS_SECRET_ACCESS_KEY"))
(def arn               (System/getenv "AWS_SNS_TOPIC"))

(deftest override-formatting-test
  (let [a (promise)]
    (with-redefs [clj-aws.sns/publish #(deliver a [%2 %3 %4])]
      (sns-publish {} {:body (fn [events]
                               (apply str "body " 
                                      (map :service events)))
                       :subject (fn [events] 
                                  (apply str "subject " 
                                         (map :service events)))
                       :arn ["my:arn"]}
                   {:service "foo"}))
    (is (= @a ["my:arn" "subject foo" "body foo"]))))

(deftest is-subject-truncated-test
  (let [a (promise)]
    (with-redefs [clj-aws.sns/publish #(deliver a [%2 %3 %4])]
      (sns-publish {} {:arn ["my:arn"]}
                   {:service (apply str (repeat 101 "あ")) :time 0}))
    (is (<= (count-string-bytes (second @a)) 100))))

(deftest sns-publisher-test
  (let [a (promise)
        sns (sns-publisher {:access-key "riemann-test"
                            :secret-key "secret"
                            :region :ap-north})
        stream (sns "test:arn")]
    (with-redefs [clj-aws.sns/publish #(deliver a [%2 %3 %4])]
      (stream {:host "localhost"
               :service "sns test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 2.71828
               :time 0}))
    (is (= @a [
               "test:arn"
               "localhost sns test ok"
               (str "At "
                    (time-at 0)
                    "\nlocalhost sns test ok (2.71828)\nTags: []\n\nall clear, uh, situation normal")]))))

(deftest ^:sns ^:integration sns-test
  (when-not access-key-id
    (println "export AWS_ACCESS_KEY_ID=\"...\" to run :sns tests."))
  (when-not secret-access-key
    (println "export AWS_SECRET_ACCESS_KEY=\"...\" to run :sns tests."))
  (when-not arn
    (println "export AWS_SNS_TOPIC=\"...\" to run :sns tests."))

  (let [sns (sns-publisher {:access-key access-key-id
                            :secret-key secret-access-key
                            :region :ap-north})
        stream (sns arn)]
    (stream {:host "localhost"
             :service "sns test"
             :state "ok"
             :description "all clear, uh, situation normal"
             :metric 3.14159
             :time (unix-time)})))
