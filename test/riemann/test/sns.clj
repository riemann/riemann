(ns riemann.test.sns
  (:use [riemann.time :only [unix-time]]
        [riemann.common :only [time-at count-string-bytes]]
        riemann.sns
        clojure.test))

(def env-access-key-id     (System/getenv "AWS_ACCESS_KEY_ID"))
(def env-secret-access-key (System/getenv "AWS_SECRET_ACCESS_KEY"))
(def env-region            (System/getenv "AWS_REGION"))
(def env-arn               (System/getenv "AWS_SNS_TOPIC"))
(def fake-aws-opts {:access-key "access-key" :secret-key "secret-key" :region :ap-north})
(def fake-event {:host "localhost"
                 :service "sns test"
                 :state "ok"
                 :description "all clear, uh, situation normal"
                 :metric 2.71828
                 :time 0})
(def fake-event-subject "localhost sns test ok")
(def fake-event-body (str "At "
                          (time-at 0)
                          "\nlocalhost sns test ok (2.71828)\nTags: []\n\nall clear, uh, situation normal"))

(deftest override-formatting-test
  (let [message (#'riemann.sns/compose-message
                 {:body (fn [events]
                          (apply str "body " 
                                 (map :service events)))
                  :subject (fn [events] 
                             (apply str "subject " 
                                    (map :service events)))
                  :arn ["my:arn"]}
                 {:service "foo"})]
    (is (= message {:arns (list "my:arn") :body "body foo" :subject "subject foo"}))))

(deftest is-message-truncated-test
  (let [a (promise)]
    (with-redefs [clj-aws.sns/publish #(deliver a [%2 %3 %4])]
      (sns-publish {} {:arn ["my:arn"]}
                   {:service (apply str (repeat 8093 "あ")) :time 0}))
    (is (<= (count-string-bytes (nth @a 1)) 100))
    (is (<= (count-string-bytes (nth @a 2)) 8092))))

(deftest sns-publisher-test
  (let [a (promise)
        sns (sns-publisher fake-aws-opts)
        stream (sns "test:arn")]
    (with-redefs [clj-aws.sns/publish #(deliver a [%2 %3 %4])]
      (stream fake-event))
    (is (= @a ["test:arn" fake-event-subject fake-event-body]))))

(deftest ^:sns ^:integration sns-test
  (when-not env-access-key-id
    (println "export AWS_ACCESS_KEY_ID=\"...\" to run :sns tests."))
  (when-not env-secret-access-key
    (println "export AWS_SECRET_ACCESS_KEY=\"...\" to run :sns tests."))
  (when-not env-region
    (println "export AWS_REGION=\"...\" to run :sns tests."))
  (when-not env-arn
    (println "export AWS_SNS_TOPIC=\"...\" to run :sns tests."))

  (let [sns (sns-publisher {:access-key env-access-key-id
                            :secret-key env-secret-access-key
                            :region env-region})
        stream (sns env-arn)
        event (merge fake-event {:time (unix-time)})]
    (stream event)))
