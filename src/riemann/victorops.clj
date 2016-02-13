(ns riemann.victorops
  "Forwards events to VictorOps"
  (:require [clj-http.client :as client])
  (:require [cheshire.core :as json])
  (:require [clojure.string :as cstr]))

(def ^:private event-url
  "https://alert.victorops.com/integrations/generic/20131114/alert")

(defn- post
  "POST to the VictorOps API"
  [api-key routing-key request]
  (client/post (cstr/join "/" [event-url api-key routing-key])
               {:body (json/generate-string request)
                :socket-timeout 5000
                :conn-timeout 5000
                :content-type :json
                :accept :json
                :throw-entire-message? true}))

(defn- format-event
  "Formats an event for VO. message-type is one of :INFO, :WARNING,
  :ACKNOWLEDGEMENT, :CRITICAL, :RECOVERY"
  [message-type event]
  {:message_type message-type
   :entity_id (cstr/join "/" [(:host event) (:service event)])
   :timestamp (:time event)
   :state_start_time (:time event)
   :state_message (str (:host event) " "
                     (:service event) " is "
                     (:state event) " ("
                     (:metric event) ")")
   :entity_is_host false
   :monitoring_tool "riemann"})

(defn victorops
  "Creates a VictorOps adapter. Takes your API key and routing key and returns a map of
  functions :info, :warning, :critical, :acknowledgement and :recovery corresponding to
  the related VictorOps API message types. Each message's entity id will be
  \"<event host>/<event service>\". The state message will be the host, service, state
  and metric.

  (let [vo (victorops \"my-api-key\" \"my-routing-key\")]
    (changed-state
      (where (state \"info\") (:info vo))
      (where (state \"warning\") (:warning vo))
      (where (state \"critical\") (:critical vo))
      (where (state \"ok\") (:recovery vo))))"
  [api-key routing-key]
  {:info            (fn [e] (post api-key routing-key (format-event :INFO e)))
   :warning         (fn [e] (post api-key routing-key (format-event :WARNING e)))
   :critical        (fn [e] (post api-key routing-key (format-event :CRITICAL e)))
   :acknowledgement (fn [e] (post api-key routing-key (format-event :ACKNOWLEDGEMENT e)))
   :recovery        (fn [e] (post api-key routing-key (format-event :RECOVERY e)))})
