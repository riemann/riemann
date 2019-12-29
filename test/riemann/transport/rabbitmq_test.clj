(ns riemann.transport.rabbitmq-test
  (:require [riemann.common :refer [encode event map-matches?]]
            [riemann.config :refer [service!]]
            [riemann.core :as core]
            [riemann.transport.rabbitmq :refer :all]
            [clojure.test :refer :all]
            [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.exchange :as le]
            [langohr.basic :as lb]))

(def ^:const events
  {:bee {:host "thisone"
         :service "busybee"
         :state "ok"
         :description "buzz"
         :metric 42.0},
   :cat {:host "thisone"
         :service "sleepycat"
         :state "ok"
         :description "meow"
         :metric 9.0}})

(deftest ^:rabbitmq valid-protobuf-payload-decodes-into-a-message
  (let [pb (encode {:events (map event (vals events))})
        result (pb->msg pb)
        [e1 e2] (:events result)]
    (is (map-matches? (:bee events) e1))
    (is (map-matches? (:cat events) e2))
    (is (contains? result :decode-time))))

(deftest ^:rabbitmq ^:integration rabbitmq-transport-integration-test
  (riemann.logging/suppress ["riemann.transport"
                             "riemann.pubsub"
                             "riemann.core"]
    (let [host "rabbitmq.local"
          ex-name "riemann-test"
          ex-type "topic"
          routing-key "test"
          transport (rabbitmq-transport {:host host
                                         :ex-name ex-name
                                         :ex-type ex-type
                                         :routing-key routing-key})
          sink (promise)
          core (core/transition! (core/core) {:services [transport]
                                              :streams [(partial deliver sink)]})
          conn (atom nil)
          ch (atom nil)
          payload (encode {:events [(event (:bee events))]})]
      (try
        (reset! conn (rmq/connect {:host host}))
        (reset! ch (lch/open @conn))
        (le/declare @ch ex-name ex-type {:durable false :auto-delete true})
        (lb/publish @ch ex-name routing-key payload {:content-type "application/octet-stream"
                                                     :type "riemann.test"})
        (is (map-matches? (:bee events) (deref sink 1000 :timed-out)))
        (finally
          (core/stop! core)
          (when @conn
            (rmq/close @ch)
            (rmq/close @conn)))))))
