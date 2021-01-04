(ns riemann.rabbitmq-test
  (:require [riemann.logging :as logging]
            [riemann.rabbitmq :refer :all]
            [riemann.test-utils :refer [with-mock]]
            [clojure.test :refer :all]
            [langohr.core :as rmq]
            [langohr.queue :as lq]
            [langohr.channel :as lch]
            [langohr.exchange :as le]
            [langohr.consumers :as lc]
            [langohr.basic :as lb]))

(logging/init)

(defn- gen-query-result-handler
  [query-result]
  (fn [_ _ ^bytes payload]
    (deliver query-result (String. payload "UTF-8"))))

(deftest ^:rabbitmq ^:integration rabbitmq-stream-integration-test
  (let [conn (atom nil)
        ch (atom nil)]
    (try
      (let [host "rabbitmq.local"
            rmq (rabbitmq {:host host})
            ex-name "riemann-test"
            routing-key "riemann.events.test"
            s (rmq {:exchange-settings {:name ex-name :auto-delete true}
                    :routing-key routing-key})
            e {:host "localhost"
               :service "busybee"
               :metric 9000
               :ttl 10}
            query-result (promise)]
        (reset! conn (rmq/connect {:host host}))
        (reset! ch (lch/open @conn))
        (le/declare @ch ex-name "topic" {:auto-delete true})
        (let [q-name (.getQueue (lq/declare @ch "" {:exclusive true}))]
          (lq/bind @ch q-name ex-name {:routing-key routing-key})
          (lc/subscribe @ch q-name (gen-query-result-handler query-result) {:auto-ack true}))
        (s e)
        (let [result (deref query-result 1000 :timed-out)]
          (is (= result "{\"host\":\"localhost\",\"service\":\"busybee\",\"metric\":9000,\"ttl\":10}"))))
      (finally
        (when @conn
          (rmq/close @ch)
          (rmq/close @conn))))))

(deftest ^:rabbitmq rabbitmq-test
  (with-mock [connect' rmq/connect]
  (with-mock [open' lch/open]
  (with-mock [declare' le/declare]
  (with-mock [publish' lb/publish]
    (testing "default options"
      (let [rmq (rabbitmq)
            s (rmq)
            e {:host "a"
               :service "b"
               :description "c"
               :metric 42
               :state "ok"}]
        (s e)
        (is (= 1 (count @connect')))
        (is (= 1 (count @open')))
        (is (= 1 (count @declare')))
        (is (= 1 (count @publish')))
        (let [[_ ex-name ex-type _] (last @declare')]
          (is (= ex-name "riemann"))
          (is (= ex-type "topic")))
        (let [[_ ex-name routing-key payload {:keys [content-type]}] (last @publish')]
          (is (= ex-name "riemann"))
          (is (= routing-key "riemann.events"))
          (is (= payload "{\"host\":\"a\",\"service\":\"b\",\"description\":\"c\",\"metric\":42,\"state\":\"ok\"}"))
          (is (= content-type "application/json")))))
    (testing "custom options"
      (let [rmq (rabbitmq {:host "example.com" :port 1234})
            formatter #(:metric %)
            s (rmq {:exchange-settings {:name "bus" :type "custom" :durable true}
                    :routing-key "one.two.three"
                    :message-properties {:content-type "test"}
                    :message-formatter formatter})
            e {:host "a"
               :service "b"
               :description "c"
               :metric 42
               :state "ok"}]
        (s e)
        (is (= 2 (count @connect')))
        (is (= 2 (count @open')))
        (is (= 2 (count @declare')))
        (is (= 2 (count @publish')))
        (let [[{:keys [host port]}] (last @connect')]
          (is (= host "example.com"))
          (is (= port 1234)))
        (let [[_ ex-name ex-type {:keys [durable]}] (last @declare')]
          (is (= ex-name "bus"))
          (is (= ex-type "custom"))
          (is (= durable true)))
        (let [[_ ex-name routing-key payload {:keys [content-type]}] (last @publish')]
          (is (= ex-name "bus"))
          (is (= routing-key "one.two.three"))
          (is (= payload 42))
          (is (= content-type "test")))))
    (testing "routing key as a function"
      (let [rmq (rabbitmq)
            s (rmq {:routing-key #(str (:service %) "." (:description %))})
            e {:host "a"
               :service "b"
               :description "c"
               :metric 42
               :state "ok"}]
        (s e)
        (is (= 3 (count @publish')))
        (let [[_ ex-name routing-key payload {:keys [content-type]}] (last @publish')]
          (is (= ex-name "riemann"))
          (is (= routing-key "b.c"))
          (is (= payload "{\"host\":\"a\",\"service\":\"b\",\"description\":\"c\",\"metric\":42,\"state\":\"ok\"}"))
          (is (= content-type "application/json"))))))))))
