(ns riemann.slack-test
  (:require [riemann.logging :as logging]
            [riemann.slack :as slack]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.test :refer :all]))


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

      (testing "formats event by default with default formatter"
        (let [slacker (slack/slack "any" "any" "test-user" "#test-channel")]
          (slacker {:host "localhost", :service "mailer", :state "error",
                    :description "Mailer failed", :metric 42, :tags ["first", "second"]})
          (is (= (json/parse-string (:body @post-request))
                 {"attachments" [{"fields" [{"title" "Riemann Event"
                                             "value" "Host:   localhost\nService:   mailer\nState:   error\nDescription:   Mailer failed\nMetric:   42\nTags:   [\"first\" \"second\"]\n" "short" true}]
                                  "fallback" "*Host:* localhost *Service:* mailer *State:* error *Description:* Mailer failed *Metric:* 42"}]
                  "channel" "#test-channel"
                  "username" "test-user"
                  "icon_emoji" ":warning:"}))))

      (testing "formats event with bundled extended formatter"
        (let [slacker (slack/slack any-account (with-formatter slack/extended-formatter))]
          (slacker {:host "localhost", :service "mailer", :state "error",
                    :description "Mailer failed", :metric 42, :tags ["first", "second"]})
          (is (= (json/parse-string (:body @post-request))
                  {"text" "This event requires your attention!"
                   "attachments" [{"text" "Mailer failed"
                                   "pretext" "Event Details:"
                                   "color" "warning"
                                   "fields" [{"title" "Host" "value" "localhost" "short" true}
                                             {"title" "Service" "value" "mailer" "short" true}
                                             {"title" "Metric" "value" 42 "short" true}
                                             {"title" "State" "value" "error" "short" true}
                                             {"title" "Description" "value" "Mailer failed" "short" true}
                                             {"title" "Tags" "value" "[\"first\" \"second\"]" "short" true}]
                                   "fallback" "*Host:* localhost *Service:* mailer *State:* error *Description:* Mailer failed *Metric:* 42"}]
                   "channel" "any"
                   "username" "any"
                   "icon_emoji" ":warning:"}))))

      (testing "allows formatting characters in main message text with custom formatter"
        (let [formatter (fn [e] {:text (str "<http://health.check.api/" (:service e) "|" (:service e) ">")})
              slacker (slack/slack any-account (with-formatter formatter))]
          (slacker {:service "my-service"})
          (is (seq (re-seq #"<http://health\.check\.api/my-service\|my-service>" (:body @post-request))))))

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
          (is (= (json/parse-string (:body @post-request))
                 {"channel" "#test-channel"
                  "username" "test-user"
                  "icon_emoji" ":ogre:"}))))

      (testing "formats multiple events with a custom formatter"
        (let [slacker (slack/slack
                        {:account "any", :token "any"}
                        {:username "test-user", :channel "#test-channel", :icon ":ogre:"
                         :formatter (fn [events]
                                      {:text (apply str (map #(str (:tags %)) events))
                                       :icon ":ship:"
                                       :username "another-user"
                                       :channel "#another-channel"
                                       :attachments [{:pretext "pretext"}]})})]
          (slacker [{:host "localhost", :service "mailer", :tags ["first" "second"]}
                    {:host "localhost", :service "mailer", :tags ["third" "fourth"]}])
          (is (= (json/parse-string (:body @post-request))
                 {"attachments" [{"pretext" "pretext"}]
                  "text" "[\"first\" \"second\"][\"third\" \"fourth\"]"
                  "channel" "#another-channel"
                  "username" "another-user"
                  "icon_emoji" ":ship:"})))))))
