(ns ^{:doc    "Forwards events to HipChat"
      :author "Hubert Iwaniuk"}
  riemann.hipchat
  (:require [clj-http.client :as client]
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

(defn ^:private chat-url [server room]
  (str "https://" server "/v2/room/" room "/notification"))

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

(defn- format-event [{:keys [message] :as conf} event]
  "Creates an event suitable for posting to hipchat."
  (merge {:color (message-colour event) :from "riemann"}
         (dissoc conf :server :room_id)
         (when-not message
           {:message (format-message (flatten [event]))})))

(defn- post
  "POST to the HipChat API."
  [token {:keys [server room_id] :as conf} event]
  (client/post (str (chat-url server room_id) "?auth_token=" token)
               {:form-params           (format-event (assoc conf :message_format "text") event)
                :socket-timeout        5000
                :conn-timeout          5000
                :accept                :json
                :throw-entire-message? true}))

(defn hipchat
  "Creates a HipChat adapter. Takes your HipChat v2 authentication token,
  and returns a function which posts a message to a HipChat.

  You can any a personal or room-specific token, which can be obtained from
  your profile page or a specific room.

  More on api tokens at https://www.hipchat.com/docs/apiv2/auth

  If you're using hosted HipChat, you can leave out :server (or set it to
  'api.hipchat.com').

  (let [hc (hipchat {:server \"...\"
                     :token \"...\"
                     :room 12345
                     :notify 0})]
    (changed-state hc))"
  [{:keys [server token room notify]}]
  (if (not (= 40 (count token)))
    (throw (IllegalArgumentException. "This adapter now requires a v2 API key")))
  (fn [e] (post token
                {:server  (or server "api.hipchat.com")
                 :room_id room
                 :notify  notify}
               e)))
