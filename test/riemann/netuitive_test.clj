(ns riemann.netuitive-test
  (:use riemann.netuitive
        clojure.test))

(def test-event {:host "riemann.local" :service "netuitive test" :state "ok" :description "Successful test" :metric 2 :time (/ (System/currentTimeMillis) 1000) :tags ["riemann" "netuitive"]})
(def other-event {:host "riemann.local" :service "netuitive other" :state "ok" :description "Successful test" :metric 5 :time (/ (System/currentTimeMillis) 1000) :tags ["other"]})

(deftest ^:netuitive netuitive-unit-tests
   (is (= (:type (generate-event test-event {})) "Riemann"))
   (is (= (:id (generate-event test-event {})) "Riemann:riemann.local"))
   (is (= (:name (generate-event test-event {})) "riemann.local"))
   (is (= (:metrics (generate-event test-event {})) #{{:id "netuitive.test"}}))
   (is (= (:metricId (first (:samples (generate-event test-event {}))) "netuitive.test")))
   (is (= (:val (first (:samples (generate-event test-event {}))) 2)))
   (is (= (:name (first (:tags (generate-event test-event {})))) "netuitive"))
   (is (= (:name (generate-tag "tagname") "tagname")))
   (is (= (:name (second (:tags (generate-event test-event {}))) "riemann")))
   (is (= (:type (generate-event test-event {:type "SERVER"})) "SERVER"))
   (is (= (netuitive-metric-name test-event) "netuitive.test"))

   (is (= (:name (combine-elements (generate-event test-event {}) (generate-event other-event {}))) "riemann.local"))
   (is (= (:id (combine-elements (generate-event test-event {}) (generate-event other-event {}))) "Riemann:riemann.local"))
   (is (= (:type (combine-elements (generate-event test-event {}) (generate-event other-event {}))) "Riemann"))

   (is (= (:metrics (combine-elements (generate-event test-event {}) (generate-event test-event {}))) #{{:id "netuitive.test"}}))
   (is (= (:tags (combine-elements (generate-event test-event {}) (generate-event test-event {}))) #{{:name "netuitive", :value :true} {:name "riemann", :value :true}}))
   (is (= (count (:samples (combine-elements (generate-event test-event {}) (generate-event test-event {})))) 2))

   (is (= (:metrics (combine-elements (generate-event test-event {}) (generate-event other-event {}))) #{{:id "netuitive.other"} {:id "netuitive.test"}}))
   (is (= (:tags (combine-elements (generate-event test-event {}) (generate-event other-event {}))) #{{:name "netuitive", :value :true} {:name "other", :value :true} {:name "riemann", :value :true}}))
   (is (= (count (:samples (combine-elements (generate-event test-event {}) (generate-event other-event {})))) 2)))

(deftest ^:integration ^:netuitive netuitive-test
   (let [k (netuitive {:api-key "netuitive-test-key" :url "https://api.app.netuitive.com/ingest/"})]
     (k test-event))
   (let [k (netuitive {:api-key "netuitive-test-key"})]
     (k test-event)))
