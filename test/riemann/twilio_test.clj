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
               from-override))))

    (testing "do not use default :from if :messaging-service-sid is set "
      (let [messenger (twilio/twilio {:account account
                                      :service-key service-key
                                      :messaging-service-sid "messaging-service"})
            sms (messenger recipient)]
        (mock-post result-atom sms event)
        (are [rkey result] (= result (rkey @result-atom))
          :Body default-body-result
          :To (list recipient)
          :From nil
          :MessagingServiceSid "messaging-service")))

    (testing "test optionals parameters with one map"
      (let [messenger (twilio/twilio {:account account
                                      :service-key service-key
                                      :from "+15005550006"
                                      :messaging-service-sid "messaging-service"
                                      :media-url "media"
                                      :status-callback "callback"
                                      :application-sid "application-sid"
                                      :max-price "max-price"
                                      :provide-feedback "feedback"})
            sms (messenger recipient)]
        (mock-post result-atom sms event)
        (are [rkey result] (= result (rkey @result-atom))
          :Body default-body-result
          :To (list recipient)
          :From "+15005550006"
          :MessagingServiceSid "messaging-service"
          :MediaUrl "media"
          :StatusCallback "callback"
          :ApplicationSid "application-sid"
          :MaxPrice "max-price"
          :ProvideFeedback "feedback")))

    (testing "test optionals parameters with 2 maps"
      (let [messenger (twilio/twilio {:account account
                                      :service-key service-key}
                                     {:from "+15005550006"
                                      :messaging-service-sid "messaging-service"
                                      :media-url "media"
                                      :status-callback "callback"
                                      :application-sid "application-sid"
                                      :max-price "max-price"
                                      :provide-feedback "feedback"})
            sms (messenger recipient)]
        (mock-post result-atom sms event)
        (are [rkey result] (= result (rkey @result-atom))
          :Body default-body-result
          :To (list recipient)
          :From "+15005550006"
          :MessagingServiceSid "messaging-service"
          :MediaUrl "media"
          :StatusCallback "callback"
          :ApplicationSid "application-sid"
          :MaxPrice "max-price"
          :ProvideFeedback "feedback")))))

(deftest add-key-body-test
  (is (= (twilio/add-key-body :from :From {:from "foo"} {})
         {:From "foo"}))
  (is (= (twilio/add-key-body :foo :Bar {:foo "foo"} {})
         {:Bar "foo"}))
  (is (= (twilio/add-key-body :foo :Bar {} {})
         {})))

(deftest get-form-params-test
  (is (= {:To "to"
          :Body "body"}
         (twilio/get-form-params {:to "to"
                                  :body "body"})))
  (is (= {:To "bar"
          :Body "body"
          :From "from"}
         (twilio/get-form-params {:to "bar"
                                  :body "body"
                                  :from "from"})))
  (is (= {:To "bar"
          :Body "body"
          :From "from"
          :MessagingServiceSid "messaging-service"
          :MediaUrl "media"
          :StatusCallback "callback"
          :ApplicationSid "application-sid"
          :MaxPrice "max-price"
          :ProvideFeedback "feedback"}
         (twilio/get-form-params {:to "bar"
                                  :body "body"
                                  :from "from"
                                  :messaging-service-sid "messaging-service"
                                  :media-url "media"
                                  :status-callback "callback"
                                  :application-sid "application-sid"
                                  :max-price "max-price"
                                  :provide-feedback "feedback"}))))
