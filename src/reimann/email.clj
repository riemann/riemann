(ns reimann.email
  (:use reimann.common)
  (:use postal.core))

; Sends emails about events.

(defn email-event [opts event]
  "Send event with the given configuration (:host, :port, :user, :to, etc)"
  (send-message 
    (merge {:subject (str (:host event) " "
                          (:service event) " "
                          (:state event))
            :body (str (:host event) " "
                       (:service event) " "
                       (:state event) " ("
                       (:metric_f event) ")\nat "
                       (time-at (:time event)) "\n\n"
                       (:description event))}
           opts)))

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
