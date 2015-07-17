(ns riemann.xymon
  "Forwards events to Xymon"
  (:require [clojure.java.io       :as io]
            [clojure.string        :as s]
            [clojure.tools.logging :refer [error]]
            [clojure.math.numeric-tower :refer [ceil]])
  (:import java.net.Socket))

(defn format-line
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
        host       (s/replace host "." ",")
        service    (s/replace service #"(\.| )" "_")]
    (format "status%s %s.%s %s %s\n"
            ttl-prefix host service state description)))

(defn send-line
  "Connects to Xymon server, sends line, then closes the connection.
   This is a blocking operation and should happen on a separate thread."
  [opts line]
  (try
    (with-open [sock   (Socket. (:host opts) (:port opts))
                writer (io/writer sock)]
      (.write writer line)
      (.flush writer))
    (catch Exception e
      (error e "could not reach xymon host"))))

(defn xymon
  "Returns a function which accepts an event and sends it to Xymon.
   Silently drops events when xymon is down. Attempts to reconnect
   automatically every five seconds. Use:

   (xymon {:host \"127.0.0.1\" :port 1984})

   "
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 1984} opts)]
    (fn [{:keys [state service] :as event}]
      (when (and state service)
        (let [statusmessage (format-line event)]
          (send-line opts statusmessage))))))
