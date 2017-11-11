(ns riemann.blueflood-test
  (:require [riemann.blueflood :refer :all]
            [riemann.config :refer [apply!]]
            [riemann.logging :as logging]
            [riemann.streams :refer :all]
            [riemann.test :refer [run-stream]]
            [riemann.time.controlled :refer :all]
            [riemann.time :refer :all]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.test :refer :all]))

(logging/init)

(use-fixtures :once control-time!)
(use-fixtures :each reset-time!)

; These tests assume you've got blueflood running on the localhost
; Fix the host:port below if that's not the case
(def query-url-template
  (str "http://localhost:20000/v2.0/tenant-id/views/%s.%s?"
       "from=000000000&to=1500000000&resolution=FULL"))

(defn test-helper [opts]
  (let [service (str (java.util.UUID/randomUUID))
        host "a"
        query-url (format query-url-template host service)
        timestamp 3
        value 3
        input
        [{:host host
          :service service
          :metric value
          :time timestamp}

         ;; This second event doesn't get included in the batch but
         ;; the timestamp causes the the batch to complete and be sent
         ;; to blueflood with just the first event.
         {:time (inc timestamp)}]
        stream (blueflood-ingest opts prn)]
    ;; Create the async executor
    (apply!)
    ;; Feed the input into BF
    (run-stream (sdo stream) input)
    (Thread/sleep 300)
    ;; Read it back from BF
    (is (= (as-> (client/get query-url) x
                 (:body x)
                 (json/parse-string x)
                 (x "values"))
           [{"numPoints" 1, "timestamp" timestamp, "average" value}]))))

(deftest ^:blueflood ^:integration blueflood-ingest-test
  ;; test synchronously
  (test-helper {})
  ;; test asynchronously
  (test-helper {:async-queue-name :testq}))


