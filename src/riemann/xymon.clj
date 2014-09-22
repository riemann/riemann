(ns riemann.xymon
  "Forwards events to Xymon"
  (:refer-clojure :exclude [replace])
  (:require [clojure.java.io :as io])
  (:import (java.net Socket))
  (:use [riemann.common]
	[clojure.string :only [split join replace]])
  )

(defn format-line
  "Formats an event "
  [event]
  (str "status" (when (and (not= "" (:ttl event)) (not= nil (:ttl event))) (str "+" (:ttl event))) " " (replace (:host event) #"\." ",") "." (replace (:service event) #"(\.| )" "_")" " (:state event) " " (:description event) "\n" )
  )

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
