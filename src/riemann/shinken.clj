(ns riemann.shinken
  "Forwards events to Shinken."
  (:require [clj-http.client :as http]))

(defn- post
  "POST to Shinken."
  [hostname port username password event]
  (http/post
    (str "http://" hostname ":" port "/push_check_result")
    {:basic-auth [username password]
     :form-params event}))

(defn- format-event
  "Formats an event for Shinken."
  [event]
  {:time_stamp (int (:time event))
   :host_name (:host event)
   :service_description (:service event)
   :return_code (:state event)
   :output (:metric event)})

(defn shinken
  "Returns a function which accepts an event and sends it to Shinken's
  ws-arbiter module.

  (shinken {:hostname \"127.0.0.1\" :port 7760 :username \"admin\" :password
  \"admin\"})

  Options:

  :hostname     Host name of the Shinken receiver/arbiter. Default is
                \"127.0.0.1\".
  :port         Port that mod-ws-arbiter is listening to. Default is 7760
  :username     Username. Default is \"admin\".
  :password     Password of the corresponding user. Default is \"admin\"."

  [opts]
  (let
    [opts (merge {:hostname "127.0.0.1"
                  :port 7760
                  :username "admin"
                  :password "admin"} opts)]

    (fn[event] (post
                 (:hostname opts)
                 (:port opts)
                 (:username opts)
                 (:password opts)
                 (format-event event)))))
