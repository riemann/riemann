(ns riemann.nagios
  "Forwards events to Nagios via NSCA"
  (:require [clj-nsca.core :as nsca]))

(defn event->nagios
  "Converts an event into a Nagios message"
  [e]
  (nsca/nagios-message (str (:host e))
                       (str (:state e))
                       (str (:service e))
                       (str (:description e))))

(defn nagios
  "Creates an adapter to forward events to Nagios. The Nagios message will
  contain the host, state, service and description. Use:

  (nagios {:host \"localhost\" :port 5667 :password \"secret\" :encryption :TRIPLE_DES})

  :host       Host where the Nagios service runs.
  :port       The port to connect to.
  :password   The password as set in /etc/nsca.cfg
  :encryption The encryption method as set in /etc/nsca.cfg. Please note
              that currently only :NONE, :XOR and :TRIPLE_DES are supported.
  "
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 5667
                     :password ""
                     :encryption :TRIPLE_DES} opts)
        sender (nsca/nagios-sender (nsca/nagios-settings opts))]
  (fn [event]
    (let [msg (event->nagios event)]
      (nsca/send-message sender msg)))))
