(ns riemann.kafka-test
  (:use riemann.kafka
        [riemann.time :only [unix-time]]
        [riemann.common :only [event]]
        clojure.test)
  (:require [kinsky.client :as client]
            [riemann.logging :as logging]
            [riemann.core :as core]))

(logging/init)

(def ^:const first-event {:service "firstservice"
                          :host "myhost"
                          :metric 1.0
                          :state "critical"
                          :time (unix-time)})

(def ^:const second-event {:service "secondservice"
                           :host "myhost"
                           :metric 2.0
                           :state "warning"
                           :time (unix-time)})

; These tests assume that you have a single server kafka instance running on localhost:9092 (plain) 
; and localhost:9093 (SSL) and the topics  "riemann" and "custom" configured:
; ./bin/kafka-topics --create --topic riemann/custom --partitions 1 --replication-factor 1 --zookeeper localhost:2181
;
; Important Kafka server.properties settings:
; listeners=PLAINTEXT://localhost:9092,SSL://localhost:9093
; ssl.keystore.location=<path_to_riemann>/riemann/test/data/kafka/kafka.server.keystore.jks
; ssl.keystore.password=test1234
; ssl.key.password=test1234
; ssl.truststore.location=<path_to_riemann>/riemann/test/data/kafka/kafka.server.truststore.jks
; ssl.truststore.password=test1234

(deftest ^:kafka ^:integration kafka-integration-plain-test
  (let [consumer (kafka-consumer {:consumer.config {:bootstrap.servers "localhost:9092"
                                                    :group.id "plaingroup"
                                                    :auto.commit.interval.ms 100}
                                  :poll.timeout.ms 1000})
        sink (promise)
        core (core/transition! (core/core)
                               {:services [consumer]
                                :streams [(partial deliver sink)]})
        producer (kafka)
        kafka-output (producer "riemann" "mykey")]
    (try 
      (testing "kafka plain with json serializer, riemann topic and custom key"
        ; Producer writes to riemann topic
        (kafka-output first-event)
        ; Verify event arrives
        (let [event (deref sink 1000 :timed-out)]
          (is (= "firstservice"
                 (:service event)))
          (is (= "myhost"
                 (:host event)))
          (is (= 1.0
                 (:metric event)))
          (is (= "critical"
                 (:state event)))))
      (finally 
        ; Wait for kafka auto commit
        (Thread/sleep 1000)
        (core/stop! core)))))

(deftest ^:kafka ^:integration kafka-integration-ssl-test
  (let [consumer (kafka-consumer {:consumer.config {:bootstrap.servers "localhost:9093"
                                                    :security.protocol "SSL"
                                                    :ssl.truststore.location "test/data/kafka/kafka.client.truststore.jks"
                                                    :ssl.truststore.password "test1234"
                                                    :group.id "sslgroup"
                                                    :auto.commit.interval.ms 100}
                                  :topics ["custom"]
                                  :value.deserializer client/edn-deserializer
                                  :poll.timeout.ms 1000})
        sink (promise)
        core (core/transition! (core/core)
                               {:services [consumer]
                                :streams [(partial deliver sink)]})
        producer (kafka {:bootstrap.servers "localhost:9093"
                         :security.protocol "SSL"
                         :ssl.truststore.location "test/data/kafka/kafka.client.truststore.jks"
                         :ssl.truststore.password "test1234"
                         :value.serializer client/edn-serializer})
        kafka-output (producer "custom")]
    (try
      (testing "kafka ssl with edn serializer, custom topic and nil key"
        (kafka-output second-event)
        (let [event (deref sink 1000 :timed-out)]
          (is (= "secondservice" 
                 (:service event)))
          (is (= "myhost" 
                 (:host event)))
          (is (= 2.0 
                 (:metric event)))
          (is (= "warning" 
                 (:state event)))))
      (finally
        ; Wait for kafka auto commit
        (Thread/sleep 1000)
        (core/stop! core)))))

