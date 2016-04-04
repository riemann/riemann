(ns riemann.twilio-test
  (:require [riemann.twilio :as twilio]
            [riemann.common :refer [body]]
            [clj-http.client :as client]
            [clojure.test :refer [deftest testing is are]]))

(defn- mock-post [result-atom sms event]
  (with-redefs [client/post (fn [url opts]
                              (reset! result-atom
                                      (merge (:form-params opts)
                                             {:url url
                                              :basic-auth (:basic-auth opts)})))]
    (sms event)))

(deftest twilio-test
  (let [result-atom (atom {})
        account "testaccount"
        service-key "testkey"
        recipient "+15005550006"
        event {:service "testservice"
               :host "test host"
               :time 123
               :metric 17}
        default-body-result (body [event])
        messenger (twilio/twilio {:account account
                                 :service-key service-key})
        sms (messenger recipient)]

    (testing "ensure the data posted to twilio matches expectations"
      (mock-post result-atom sms event)
      (are [key result] (= result (key @result-atom))
           :url (str "https://api.twilio.com/2010-04-01/Accounts/" account "/Messages.json")
           :basic-auth [account service-key]
           :From (str "+15005550006")
           :To (list recipient)
           :Body default-body-result))

    (testing "ensure message overrides are used"
      (let [body-formatter-result "this is the body"
            body-formatter (fn [_] body-formatter-result)
            from-override "+15005550006"
            messenger (twilio/twilio {:account account
                                     :service-key service-key}
                                    {:body body-formatter
                                     :from from-override})
            sms (messenger recipient)]
        (mock-post result-atom sms event)
        (are [rkey result] (= result (rkey @result-atom))
           :Body body-formatter-result
           :From from-override)))

    (testing "ensure twilio options are split out when given only one map"
      (let [from-override "+15005550006"
            messenger (twilio/twilio {:account account
                                     :service-key service-key
                                     :from from-override})
            sms (messenger recipient)]
        (mock-post result-atom sms event)
        (is (= (:From @result-atom)
               from-override))))))
