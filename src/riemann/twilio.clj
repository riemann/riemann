(ns riemann.twilio
  "Forwards events to Twilio"
  (:require [clj-http.client :as client]
            [riemann.common :refer [body]]))

(def ^:private messages-url
  "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json")

(defn add-key-body
  "Add the `opts-key` value from `msg-opts` (if exists) into the `body-key` in `result`.
  Returns result."
  [opts-key body-key msg-opts result]
  (if-let [value (get msg-opts opts-key)]
    (assoc result body-key value)
    result))

(defn get-form-params
  "construct the `form-params` request parameter from `msg-opts`"
  [msg-opts]
  (->> {:To (:to msg-opts)
        :Body (:body msg-opts)}
       (add-key-body :from :From msg-opts)
       (add-key-body :messaging-service-sid :MessagingServiceSid msg-opts)
       (add-key-body :media-url :MediaUrl msg-opts)
       (add-key-body :status-callback :StatusCallback msg-opts)
       (add-key-body :application-sid :ApplicationSid msg-opts)
       (add-key-body :max-price :MaxPrice msg-opts)
       (add-key-body :provide-feedback :ProvideFeedback msg-opts)))

(defn- post
  "POST to the Twilio Messages API."
  [twilio-opts msg-opts]
  (client/post (format messages-url (:account twilio-opts))
     {:basic-auth [(:account twilio-opts) (:service-key twilio-opts)]
      :form-params (get-form-params msg-opts)
      :socket-timeout 5000
      :conn-timeout 5000
      :accept :json
      :throw-entire-message? true}))

(defn twilio-message
  "Send a message via Twilio"
  [twilio-opts msg-opts events]
  (let [events  (flatten [events])
        body    ((get msg-opts :body body) events)]
    (post
      twilio-opts
      (merge msg-opts {:body body}))))

(defn twilio
  "Returns a messenger, which is a function invoked with a phone number or a sequence
  of phone numbers and returns a stream. That stream is a function which takes a
  single event, or a sequence of events, and sends a message about them.

  (def messenger (twilio))
  (def text (messenger \"+15005550006\" \"+15005550006\"))

  This messenger sends sms out via twilio using the twilio http api. When used
  it outputs the http response recieved from twilio.

  (changed :state
    #(info \"twilio response\" (text %)))

  The first argument is a map of the twilio options :account and :key.
  The second argument is a map of default message option.

  (def text (twilio {:account \"id\" :service-key \"key\"}
                    {:from \"+15005550006\"}))

  Message options can be :

  `:from`                    A twilio phone number

  `:messaging-service-sid`   The 34 character unique id of the Messaging Service you want to associate with this Message

  `:media-url`               The URL of the media you wish to send out with the message.

  `:status-callback`         A URL that Twilio will POST to each time your message status changes to one of the following: queued, failed, sent, delivered, or undelivered

  `:application-sid`         Twilio will POST MessageSid as well as MessageStatus=sent or MessageStatus=failed to the URL in the MessageStatusCallback property of this Application

  `:max-price`               The total maximum price up to the fourth decimal (0.0001) in US dollars acceptable for the message to be delivered

  `:provide-feedback`        Set this value to true if you are sending messages that have a trackable user action and you intend to confirm delivery of the message using the Message Feedback API

  Full API documentation can be found here : https://www.twilio.com/docs/api/rest/sending-messages

  If you provide a single map, the messenger will split the twilio options out
  for you.

  (def text (twilio {:account \"id\"
                     :service-key \"key\"
                     :from \"+15005550006\"}))

  By default, riemann uses (body events) to format messages.
  You can set your own body formatter functions by including :body in msg-opts.
  These formatting functions take a sequence of
  events and return a string.

  (def text (twilio {} {:body (fn [events]
                                 (apply prn-str events))}))"
  ([] (twilio {}))
  ([opts]
        (let [twilio-keys #{:account :service-key}
              twilio-opts (select-keys opts twilio-keys)
              msg-opts  (select-keys opts (remove twilio-keys (keys opts)))]
          (twilio twilio-opts msg-opts)))
  ([twilio-opts msg-opts]
   (let [msg-opts (if-not (or (:from msg-opts)
                              (:messaging-service-sid msg-opts))
                    ;; the default :from for backward compatibility
                    (merge msg-opts {:from "+15005550006"})
                    msg-opts)]
     (fn make-stream [& recipients]
       (fn stream [event]
         (let [msg-opts (if (empty? recipients)
                          msg-opts
                          (merge msg-opts {:to recipients}))]
           (twilio-message twilio-opts msg-opts event)))))))

