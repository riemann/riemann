(ns riemann.kafka
  "Forwards events to Kafka."
  (:require [kinsky.client :as client]))

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

  (def kafka-output (kafka {:boostrap.servers \"kafka.example.com:9092\"
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
