(ns riemann.twilio
  "Forwards events to Twilio"
  (:require [clj-http.client :as client]
            [riemann.common :refer [body]]))

(def ^:private messages-url
  "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json")

(defn- post
  "POST to the Twilio Messages API."
  [twilio-opts msg-opts]
  (client/post (format messages-url (:account twilio-opts))
     {:basic-auth [(:account twilio-opts) (:service-key twilio-opts)]
     :form-params
     {:From (:from msg-opts)
      :To (:to msg-opts)
      :Body (:body msg-opts)}
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
  The second argument is a map of default message option (:from).

  (def text (twilio {:account \"id\" :service-key \"key\"}
                     {:from \"+15005550006\"}))

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
   (let [msg-opts (merge {:from "+15005550006"} msg-opts)]
     (fn make-stream [& recipients]
       (fn stream [event]
         (let [msg-opts (if (empty? recipients)
                          msg-opts
                          (merge msg-opts {:to recipients}))]
           (twilio-message twilio-opts msg-opts event)))))))

