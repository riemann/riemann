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
            [clojure.java.io :as io]
            [clojure.tools.logging :refer [warn info]]
            [riemann.common :refer [decode-inputstream encode]]
            [riemann.instrumentation :refer [Instrumented]]
            [riemann.service :refer [Service ServiceEquiv]]
            [riemann.transport :refer [handle]]))

(def ^:no-doc pb->msg
  #(-> % (io/input-stream) (decode-inputstream)))

(def ^:no-doc msg->pb
  #(encode %))

(defn- gen-message-handler
  [core stats ex-name]
  (fn [channel {:keys [delivery-tag reply-to correlation-id] :as meta} ^bytes payload]
    (letfn [(reply-with [msg]
              (lb/publish channel ex-name reply-to (msg->pb msg) {:content-type "application/octet-stream" :correlation-id correlation-id}))]
      (try
        (let [msg (pb->msg payload)
              result (handle core msg)]
          (metrics/update! stats (- (System/nanoTime) (:decode-time msg)))
          (when reply-to
            (reply-with result)))
        (catch Exception e
          (let [errmsg (.getMessage e)]
            (when reply-to
              (reply-with {:ok false :error errmsg}))
            (warn "rabbitmq-transport caught an exception:" errmsg)))
        (finally
          (lb/ack channel delivery-tag))))))

(defn- same-settings?
  [one two]
  (let [ks [:host :port :vhost :riemann.exchange-settings :riemann.routing-key]
        [one two] (->> [one two]
                       (map rmq/normalize-settings)
                       (map #(select-keys % ks)))]
    (= one two)))

(defn- conn->addr
  [conn]
  (str (-> conn (.getAddress) (.getHostAddress)) ":" (.getPort conn)))

(defn- deep-merge
  [a & maps]
  (if (map? a)
    (apply merge-with deep-merge a maps)
    (apply merge-with deep-merge maps)))

(defrecord RabbitMQTransport [settings core stats connection channel]
  ServiceEquiv
  (equiv? [this other]
    (and (instance? RabbitMQTransport other)
         (same-settings? settings (:settings other))))
 
  Service
  (conflict? [this other]
    (and (instance? RabbitMQTransport other)
         (same-settings? settings (:settings other))))
 
  (reload! [this new-core]
    (reset! core new-core))
 
  (start! [this]
    (when-not test/*testing*
      (locking this
        (when-not @connection
          (reset! connection (rmq/connect settings))
          (reset! channel (lch/open @connection))
          (let [{{ex-name :name ex-type :type :as ex-settings} :riemann.exchange-settings
                 routing-key :riemann.routing-key} settings
                q-name (.getQueue (lq/declare @channel "" {:exclusive true}))]
            (le/declare @channel ex-name ex-type ex-settings)
            (lq/bind @channel q-name ex-name {:routing-key routing-key})
            (lc/subscribe @channel q-name (gen-message-handler @core stats ex-name)))
          (info "rabbitmq-transport connected to the server" (conn->addr @connection))))))
 
  (stop! [this]
    (locking this
      (when @connection
        (let [addr (conn->addr @connection)]
          (rmq/close @channel)
          (rmq/close @connection)
          (reset! channel nil)
          (reset! connection nil)
          (info "rabbitmq-transport disconnected from the server" addr)))))
 
  Instrumented
  (events [this]
    (let [svc (str "riemann transport rabbitmq " (conn->addr @connection))
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

  Configure it via connection parameters described [here](http://clojurerabbitmq.info/articles/connecting.html).

  Additional settings are:

  - :riemann.exchange-settings Settings an exchange is declared with, defaults are:
  
  ```clojure
  {:name \"riemann\"
   :type \"topic\"
   :durable false
   :auto-delete false
   :internal false}
  ```

  - :riemann.routing-key Routing key to match with, default is \"#\" (get everything)

  For details on exchange declaration options see [this](http://reference.clojurerabbitmq.info/langohr.exchange.html#var-declare).

  Use [message properties](https://www.rabbitmq.com/consumers.html#message-properties) such as
  \"Reply To\" and \"Correlation ID\" when publishing to receive statuses and query results."
  ([] (rabbitmq-transport {}))
  ([settings]
    (let [base {:riemann.exchange-settings {:name "riemann" :type "topic"}
                :riemann.routing-key "#"}
          settings (deep-merge base settings)
          core (get settings :core (atom nil))
          stats (metrics/rate+latency)]
      (RabbitMQTransport. settings core stats (atom nil) (atom nil)))))
