(ns riemann.email
  "Send email about events. Create a mailer with (mailer opts), then create
  streams which send email with (your-mailer \"shodan@tau.ceti.five\"). Or
  simply call email-event directly."
  (:use riemann.common)
  (:use postal.core)
  (:use [clojure.string :only [join]]))

(defn- human-uniq
  "Returns a human-readable string describing things, e.g.

  importer
  api1, api2, api4
  23 services"
  [things, type]
  (let [things (distinct things)]
    (case (count things)
      0 nil
      1 (first things)
      2 (str (first things) " and " (nth things 1))
      3 (join ", " things)
      4 (join ", " things)
      (str (count things) " " type))))

(defn- subject
  "Constructs a subject line for a set of events."
  [events]
  (join " " (keep identity
        [(human-uniq (map :host events) "hosts")
         (human-uniq (map :service events) "services")
         (human-uniq (map :state events) "states")])))

(defn- body
  "Constructs an email body for a set of events."
  [events]
  (join "\n\n\n"
        (map
          (fn [event]
            (str
              "At " (time-at (:time event)) "\n"
              (:host event) " "
              (:service event) " "
              (:state event) " ("
              (:metric event) ")\n"
              "Tags: [" (join ", " (:tags event)) "]"
              "\n\n"
              (:description event)))
          events)))

(defn email-event
  "Send event(s) with the given configuration (:host, :port, :user, :to, etc)"
  [opts events]
  (let [events (flatten [events])]
    (send-message
      (merge {:subject (subject events)
              :body    (body events)}
             opts))))

(defn mailer
  "Returns a mailer which creates email streams, which take events. The mailer
  is invoked with an address or a sequence of addresses; it returns a function
  that takes events and sends email about that event to those addresses.
  Example:

  (def email (mailer {:from \"riemann@trioptimum.org\"
                      :host \"mail.relay\"
                      :user \"foo\"
                      :pass \"bar\"}))

  (changed :state
    (email \"xerxes@trioptimum.org\" \"shodan@trioptimum.org\"))

  This makes it easy to configure your email settings once and re-use them
  for different recipients. Of course, you can set :to in the mailer options
  as well, and use (email) without args. Options are passed to Postal."
  [opts]

  (let [opts (merge {:from "riemann"}
                    opts)]

    (fn [& recipients]
      (fn [event]
        (let [opts (if (empty? recipients)
                     opts
                     (merge opts {:to recipients}))]
          (email-event opts event))))))
