(ns riemann.xymon
  "Forwards events to Xymon"
  (:require [clojure.java.io       :as io]
            [clojure.string        :as s]
            [clojure.tools.logging :refer [error]]
            [clojure.math.numeric-tower :refer [ceil]])
  (:import (java.net Socket InetSocketAddress)))


(defn host->xymon
  "Format an hostname for Xymon. Basically, replace all dot chars by commas."
  [host]
  (s/replace host "." ","))

(defn service->xymon
  "Format a service name to be understood by Xymon."
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
  "
  Convert an event to an Xymon enable message:

  enable HOSTNAME.TESTNAME

  if no service is provided, the complete host is enabled.
  "
  [{:keys [host service]
    :or {host "" service "*"}}]
  (let [host (host->xymon host)
        service (service->xymon service)]
    (format "enable %s.%s" host service)))

(defn event->disable
  "
  Convert an event to a Xymon disable message:
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

(defn- send-line-error-handler
  [e]
  (error e "cannot reach xymon host"))

(defn send-line
  "Connects to Xymon server, sends line, then closes the connection.
   This is a blocking operation and should happen on a separate thread."
  [opts line]
  (try
    (let [opts (merge
                {:host "127.0.0.1" :port 1984 :timeout 5
                 :error-handler send-line-error-handler}
                opts)
          addr (InetSocketAddress. (:host opts) (:port opts))
          sock (Socket.)]
      (.setSoTimeout sock (:timeout opts))
      (.connect sock addr (:timeout opts))
      (with-open [writer (io/writer sock)]
        (.write writer line)
        (.flush writer)))
    (catch Exception e
      ((:error-handler opts) e))))

(defn xymon
  "Returns a function which accepts an event and sends it to Xymon.
   Silently drops events when xymon is down. Attempts to reconnect
   automatically every five seconds. Use:

   (xymon {:host \"127.0.0.1\" :port 1984
           :timeout 5 :formatter event->status})
   "
  [opts]
  (let [formatter (or (:formatter opts) event->status)]
    (fn [{:keys [state service] :as event}]
      (when (and state service)
        (let [statusmessage ((:formatter opts) event)]
          (send-line opts statusmessage))))))
