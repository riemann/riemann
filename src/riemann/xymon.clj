(ns riemann.xymon
  "Forwards events to Xymon"
  (:require [clojure.java.io :as io]
            [clojure.string  :as s])
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
  "Connects to Xymon server, sends line, then closes the connection"
  	[opts line]
  	(with-open [sock (Socket. (:host opts) (:port opts))
               writer (io/writer sock)
               ]
              		(.write writer line)
              		(.flush writer)
              	)
  )

(defn xymon
  "Returns a function which accepts an event and sends it to Xymon.
   Silently drops events when xymon is down. Attempts to reconnect
   automatically every five seconds. Use:
   
   (xymon {:host \"127.0.0.1\" :port 1984})
   
   
   "
  [opts]
  (let [opts (merge {:host "127.0.0.1"
                     :port 1984
                     } opts)
        ]
    
    (fn [event]
      (when (:state event)
        	(when (:service event)
           (let [statusmessage (format-line event)]
             (send-line opts statusmessage)))))))
