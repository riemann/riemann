(ns riemann.transport.rabbitmq
  "Consumes messages from RabbitMQ. Associated with a core. Sends events
  to the core's streams, queries the core's index for states."
  (:require [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.queue :as lq]
            [langohr.exchange :as le]
            [langohr.consumers :as lc]
            [langohr.basic :as lb]
            [riemann.test :as test]
            [interval-metrics.core :as metrics]
            [clojure.java.io :as io])
  (:use [clojure.tools.logging :only [warn info]]
        [riemann.core :only [stream!]]
        [riemann.common :only [decode-inputstream encode]]
        [riemann.instrumentation :only [Instrumented]]
        [riemann.service :only [Service ServiceEquiv]]
        [riemann.transport :only [handle]]))

(def ^{:doc "Converts a protobuf byte array into an Msg map."} pb->msg
  #(-> % (io/input-stream) (decode-inputstream)))

(def ^{:doc "Converts an Msg map into a protobuf byte array"} msg->pb
  #(encode %))

(defn- gen-message-handler
  [core stats ex-name]
  (fn [channel {:keys [delivery-tag reply-to correlation-id] :as meta} ^bytes payload]
    (let [msg (pb->msg payload)
          result (handle core msg)]
      (metrics/update! stats (- (System/nanoTime) (:decode-time msg)))
      (when reply-to
        (lb/publish channel ex-name reply-to (msg->pb result) {:content-type "application/octet-stream"
                                                               :correlation-id correlation-id}))
      (lb/ack channel delivery-tag))))

(defrecord RabbitMQTransport [host port ex-name ex-type routing-key core stats connection channel]
  ServiceEquiv
  (equiv? [this other]
    (and (instance? RabbitMQTransport other)
         (= host (:host other))
         (= port (:port other))
         (= ex-name (:ex-name other))
         (= ex-type (:ex-type other))
         (= routing-key (:routing-key other))))
 
  Service
  (conflict? [this other]
    (and (instance? RabbitMQTransport other)
         (= host (:host other))
         (= port (:port other))
         (= ex-name (:ex-name other))
         (= ex-type (:ex-type other))
         (= routing-key (:routing-key other))))
 
  (reload! [this new-core]
    (reset! core new-core))
 
  (start! [this]
    (when-not test/*testing*
      (locking this
        (when-not @connection
          (reset! connection (rmq/connect {:host host :port port}))
          (reset! channel (lch/open @connection))
          (le/declare @channel ex-name ex-type {:durable false :auto-delete true})
          (let [q-name (.getQueue (lq/declare @channel "" {:exclusive false :auto-delete true}))]
            (lq/bind @channel q-name ex-name {:routing-key routing-key})
            (lc/subscribe @channel q-name (gen-message-handler @core stats ex-name)))
          (info "rabbitmq-transport connected to the server" host port)))))
 
  (stop! [this]
    (locking this
      (when @connection
        (rmq/close @channel)
        (rmq/close @connection)
        (reset! channel nil)
        (reset! connection nil)
        (info "rabbitmq-transport disconnected from the server" host port))))
 
  Instrumented
  (events [this]
    (let [svc (str "riemann transport rabbitmq " host ":" port)
          in (metrics/snapshot! stats)
          base {:state "ok"
                :tags ["riemann"]
                :time (:time in)}]
      (map (partial merge base)
           (concat [{:service (str svc " in rate")
                     :metric (:rate in)}]
                   (map (fn [[q latency]]
                          {:service (str svc " in latency " q)
                           :metric latency})
                        (:latencies in)))))))

(defn rabbitmq-transport
  "Start consuming messages from RabbitMQ, applying them into the current core.
  Requires (service/start!).
  
  Options:
  
  - :host \"127.0.0.1\"
  - :port 5672
  - :ex-name \"riemann\"
  - :ex-type \"topic\"
  - :routing-key \"#\""
  ([] (rabbitmq-transport {}))
  ([opts]
    (let [core (get opts :core (atom nil))
          stats (metrics/rate+latency)
          host (get opts :host "127.0.0.1")
          port (get opts :port 5672)
          ex-name (get opts :ex-name "riemann")
          ex-type (get opts :ex-type "topic")
          routing-key (get opts :routing-key "#")]
      (RabbitMQTransport. host port ex-name ex-type routing-key core stats (atom nil) (atom nil)))))
