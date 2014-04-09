(ns ^{:doc    "Forwards events to HipChat"
      :author "Hubert Iwaniuk"}
  riemann.hipchat
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.string :refer [join]]))

(defn- message-colour [ev]
  "Set the colour to be used in the
  hipchat message."
  (let [state (or (:state ev) (:state (first ev)))]
    (get {"ok"        "green"
          "critical"  "red"
          "error"     "red"}
         state
         "yellow")))

(def ^:private chat-url
  "https://api.hipchat.com/v1/rooms/message?format=json")

(defn- format-message [ev]
  "Formats a message, accepts a single
  event or a sequence of events."
  (join "\n\n"
        (map
          (fn [e]
            (str
              "Host: " (:host e)
              " \nService: " (:service e)
              " \nState: " (:state e)
              " \nMetric: " (:metric e)
              " \nDescription: " (:description e))) ev)))

(defn- format-event [{:keys [room_id from notify message] :as conf} event]
  "Creates an event suitable for posting to hipchat."
  (merge {:color (message-colour event)}
         conf
         (when-not message
           {:message (format-message (flatten [event]))})))

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
