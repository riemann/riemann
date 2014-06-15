(ns riemann.slack-test
  (:use clojure.test)
  (:require [riemann.logging :as logging]
            [riemann.slack :as slack]
            [clj-http.client :as client]))


(def api-key (System/getenv "SLACK_API_KEY"))
(def room (System/getenv "SLACK_ALERT_ROOM"))
(def account (System/getenv "SLACK_ALERT_ACCOUNT"))
(def user "Riemann_Slack_Test")

(when-not api-key
  (println "export SLACK_API_KEY=\"...\" to run these tests."))

(when-not room
  (println "export SLACK_ALERT_ROOM=\"...\" to run these tests."))

(when-not account
  (println "export SLACK_ALERT_ACCOUNT=\"...\" to run these tests."))

(logging/init)

(deftest ^:slack ^:integration test_event
  (let [slack_connect (slack/slack account api-key user room)]
    (slack_connect {:host "localhost"
                    :service "good event test"
                    :description "Testing slack.com alerts from riemann"
                    :metric 42
                    :state "ok"})))

(defn- capture-post [result-atom url params]
  (reset! result-atom {:url url, :body (get-in params [:form-params :payload])}))

(def ^:private any-account {:account "any", :token "any"})
(defn- with-formatter [formatter-fn]
  {:username "any", :channel "any", :formatter formatter-fn})

(deftest slack
  (let [post-request (atom {})]
    (with-redefs [client/post (partial capture-post post-request)]

      (testing "forms correct slack URL"
        (let [slacker (slack/slack "test-account" "test-token" "any" "any")]
          (slacker {})
          (is (= (:url @post-request)
                 "https://test-account.slack.com/services/hooks/incoming-webhook?token=test-token"))))

      (testing "formats event by default"
        (let [slacker (slack/slack "any" "any" "test-user" "#test-channel")]
          (slacker {:host "localhost", :service "mailer", :state "error",
                    :description "Mailer failed", :metric 42, :tags ["first", "second"]})
          (is (= (:body @post-request)
                 (str "{\"text\":\"*Host:* localhost "
                                  "*State:* error "
                                  "*Description:* Mailer failed "
                                  "*Metric:* 42\","
                      "\"channel\":\"#test-channel\","
                      "\"username\":\"test-user\","
                      "\"icon_emoji\":\":warning:\"}")))))

      (testing "escapes formatting characters in main message text"
        (let [slacker (slack/slack any-account (with-formatter (fn [e] {:text (:host e)})))]
          (slacker {:host "<>&"})
          (is (seq (re-seq #"&lt;&gt;&amp;" (:body @post-request))))))

      (testing "allows for empty message text"
        (let [slacker (slack/slack any-account (with-formatter (constantly {})))]
          (slacker {:host "empty"})
          (is (= (:body @post-request)
                 (str "{\"channel\":\"any\","
                       "\"username\":\"any\","
                       "\"icon_emoji\":\":warning:\"}")))))

      (testing "specifies username, channel and icon when initializing slacker"
        (let [slacker (slack/slack any-account
                                   {:username "test-user", :channel "#test-channel", :icon ":ogre:"
                                    :formatter (constantly {})})]
          (slacker [{:host "localhost", :service "mailer"}])
          (is (= (:body @post-request)
                 (str "{\"channel\":\"#test-channel\","
                      "\"username\":\"test-user\","
                      "\"icon_emoji\":\":ogre:\"}")))))

      (testing "formats multiple events with a custom formatter"
        (let [slacker (slack/slack {:account "any", :token "any"}
                                   {:username "test-user", :channel "#test-channel", :icon ":ogre:"
                                    :formatter (fn [events] {:text (apply str (map #(str (:tags %)) events))
                                                             :icon ":ship:"
                                                             :username "another-user"
                                                             :channel "#another-channel"
                                                             :attachments [{:pretext "pretext"}]})})]
          (slacker [{:host "localhost", :service "mailer", :tags ["first" "second"]}
                    {:host "localhost", :service "mailer", :tags ["third" "fourth"]}])
          (is (= (:body @post-request)
                 (str "{\"attachments\":[{\"pretext\":\"pretext\"}],"
                       "\"text\":\"[\\\"first\\\" \\\"second\\\"][\\\"third\\\" \\\"fourth\\\"]\","
                       "\"channel\":\"#another-channel\","
                       "\"username\":\"another-user\","
                       "\"icon_emoji\":\":ship:\"}"))))))))
