(ns reimann.email
  (:use reimann.common)
  (:use postal.core)
  (:use [clojure.contrib.string :only [join]]))

; Sends emails about events.

(defn- human-uniq [things, type]
  "Returns a human-readable string describing things, e.g.

  importer
  api1, api2, api4
  23 services"
  (let [things (distinct things)]
    (case (count things)
      0 nil
      1 (first things)
      2 (str (first things) " and " (nth things 1))
      3 (join ", " things)
      4 (join ", " things)
      (str (count things) " " type))))

(defn- subject [events]
  "Constructs a subject line for a set of events."
  (join " " (keep identity 
        [(human-uniq (map :host events) "hosts")
         (human-uniq (map :service events) "services")
         (human-uniq (map :state events) "states")])))

(defn- body [events]
  "Constructs a body for a set of events."
  (join "\n\n"
        (map 
          (fn [event]
            (:host event) " "
            (:service event) " "
            (:state event) " ("
            (:metric event) ")\nat "
            (time-at (:time event)) " "
            "tags: " (join ", " (:tags event)) 
            "\n"
            (:description event))
          events)))

(defn email-event [opts events]
  "Send event(s) with the given configuration (:host, :port, :user, :to, etc)"
  (let [events (flatten [events])]
    (send-message 
      (merge {:subject (subject events)
              :body    (body events)}
             opts))))

(defn mailer [opts]
  "Returns a mailer which creates email streams, which take events. The mailer
  is invoked with an address or a sequence of addresses; it returns a function
  that takes events and sends email about that event to those addresses.
  Example:

  (def email (mailer {:from \"reimann@trioptimum.org\"
                      :host \"mail.relay\"
                      :user \"foo\"
                      :pass \"bar\"}))

  (changed :state 
    (email \"xerxes@trioptimum.org\" \"shodan@trioptimum.org\"))

  This makes it easy to configure your email settings once and re-use them
  for different recipients. Of course, you can set :to in the mailer options
  as well, and use (email) without args. Options are those for Postal."

  (let [opts (merge {:from "reimann"}
                    opts)]

    (fn [& recipients]
      (fn [event]
        (let [opts (if (empty? recipients) 
                     opts
                     (merge opts {:to recipients}))]
          (email-event opts event))))))
