(ns riemann.pagerduty
  "Forwards events to Pagerduty"
  (:require [org.httpkit.client :as client])
  (:require [cheshire.core :as json]))

(def ^:private event-url
  "https://events.pagerduty.com/generic/2010-04-15/create_event.json")

(defn- post
  "POST to the PagerDuty events API."
  [request]
  ;; fire and forget
  (client/post event-url
               {:body (json/generate-string request)
                :timeout 5000
                :headers {"Content-Type" "application/json"
                          "Accept"       "application/json"}}))

(defn- format-event
  "Formats an event for PD. event-type is one of :trigger, :acknowledge,
  :resolve"
  [service-key event-type event]
  {:service_key service-key
   :event_type event-type
   :incident_key (str (:host event) " " (:service event))
   :description (str (:host event) " "
                     (:service event) " is "
                     (:state event) " ("
                     (:metric event) ")")
   :details event})

(defn pagerduty
  "Creates a pagerduty adapter. Takes your PD service key, and returns a map of
  functions which trigger, acknowledge, and resolve events. Event service will
  be used as the incident key. The PD description will be the service, state,
  and metric. The full event will be attached as the details.

  (let [pd (pagerduty \"my-service-key\")]
    (changed-state
      (where (state \"ok\") (:resolve pd))
      (where (state \"critical\") (:trigger pd))))"
  [service-key]
  {:trigger     (fn [e] (post (format-event service-key :trigger e)))
   :acknowledge (fn [e] (post (format-event service-key :acknowledge e)))
   :resolve     (fn [e] (post (format-event service-key :resolve e)))})
