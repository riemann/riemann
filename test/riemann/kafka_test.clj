(ns riemann.kafka-test
  (:require [riemann.common :refer [event]]
            [riemann.core :as core]
            [riemann.kafka :refer :all]
            [riemann.logging :as logging]
            [riemann.time :refer [unix-time]]
            [clojure.test :refer :all]
            [kinsky.client :as client]))

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

(def consumer-timeout-ms 5000)  ; 5s seems necessary on an average developer workstation
(def burst-event-count 5000000)
(def burst-consumer-timeout-ms 10000)

; These tests assume that you have a single server kafka instance running on localhost:9092 (plain) 
; and localhost:9093 (SSL) and the topics  "riemann", "custom", and "burst" configured:
; $KAFKA_HOME/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --partitions 1 --replication-factor 1 --topic riemann
; $KAFKA_HOME/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --partitions 1 --replication-factor 1 --topic custom
; $KAFKA_HOME/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --partitions 1 --replication-factor 1 --topic burst \
;   --config segment.bytes=$((10*1024*1024)) --config retention.bytes=$((100*1024*1024)) --config compression.type=lz4
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
        (let [event (deref sink consumer-timeout-ms :timed-out)]
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
        (let [event (deref sink consumer-timeout-ms :timed-out)]
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

(defn counting-sink [consumer-counter]
  (fn stream [event] (swap! consumer-counter inc)))

(deftest ^:kafka ^:integration kafka-integration-burst-test
  (let [consumer (kafka-consumer {:consumer.config {:bootstrap.servers "localhost:9092"
                                                    :group.id "burstgroup"
                                                    :auto.commit.interval.ms 1000}
                                  :topics ["burst"]
                                  :poll.timeout.ms 1000})

        consumer-counter (atom 0)
        time-elapsed-ms (atom 0)
        time-interval-ms 1000
        core (core/transition! (core/core)
                               {:services [consumer]
                                :streams [(counting-sink consumer-counter)]})
        producer (kafka)
        kafka-output (producer "burst")]
    (try
      (testing "kafka plain with json serializer and burst topic"
        (dotimes [n burst-event-count] (kafka-output first-event))
        (do
          (while (and (< @time-elapsed-ms burst-consumer-timeout-ms) (< @consumer-counter burst-event-count))
            (do
              (Thread/sleep time-interval-ms)
              (swap! time-elapsed-ms + time-interval-ms)
              (println (str "Consumed events: " @consumer-counter "/" burst-event-count " in " @time-elapsed-ms "ms"
                (if (.running? consumer) " (running)" " (stopped)")))))
          (is (.running? consumer))))
      (finally
        ; Wait for kafka auto commit
        (Thread/sleep 1000)
        (core/stop! core)))))

