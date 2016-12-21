(ns riemann.netuitive-test
  (:use riemann.netuitive
        clojure.test)
  )

(def test-event {:host "riemann.local" :service "netuitive test" :state "ok" :description "Successful test" :metric 2 :time (/ (System/currentTimeMillis) 1000)})

(deftest ^:netuitive ^:integration netuitive-test

   (let [k (netuitive {:api-key "netuitive-test-key" :url "https://api.app.netuitive.com/ingest/"})]
     (k test-event))
		   
   (let [k (netuitive {:api-key "netuitive-test-key"})]
     (k test-event))
		   
   (is (= (:type (generate-event test-event {})) "Riemann"))
   
   (is (= (:type (generate-event test-event {:type "SERVER"})) "SERVER"))
   
   (is (= (netuitive-metric-name test-event) "netuitive.test")) 
)