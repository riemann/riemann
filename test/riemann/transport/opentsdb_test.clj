(ns riemann.transport.opentsdb-test
  (:require [riemann.common :refer [event]]
            [riemann.core :as core]
            [riemann.opentsdb :as client]
            [riemann.logging :as logging]
            [riemann.pubsub :as pubsub]
            [riemann.transport.opentsdb :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.test :refer :all]
            [slingshot.slingshot :refer [try+]]))

(deftest decode-opentsdb-line-success-test
  (is (= (event {:service "name" :description "name" :metric 456.0 :time 123})
         (decode-opentsdb-line "put name 123 456")))
  (is (= (event {:host "host" :service "name" :description "name" :metric 456.0 :time 123})
         (decode-opentsdb-line "put name 123 456 host=host")))
  (is (= (event {:service "name tag=value" :description "name" :metric 456.0 :tag "value" :time 123})
         (decode-opentsdb-line "put name 123 456 tag=value")))
  (is (= (event {:service "name tag=value tag2=value2" :description "name" :metric 456.0 :tag "value" :tag2 "value2" :time 123})
         (decode-opentsdb-line "put name 123 456 tag=value tag2=value2")))
  (is (= (event {:service "name service=value" :description "name" :metric 456.0 :servicetag "value" :time 123})
        (decode-opentsdb-line "put name 123 456 service=value")))
)

(deftest decode-opentsdb-line-failure-test
  (let [err #(try+ (decode-opentsdb-line %)
                   (catch Object e e))]
    (is (= (err "") "blank line"))
    (is (= (err "version") "version request"))
    (is (= (err "put") "no metric name"))
    (is (= (err "put name") "no timestamp"))
    (is (= (err "put name 123") "no metric"))
    (is (= (err "put name 123 NaN") "NaN metric"))
    (is (= (err "put name 123 metric") "invalid metric"))
    (is (= (err "put name timestamp 123") "invalid timestamp"))))

(deftest round-trip-test
  (riemann.logging/suppress ["riemann.transport"
                             "riemann.pubsub"
                             "riemann.opentsdb"
                             "riemann.core"]
    (let [server (opentsdb-server)
          sink   (promise)
          core   (core/transition! (core/core)
                                   {:services [server]
                                    :streams  [(partial deliver sink)]})]
      (try
        ; Open a client and send an event
        (let [client (client/opentsdb {:pool-size 1 :block-start true})]
          (client {:host "computar"
                   :service "hi there" :metric 2.5 :time 123 :ttl 10})

          ; Verify event arrives
          (is (= (deref sink 1000 :timed-out)
                 (event {:host "computar"
                         :service "hi.there"
                         :state nil
                         :description "hi.there"
                         :metric 2.5
                         :tags nil
                         :time 123
                         :ttl nil}))))
        (finally
          (core/stop! core))))))
