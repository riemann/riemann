(ns riemann.msteams-test
  (:require [riemann.test-utils :refer [with-mock]]
            [riemann.logging :as logging]
            [riemann.msteams :refer :all]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.test :refer :all]))

(logging/init)

(def test-event
  {:service "testservice1"
   :host "testhost1"
   :state "ok"
   :metric 1.0
   :description "testdescription1"
   :tags ["tag1" "tag2"]})

(def test-events
  [{:service "testservice1"
    :host "testhost1"
    :state "ok"
    :metric 1.0
    :description "testdescription1"
    :tags ["tag1" "tag2"]}
   {:service "testservice2"
    :host "testhost2"
    :state "critical"
    :metric 10
    :description "testdescription2"
    :tags ["tag3" "tag4"]}])

(defn- my-custom-formatter [events]
  {(keyword "@type") "MessageCard"
   (keyword "@context") "http://schema.org/extensions"
   :title "Riemann"
   :summary "Received alert"
   :text (str "* <h1>Service:</h1>" (:service (first events))
              "* <h1>Host:</h1>" (:host (first events))
              "* <h1>Metric:</h1>" (:host (first events))
              "* <h1>State:</h1>" (:host (first events)))})

(deftest ^:msteams msteams-test
  (with-mock [calls clj-http.client/post]
    (let [mst (msteams {:url "http://msteams.com"})
          mst-custom (msteams {:url "http://msteams-custom.com" :formatter my-custom-formatter})]

      (testing "event with default formatter"
        (mst test-event)
        (is (= (:body (nth (last @calls) 1))
               (json/generate-string 
                 {(keyword "@type") "MessageCard"
                  (keyword "@context") "http://schema.org/extensions"
                  :title "Riemann Alerting",
                  :summary "Received alerts from Riemann"
                  :sections [
                             {:title "testservice1 is ok"
                              :facts [
                                      {:name "Service"
                                       :value "testservice1"}
                                      {:name "Host"
                                       :value "testhost1"}
                                      {:name "Metric"
                                       :value 1.0}
                                      {:name "State"
                                       :value "ok"}
                                      {:name "Description"
                                       :value "testdescription1"}
                                      {:name "Tags"
                                       :value "tag1, tag2"}]}]}))))
      
      (testing "events with default formatter"
        (mst test-events)
        (is (= (:body (nth (last @calls) 1))
               (json/generate-string 
                 {(keyword "@type") "MessageCard"
                  (keyword "@context") "http://schema.org/extensions"
                  :title "Riemann Alerting",
                  :summary "Received alerts from Riemann"
                  :sections [
                             {:title "testservice1 is ok"
                              :facts [
                                      {:name "Service"
                                       :value "testservice1"}
                                      {:name "Host"
                                       :value "testhost1"}
                                      {:name "Metric"
                                       :value 1.0}
                                      {:name "State"
                                       :value "ok"}
                                      {:name "Description"
                                       :value "testdescription1"}
                                      {:name "Tags"
                                       :value "tag1, tag2"}
                                      ]}
                             {:title "testservice2 is critical"
                              :facts [
                                      {:name "Service"
                                       :value "testservice2"}
                                      {:name "Host"
                                       :value "testhost2"}
                                      {:name "Metric"
                                       :value 10}
                                      {:name "State"
                                       :value "critical"}
                                      {:name "Description"
                                       :value "testdescription2"}
                                      {:name "Tags"
                                       :value "tag3, tag4"}]}]}))))
          
      (testing "events with custom formatter"
        (mst-custom test-events)
        (is (= (:body (nth (last @calls) 1))
               (json/generate-string 
                 {(keyword "@type") "MessageCard"
                  (keyword "@context") "http://schema.org/extensions"
                  :title "Riemann",
                  :summary "Received alert"
                  :text "* <h1>Service:</h1>testservice1* <h1>Host:</h1>testhost1* <h1>Metric:</h1>testhost1* <h1>State:</h1>testhost1"})))))))
