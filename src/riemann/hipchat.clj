(ns ^{:doc    "Forwards events to HipChat"
      :author "Hubert Iwaniuk"}
  riemann.hipchat
  (:require [clj-http.client :as client]
            [cheshire.core :as json]))

(def ^:private chat-url
  "https://api.hipchat.com/v1/rooms/message?format=json")

(defn- format-message [{:keys [host service state metric]}]
  (str "Host: " host
       ",\nservice: " service
       ",\nstate: " state
       ",\nmetric: " metric))

(defn- format-event [{:keys [room_id from notify message] :as conf} event]
  (merge {:color (condp = (:state event)
                   "ok"       "green"
                   "critical" "red"
                   "error"    "red"
                   "yellow")}
         conf
         (when-not message
           {:message (format-message event)})))

(defn- post
  "POST to the HipChat API."
  [token {:keys [room_id from message notify] :as conf} event]
  (client/post (str chat-url "&auth_token=" token)
               {:form-params           (format-event (assoc conf :message_format "text") event)
                :socket-timeout        5000
                :conn-timeout          5000
                :accept                :json
                :throw-entire-message? true}))

(defn hipchat
  "Creates a HipChat adapter. Takes your HipChat authentication token,
   and returns a function which posts a message to a HipChat.

  (let [hc (hipchat {:token \"...\"
                     :room 12345
                     :from \"Riemann reporting\"
                     :notify 0})]
    (changed-state hc))"
  [{:keys [token room from notify]}]
  (fn [e] (post token
               {:room_id room
                :from    from
                :notify  notify}
               e)))
