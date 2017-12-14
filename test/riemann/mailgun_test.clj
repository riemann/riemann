(ns riemann.mailgun-test
  (:require [riemann.common :refer [body]]
            [riemann.mailgun :as mailgun]
            [clj-http.client :as client]
            [clojure.test :refer [deftest testing is are]]))

(defn- mock-post [result-atom email event]
  (with-redefs [client/post (fn [url opts]
                              (reset! result-atom
                                      (merge (:form-params opts)
                                             {:url url
                                              :basic-auth (:basic-auth opts)})))]
    (email event)))

(deftest mailgun-test
  (let [result-atom (atom {})
        sandbox "mail.relay"
        service-key "testkey"
        recipient "somedude@somewhere.com"
        event {:service "testservice"
               :host "test host"
               :time 123
               :metric 17}
        default-body-result (body [event])
        default-subject-result "test host testservice"
        mailer (mailgun/mailgun {:sandbox sandbox
                                 :service-key service-key})
        email (mailer recipient)]

    (testing "ensure the data posted to mailgun matches expectations"
      (mock-post result-atom email event)
      (are [key result] (= result (key @result-atom))
           :url (str "https://api.mailgun.net/v2/" sandbox "/messages")
           :basic-auth ["api" service-key]
           :from (str "Riemann <riemann@" sandbox ">")
           :to (list recipient)
           :subject default-subject-result
           :text default-body-result))

    (testing "ensure the data posted to mailgun contains html field when body formatter returns html map"
      (let [html-content-result "<h1>HTML</h1>"
            body-formatter-result {:type :html
                                   :content html-content-result}
            body-formatter (fn [_] body-formatter-result)
            mailer (mailgun/mailgun {:sandbox sandbox
                                     :service-key service-key}
                                    {:body body-formatter})
            email (mailer recipient)]
        (mock-post result-atom email event)
        (are [key result] (= result (key @result-atom))
             :url (str "https://api.mailgun.net/v2/" sandbox "/messages")
             :basic-auth ["api" service-key]
             :from (str "Riemann <riemann@" sandbox ">")
             :to (list recipient)
             :subject default-subject-result
             :html html-content-result)))

    (testing "ensure message overrides are used"
      (let [body-formatter-result "this is the body"
            body-formatter (fn [_] body-formatter-result)
            subject-formatter-result "this is the subject"
            subject-formatter (fn [_] subject-formatter-result)
            from-override "my-override@xanadu"
            mailer (mailgun/mailgun {:sandbox sandbox
                                     :service-key service-key}
                                    {:subject subject-formatter
                                     :body body-formatter
                                     :from from-override})
            email (mailer recipient)]
        (mock-post result-atom email event)
        (are [rkey result] (= result (rkey @result-atom))
           :subject subject-formatter-result
           :text body-formatter-result
           :from from-override)))

    (testing "ensure mailgun options are split out when given only one map"
      (let [from-override "my-override@xanadu"
            mailer (mailgun/mailgun {:sandbox sandbox
                                     :service-key service-key
                                     :from from-override})
            email (mailer recipient)]
        (mock-post result-atom email event)
        (is (= (:from @result-atom)
               from-override))))))
