(ns riemann.sns-test
  (:import [com.amazonaws.services.sns.model PublishResult])
  (:use [riemann.time :only [unix-time]]
        [riemann.common :only [time-at count-string-bytes]]
        riemann.sns
        clojure.test))

(def env-access-key-id     (System/getenv "AWS_ACCESS_KEY_ID"))
(def env-secret-access-key (System/getenv "AWS_SECRET_ACCESS_KEY"))
(def env-region            (System/getenv "AWS_REGION"))
(def env-arn               (System/getenv "AWS_SNS_TOPIC"))
(def fake-aws-opts {:access-key "access-key" :secret-key "secret-key" :region "ap-northeast-1"})
(def fake-event {:host "localhost"
                 :service "sns test"
                 :state "ok"
                 :description "all clear, uh, situation normal"
                 :metric 2.71828
                 :time 0})
(def fake-event-subject "localhost sns test ok")
(def fake-event-body (str "At "
                          (time-at 0)
                          "\nlocalhost sns test ok (2.71828)\nTags: []\nCustom Attributes: {}\n\nall clear, uh, situation normal"))

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
    ; delivers: arn body subject
    (with-redefs [riemann.sns/aws-sns-publish #(deliver a [%2 %3 %4])]
      (sns-publish fake-aws-opts
                   {:arn ["my:arn"]}
                   {:service (apply str (repeat 8093 "あ")) :time 0}))
    (is (<= (count-string-bytes (nth @a 1)) 8092))
    (is (<= (count-string-bytes (nth @a 2)) 100))))

(deftest sns-publisher-static-subject-overriding-test
  (let [a (promise)
        sns (sns-publisher fake-aws-opts {:subject "something went wrong"})
        stream (sns "test:arn")]
    (with-redefs [riemann.sns/aws-sns-publish #(deliver a [%2 %3 %4])]
      (stream fake-event))
    (is (= @a ["test:arn" fake-event-body "something went wrong"]))))

(deftest sns-publisher-sync-test
  (let [a (promise)
        sns (sns-publisher fake-aws-opts)
        stream (sns "test:arn")]
    (with-redefs [riemann.sns/aws-sns-publish #(deliver a [%2 %3 %4])]
      (stream fake-event))
    (is (= @a ["test:arn" fake-event-body fake-event-subject]))))

(deftest sns-publisher-async-test
  (let [a (promise)
        done (promise)
        fail (promise)
        sns (sns-publisher (merge fake-aws-opts {:async true}))
        stream (sns "test:arn:async")]
    (with-redefs [riemann.sns/aws-sns-publish-async #(deliver a [%2 %3 %4])]
      (stream fake-event))
    (is (= @a ["test:arn:async" fake-event-body fake-event-subject]))))

(deftest sns-publisher-async-callbacks-test
  (let [a (promise)
        done (promise)
        fail (promise)
        success #(deliver done [%1 %2])
        error #(deliver fail [%1])
        sns (sns-publisher fake-aws-opts {} {:async true :success success :error error})
        stream (sns "test:arn:async:callbacks")]
    (with-redefs [riemann.sns/aws-sns-publish-async #(deliver a [%2 %3 %4 %5 %6])]
      (stream fake-event))
    (is (= @a ["test:arn:async:callbacks" fake-event-body fake-event-subject success error]))))

(deftest sns-publisher-insufficient-async-callbacks-test
  (is (thrown? AssertionError (sns-publisher fake-aws-opts {} {:async true :success #(prn %1 %2)}))))

(deftest ^:sns ^:integration sns-test
  (when-not env-access-key-id
    (println "export AWS_ACCESS_KEY_ID=\"...\" to run :sns tests."))
  (when-not env-secret-access-key
    (println "export AWS_SECRET_ACCESS_KEY=\"...\" to run :sns tests."))
  (when-not env-region
    (println "export AWS_REGION=\"...\" to run :sns tests."))
  (when-not env-arn
    (println "export AWS_SNS_TOPIC=\"...\" to run :sns tests."))

  (let [aws-opts {:access-key env-access-key-id
                  :secret-key env-secret-access-key
                  :region     env-region}
        done (promise)
        fail (promise)
        sns-sync (sns-publisher aws-opts)
        sns-async (sns-publisher aws-opts {} {:async true})
        sns-callbacks (sns-publisher aws-opts {} {:async true
                                                  :success #(deliver done [%2])
                                                  :error #(deliver fail [%1])})
        event (merge fake-event {:time (unix-time)})]
    ((sns-sync env-arn) (merge event {:service "sns sync test"}))
    ((sns-async env-arn) (merge event {:service "sns async test"}))
    ((sns-callbacks env-arn) (merge event {:service "sns async callback test"}))
    ((sns-callbacks (str env-arn ":non:existent")) (merge event {:service "sns async callback test"}))
    (is (instance? PublishResult (first (deref done 10000 nil))))
    (is (instance? Exception (first (deref fail 10000 nil))))))
