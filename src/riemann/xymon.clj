(ns riemann.xymon
  "Forwards events to Xymon"
  (:require [clojure.java.io       :as io]
            [clojure.string        :as s]
            [clojure.tools.logging :refer [error]])
  (:import java.net.Socket))

(defn format-line
  "Formats an event "
  [{:keys [ttl host service state description]
    :or {host "" service "" description "" state "unknown"}}]
  (let [ttl-prefix (if ttl (str "+" ttl) "")
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
