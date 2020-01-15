(ns riemann.rabbitmq
  "Forwards events to RabbitMQ."
  (:require [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.exchange :as le]
            [langohr.basic :as lb]
            [cheshire.core :as json]))

(def ^{:private true} json-formatter json/generate-string)

(defn- deep-merge
  [a & maps]
  (if (map? a)
    (apply merge-with deep-merge a maps)
    (apply merge-with deep-merge maps)))

(defn rabbitmq
  "Accepts options described [here](http://clojurerabbitmq.info/articles/connecting.html) and returns a function
  that, being invoked with options listed below, returns a stream which publishes events to RabbitMQ.

  Options:

  - :exchange-settings Settings an exchange is declared with, defaults are {:name \"riemann\" :type \"topic\" :durable false :auto-delete false :internal false}.
  - :routing-key Routing key messages to be published with, default is \"riemann.events\".
  - :message-properties Properties of messages to publish, defaults are {:content-type \"application/json\" :mandatory false}.
  - :message-formatter A function to format event(s), default format is JSON.

  ```clojure
  (def rmq (rabbitmq {:host \"riemann.local\"
                      :port 1234}))

  (changed :state
    (rmq {:exchange-settings {:durable true}
          :routing-key \"riemann.events.hello\"}))
  ```

  For details on exchange declaration options and message properties refer to Langohr API reference for
    - [declare](http://reference.clojurerabbitmq.info/langohr.exchange.html#var-declare) and
    - [publish](http://reference.clojurerabbitmq.info/langohr.basic.html#var-publish)
  "
  ([] (rabbitmq {}))
  ([opts]
   (let [connection (rmq/connect opts)
         channel (lch/open connection)]
     (fn make-stream
       ([] (make-stream {}))
       ([opts]
        (let [base {:exchange-settings {:name "riemann" :type "topic"}
                    :routing-key "riemann.events"
                    :message-properties {:content-type "application/json"}
                    :message-formatter json-formatter}
              {:keys [exchange-settings
                      routing-key
                      message-properties
                      message-formatter]} (deep-merge base opts)
              {ex-name :name ex-type :type} exchange-settings]
          (le/declare channel ex-name ex-type exchange-settings)
          (fn stream [e]
            (let [payload (message-formatter e)]
              (lb/publish channel ex-name routing-key payload message-properties)))))))))
