(ns riemann.transport.graphite-test
  (:require [riemann.common :refer [event]]
            [riemann.core :as core]
            [riemann.graphite :as client]
            [riemann.logging :as logging]
            [riemann.transport.graphite :refer :all]
            [clojure.test :refer :all]
            [slingshot.slingshot :refer [try+]]))

(deftest decode-graphite-line-success-test
  (is (= (event {:service "name", :metric 123.0, :time 456})
         (decode-graphite-line "name 123 456")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name\t456\t789")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name\t 456\t 789")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name\t\t456\t\t789")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name\t\t456 789")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name 456\t789")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name\t456 789")))
  (is (= (event {:service "name", :metric 456.0 :time 789})
         (decode-graphite-line "name  456      789"))))

(deftest decode-graphite-line-failure-test
  (let [err #(try+ (decode-graphite-line %)
                   (catch Object e e))]
    (is (= (err "") "blank line"))
    (is (= (err "name nan 456") "NaN metric"))
    (is (= (err "name metric 456") "invalid metric"))
    (is (= (err "name 123 timestamp") "invalid timestamp"))
    (is (= (err "name with space 123 456") "too many fields"))
    (is (= (err "name\twith\ttab\t123\t456") "too many fields"))
    (is (= (err "name with space\tand\ttab 123\t456") "too many fields"))
    (is (= (err "name\t\t123\t456\t\t\t789") "too many fields"))))

(deftest round-trip-test
  (riemann.logging/suppress ["riemann.transport"
                             "riemann.pubsub"
                             "riemann.graphite"
                             "riemann.core"]
    (let [server (graphite-server)
          sink   (promise)
          core   (core/transition! (core/core)
                                   {:services [server]
                                    :streams  [(partial deliver sink)]})]
      (try
        ; Open a client and send an event
        (let [client (client/graphite {:pool-size 1 :block-start true})]
          (client {:host "computar"
                   :service "hi there" :metric 2.5 :time 123 :ttl 10})

          ; Verify event arrives
          (is (= (deref sink 1000 :timed-out)
                 (event {:host        nil
                         :service     "computar.hi.there"
                         :state       nil
                         :description nil
                         :metric      2.5
                         :tags        nil
                         :time        123
                         :ttl         nil}))))
        (finally
          (core/stop! core))))))

(deftest round-trip-test-udp
  (riemann.logging/suppress ["riemann.transport"
                             "riemann.pubsub"
                             "riemann.graphite"
                             "riemann.core"]
         (let [server (graphite-server {:pool-size 1 :block-start true :protocol :udp})
          sink   (promise)
          core   (core/transition! (core/core)
                                   {:services [server]
                                    :streams  [(partial deliver sink)]})]
      (try
        ; Open a client and send an event
        (let [client (client/graphite {:protocol :udp})]
          (client {:host "computar"
                   :service "hi there" :metric 2.5 :time 123 :ttl 10})
          ; Verify event arrives
          (is (= (deref sink 1000 :timed-out)
                 (event {:host        nil
                         :service     "computar.hi.there"
                         :state       nil
                         :description nil
                         :metric      2.5
                         :tags        nil
                         :time        123
                         :ttl         nil}))))
        (finally
          (core/stop! core))))))
