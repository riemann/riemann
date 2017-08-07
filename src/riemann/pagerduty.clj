(ns riemann.pagerduty
  "Forwards events to Pagerduty"
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clj-time.format :as f]
            [clj-time.coerce :as coerce]))

(def ^:private event-url-v1
  "https://events.pagerduty.com/generic/2010-04-15/create_event.json")

(def ^:private event-url-v2
  "https://events.pagerduty.com/v2/enqueue")

(def timestamp-formatter (f/formatters :date-time))

(defn- post
  "POST to the PagerDuty events API."
  [request-body url options]
  (client/post url
               (merge
                {:body (json/generate-string request-body)
                 :socket-timeout 5000
                 :conn-timeout 5000
                 :content-type :json
                 :accept :json
                 :throw-entire-message? true}
                options)))

(defn format-event-v1
  "Formats an event for PagerDuty v1 API"
  [event]
  {:incident_key (str (:host event) " " (:service event))
   :description (str (:host event) " "
                     (:service event) " is "
                     (:state event) " ("
                     (:metric event) ")")
   :details event})

(defn format-event-v2
  "Formats an event for PagerDuty v2 API"
  [event]
  {:summary (str (:host event) " - "
                 (:service event) " is "
                 (:state event) " ("
                 (:metric event) ")")
   :source (:host event)
   :severity (:state event)
   :timestamp (->> (or (:time event) (long (riemann.time/unix-time)))
                   (coerce/from-long)
                   (f/unparse timestamp-formatter))
   :custom_details event})

(defn request-body-v2
  "Generate PD v2 API request body. event-action is one of :trigger, :acknowledge,
  :resolve"
  [service-key event-action formatter event]
  (merge
   {:routing_key service-key
    :event_action event-action
    :payload (formatter event)}
   (if-let [dedup-key (:dedup-key event)]
     {:dedup_key dedup-key}
     {})))

(defn request-body-v1
  "Generate PD v1 API request body. event-type is one of :trigger, :acknowledge,
  :resolve"
  [service-key event-type formatter event]
  (merge
   {:service_key service-key
    :event_type event-type}
   (formatter event)))

(defn send-event
  "Send an event to Pagerduty."
  [event-type config event]
  (let [[request-body event-url formatter] (if (= :v2 (:version config))
                                             [request-body-v2
                                              event-url-v2
                                              format-event-v2]
                                             [request-body-v1
                                              event-url-v1
                                              format-event-v1])]
    (post (request-body (:service-key config)
                        event-type
                        (or (:formatter config) formatter)
                        event)
          event-url
          (:options config))))

(defn pagerduty
  "Creates a PagerDuty adapter.
  By default, use the pagerduty v1 API. You can use the v2 API by setting the
  `:version` option to `:v2`.

  Returns a map of functions which trigger, acknowledge, and resolve events.

  General options:

  `:service-key`         Pagerduty service key (also called integration key
  or routing key)
  `:formatter`           Formatter for the pagerduty event. You can override
  the default formatter. The formatter must be a function that accepts an event
  and emits a hash. (optional)
  `:options`             Extra HTTP options. (optional)
  `:version`             set to `:v2` to use Pagerduty v2 API. (optional)

  v1 API:

  By default, event host and service will be used as the incident key. The PD
  description will be the host, service, state, and metric. The full event will be
  attached as the details.

  v2 API:

  By default, event host will be used as the source. The PD summary will be the
  host, service, state, and metric. The severity is the state. The full event will
  be attached as the details.

  Each event can also contains a `:dedup-key` key to handle alert de-duplication.

  Example, using the v1 API with a custom formatter:

  (defn pd-format-event
    [event]
    {:incident_key 'Incident key', :description 'Incident Description',
     :details 'Incident details'})

  (let [pd (pagerduty { :service-key \"my-service-key\" :formatter pd-format-event})]
    (changed-state
      (where (state \"ok\") (:resolve pd))
      (where (state \"critical\") (:trigger pd))))"
  [config]
  {:trigger     (fn [e] (send-event :trigger config e))
   :acknowledge (fn [e] (send-event :acknowledge config e))
   :resolve     (fn [e] (send-event :resolve config e))})
