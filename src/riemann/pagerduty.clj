(ns riemann.pagerduty
  "Forwards events to Pagerduty"
  (:require [clj-http.client :as client])
  (:require [cheshire.core :as json]))

(def ^:private event-url
  "https://events.pagerduty.com/generic/2010-04-15/create_event.json")

(defn- post
  "POST to the PagerDuty events API."
  [request]
  (client/post event-url
               {:body (json/generate-string request)
                :socket-timeout 5000
                :conn-timeout 5000
                :content-type :json
                :accept :json
                :throw-entire-message? true}))

(defn format-event
  "Formats an event for PagerDuty"
  [event]
  {:incident_key (str (:host event) " " (:service event))
   :description (str (:host event) " "
                     (:service event) " is "
                     (:state event) " ("
                     (:metric event) ")")
   :details event})

(defn send-event
  "Sends an event to PD. event-type is one of :trigger, :acknowledge,
  :resolve"
  [service-key event-type formatter event]
  (merge
    {:service_key service-key, :event_type event-type} (formatter event)))

(defn pagerduty
  "Creates a PagerDuty adapter. Takes your PD service key, and returns a map of
  functions which trigger, acknowledge, and resolve events. By default, event
  service will be used as the incident key. The PD description will be the service,
  state, and metric. The full event will be attached as the details.

  You can override this by specifying a formatter. The formatter must be a function that
  accepts an event and emits a hash.

  (defn pd-format-event
    [event]
    {:incident_key 'Incident key', :description 'Incident Description',
     :details 'Incident details'})

  The :formatter is an optional argument.

  (let [pd (pagerduty { :service-key \"my-service-key\" :formatter pd-format-event})]
    (changed-state
      (where (state \"ok\") (:resolve pd))
      (where (state \"critical\") (:trigger pd))))"
  [{:keys [service-key formatter] :or {formatter format-event}}]
  {:trigger     (fn [e] (post (send-event service-key :trigger formatter e)))
   :acknowledge (fn [e] (post (send-event service-key :acknowledge formatter e)))
   :resolve     (fn [e] (post (send-event service-key :resolve formatter e)))})
