(ns riemann.kafka
  "Receives events from and forwards events to Kafka."
  (:require [kinsky.client :as client]
            [cheshire.core :as json]
            [riemann.test :as test])
  (:use [riemann.common        :only [event]]
        [riemann.core          :only [stream!]]
        [riemann.service       :only [Service ServiceEquiv]]
        [clojure.tools.logging :only [info error]]))

(defn kafka
  "Returns a function that is invoked with a topic name and an optional message key and returns a stream. That stream is a function which takes an event or a sequence of events and sends them to Kafka.
  
  (def kafka-output (kafka))

  (changed :state
    (kafka-output \"mytopic\"))

  Options:

  For a complete list of producer configuration options see https://kafka.apache.org/documentation/#producerconfigs  

  :bootstrap.servers  Bootstrap configuration, default is \"localhost:9092\".
  :value.serializer   Value serializer, default is json-serializer.

  Example with SSL enabled:

  (def kafka-output (kafka {:bootstrap.servers \"kafka.example.com:9092\"
                            :security.protocol \"SSL\"
                            :ssl.truststore.location \"/path/to/my/truststore.jks\"
                            :ssl.truststore.password \"mypassword\"}))"

  ([] (kafka {}))
  ([opts]
   (let [opts (merge {:bootstrap.servers "localhost:9092"
                      :value.serializer client/json-serializer}
                     opts)
         producer (client/producer (dissoc opts :value.serializer)
                                   (:value.serializer opts))]
     (fn make-stream [& args]
       (fn stream [event]
         (let [[topic message-key] args]
           (client/send!
             producer topic message-key event)))))))

(defn json-deserializer
  "Deserialize JSON. Let bad payload not break the consumption."
  []
  (client/deserializer
    (fn [_ payload]
      (when payload
        (try
          (json/parse-string (String. payload "UTF-8") true)
        (catch Exception e
          (error e "Could not decode message")))))))

(defn start-kafka-thread
  "Start a kafka thread which will pop messages off the queue as long
  as running? is true"
  [running? core opts]
  (let [opts (merge {:consumer.config {:bootstrap.servers "localhost:9092"
                                       :group.id "riemann"}
                     :topics ["riemann"]
                     :key.deserializer client/keyword-deserializer
                     :value.deserializer json-deserializer
                     :poll.timeout.ms 100}
                    opts)
        consumer (client/consumer (dissoc (:consumer.config opts) :enable.auto.commit)
                                  (:key.deserializer opts)
                                  (:value.deserializer opts))
        topics (flatten (:topics opts))]
    (future
      (try
        (info "Subscribing to " topics "...")
        (client/subscribe! consumer topics)
        (while running?
          (let [msgs (client/poll! consumer (:poll.timeout.ms opts))
                msgs-by-topic (get msgs :by-topic)]
            (doseq [records msgs-by-topic
                    record (last records)]
              (let [event (event (get record :value))]
                (stream! @core event)))))
        (catch Exception e
          (error e "Interrupted consumption"))
        (finally 
          (client/close! consumer))))))

(defn kafka-consumer
  "Yield a kafka consumption service"
  [opts]
  (let [running? (atom true)
        core     (atom nil)]
    (reify
      clojure.lang.ILookup
      (valAt [this k not-found]
        (or (.valAt this k) not-found))
      (valAt [this k]
        (info "Looking up: " k)
        (when (= (name k) "opts") opts))
      ServiceEquiv
      (equiv? [this other]
        (= opts (:opts other)))
      Service
      (conflict? [this other]
        (= opts (:opts other)))
      (start! [this]
        (when-not test/*testing*
          (do (info "Starting kafka consumer")
              (start-kafka-thread running? core opts))))
      (reload! [this new-core]
        (info "Reload called, setting new core value")
        (reset! core new-core))
      (stop! [this]
        (reset! running? false)
        (info "Stopping kafka consumer")))))
