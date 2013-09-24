(ns riemann.nagios
  "Forwards events to Nagios via NSCA"
  (:require [clj-nsca.core :as nsca]))

(defn event->nagios
  "Formats an event into a Nagios message"
  [e]
  (nsca/nagios-message :host (str (:host e))
                       :service (str (:service e))
                       :level (str (:state e))
                       :message (str (:description e))))

(defn nagios
  "Creates an adaptor to forward events to Nagios. The Nagios message will
  contain the host, state, service and description.

  Tested with:
  (streams
    (by [:host, :service]
      (let [nag (nagios \"host\", 5667, \"top-secret\", :DES)]
        nag)))"
  [host port password encryption]
  (fn [e]
    (let [msg (event->nagios e)
          sender (nsca/nagios-sender (nsca/nagios-settings :host host 
                                                           :port port 
                                                           :password password 
                                                           :encryption encryption))]
      (nsca/send-message sender msg))))
