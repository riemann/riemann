(ns riemann.xymon
  "Forwards events to Xymon"
  (:require [clojure.java.io       :as io]
            [clojure.string        :as s]
            [clojure.tools.logging :refer [error]]
            [clojure.math.numeric-tower :refer [ceil]])
  (:import (java.net Socket InetSocketAddress)))


(defn host->xymon
  "Formats an hostname for Xymon. Basically, replaces all dot chars by commas."
  [host]
  (s/replace host "." ","))

(defn service->xymon
  "Formats a service name to be understood by Xymon."
  [service]
  (s/replace service #"(\.| )" "_"))

(defn event->status
  "Formats an event as a Xymon status message:

  status[+LIFETIME][/group:GROUP] HOSTNAME.TESTNAME COLOR <additional text>

  Note about fields mapping:
    - HOSTNAME results from the string conversion (\".\" -> \",\") of :host
    - TESTNAME results from the string conversion (#\"(\\.| )\" -> \"_\") of :service
    - COLOR is taken as is from :state
    - <additional text> is taken \"as is\" from :description
    - GROUP is not handled
    - LIFETIME results from the rounding up to the nearest whole number of the division by 60 of :ttl.
       - No :ttl (i.e. :ttl nil) ends up with no LIFETIME set (defaults to Xymon server's default lifetime)
       - :ttl 0 becomes +0 LIFETIME and will end up as immediate purple

  "
  [{:keys [ttl host service state description]
    :or {host "" service "" description "" state "unknown"}}]
  (let [ttl-prefix (if ttl (str "+" (int (ceil (/ ttl 60)))) "")
        host       (host->xymon host)
        service    (service->xymon service)]
    (format "status%s %s.%s %s %s\n"
            ttl-prefix host service state description)))

(defn event->enable
  "Converts an event to an Xymon enable message:

  enable HOSTNAME.TESTNAME

  if no service is provided, the complete host is enabled.
  "
  [{:keys [host service]
    :or {host "" service "*"}}]
  (let [host (host->xymon host)
        service (service->xymon service)]
    (format "enable %s.%s" host service)))

(defn event->disable
  "Converts an event to a Xymon disable message:

  disable HOSTNAME.TESTNAME DURATION <additional text>

  Fields mapping is the same as event->status'. Also, the event ttl is
  used as duration, same as LIFETIME in event->status.
  "
  [{:keys [host service ttl description]
    :or {host "" service "*" description ""}}]
  (let [host (host->xymon host)
        service (service->xymon service)]
    (format "disable %s.%s %s %s"
            host service (int (ceil (/ ttl 60))) description)))

(defn send-message-error
  "Logs given exception as an error message.

  send-message-error is the default error handler invoked by
  send-single-message if none is provided.
  "
  [opts exception]
  (error exception (format "cannot reach xymon host (%s:%s)"
                           (:host opts) (:port opts))))

(defn send-single-message
  "Connects to a Xymon server, sends message, then closes the
  connection. This is a blocking operation and should happen on a
  dedicated thread.

  If any exception is raised during the connect/send process, the
  result of (:error-handler opts send-message-error) is invoked as a
  function with opts and the exception as its parameter.
  "
  [opts message]
  (let [opts (merge
              {:host "127.0.0.1" :port 1984 :timeout 5000
               :error-handler send-message-error}
              opts)]
    (try
      (let [addr (InetSocketAddress. (:host opts) (:port opts))
            sock (Socket.)]
        (.setSoTimeout sock (:timeout opts))
        (.connect sock addr (:timeout opts))
        (with-open [writer (io/writer sock)]
          (.write writer message)
          (.flush writer)))
      (catch Exception e
        ((:error-handler opts) opts e)))))

(defn send-message
  "Sends given message to Xymon host(s).

  If (:hosts opts) is a sequence _and_ (:host opts) is false, sends
  the message to all hosts described in (:hosts opts). When
  provided (:host opts) should be a list of map and
  send-single-message will invoked with (merge opts <item in :hosts>).

  If (:host opts) is not false, sends the message to opts.
  "
  [opts message]
  (if (and (seq (:hosts opts))
           (not (:host opts)))
    (let [hosts (:hosts opts)
          opts (dissoc opts :hosts)]
      (doseq [host hosts]
        (send-single-message (merge opts host) message)))
    (send-single-message opts message)))

(def message-max-length 4096)

(def ^:private combo-header "combo\n")
(def ^:private combo-header-len (count combo-header))

(defn- make-combo [messages]
  (if (= (count messages) 1)            ; messages must not be empty
    (first messages)
    (str combo-header (s/join "\n\n" messages) "\n\n")))

(defn events->combo
  "Returns a lazy sequence of combo messages. Each message is at most
  message-max-length long.
  "
  ([formatter events] (events->combo formatter events [] combo-header-len))
  ([formatter events messages len]
   (cond
     (seq events)
     ;; We have more events, consume next one.
     (let [next-message (formatter (first events))
           next-length (count next-message)
           length (+ len next-length 2)
           events (rest events)]
       (if (< length message-max-length)
         ;; keep appending to message
         (recur formatter events (conj messages next-message) length)
         ;; message is getting too big, so we create a combo and
         ;; iterate over the rest of the sequence.
         (cons (make-combo messages)
               (lazy-seq (events->combo
                          formatter events [next-message]
                          (+ next-length combo-header-len 2))))))
     ;; Stop case, no message found // drop combo
     (empty? messages) messages
     ;; Stop case, multiple messages joined as a combo
     :else (list (make-combo messages)))))

(defn xymon
  "Returns a function which accepts an event or a vector of events and
  which sends them to Xymon, as 'combo' messages if specified. Filters
  events with nil :state or :service. Use:

  (xymon {:host \"127.0.0.1\" :port 1984
          :timeout 5000 :formatter event->status})

  The :timeout value is expressed in milliseconds, as specified in
  java.net.Socket documentation.
  "
  [opts]
  (let [formatter (partial (if (:combo opts false) events->combo map)
                           (or (:formatter opts) event->status))]
    (fn [events]
      (doseq [message (formatter
                       (filter #(and (:service %) (:state %))
                               (if (sequential? events) events [events])))]
        (send-message opts message)))))
