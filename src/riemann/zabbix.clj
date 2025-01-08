(ns riemann.zabbix
  "Forwards events to Zabbix"
  (:require
    [cheshire.core :as json]
    [clojure.java.io :as io])
  (:import
    (java.io ByteArrayOutputStream)
    (java.nio ByteBuffer ByteOrder)
    (java.net Socket)))

;; The Zabbix documentation is somewhat inconsistent when it comes to naming
;; things. Zabbix *sender* and Zabbix *trapper* are used interchangeably to
;; describe the protocol used to send metrics to a Zabbix server. Such items are
;; configured in the Zabbix server as Zabbix Trapper items, while zabbix_sender
;; is the name of the command line client utility.
;;
;; For consistency here, the following conventions are used:
;;
;; datapoint - map representing a single Zabbix item
;; request   - map containing one or more datapoints, as described in the
;;             Zabbix Trapper items documentation.
;; frame     - a Zabbix protocol frame with header and length information
;;             followed by request (or response) data.
;;
;; In a nutshell, a frame contains a request, which contains one or more
;; datapoints.

(defn- long->buf
  "Converts a long int n to a 64-bit little-endian ByteBuffer."
  [n]
  (-> (ByteBuffer/wrap (byte-array 8))
      (.order ByteOrder/LITTLE_ENDIAN)
      (.putLong n)
      (.array)))

(defn make-frame
  "Creates a Zabbix sender protocol frame from a supplied request, returning
  the frame as a byte array."
  [request]
  (let [json (json/generate-string request)
        body (.getBytes json)
        length (long->buf (count body))
        version (.getBytes "ZBXD\1")]
    (.toByteArray (doto (ByteArrayOutputStream.)
                    (.write version)
                    (.write length)
                    (.write body)))))

(defn make-request
  "Format a collection of datapoints as a Zabbix request."
  [datapoints]
  {:request "sender data"
   :data datapoints})

(defn make-datapoint
  "Converts a Riemann event into a Zabbix datapoint."
  [event]
  {:host (:host event)
   :key (:service event)
   :value (str (:metric event))
   :clock (:time event)})

(defn- send-request
  "Send a supplied request to a Zabbix server."
  [host port request]
  (with-open [sock (Socket. host port)]
    (let [stream (io/output-stream sock)
          frame (make-frame request)]
      (.write stream frame)
      (.flush stream))))

(defn zabbix
  "Returns a function which accepts an event or sequence of events and forwards
  them to Zabbix using the Zabbix Sender protocol. The :service field of the
  Riemann event is used as the key for the Zabbix item, with the :metric field
  used as the value. Use:

  (zabbix {:host \"localhost\" :port 10051})

  Options:

  - :host           Hostname of the Zabbix server or proxy to send events to.
                    Default: localhost.
  - :port           Port of the Zabbix host. Default 10051.

  Connection failures will result in an exception, but other types of Zabbix
  error are silently ignored. For example, it is up to the caller to ensure that
  the :service fields of events forwarded to Zabbix are valid Zabbix item keys."
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 10051} opts)]
    (fn [events]
      (let [events (if (sequential? events)
                     events
                     (vector events))]
        (when (seq events)
          (let [data (map make-datapoint events)
                request (make-request data)]
            (send-request (:host opts) (:port opts) request)))))))
