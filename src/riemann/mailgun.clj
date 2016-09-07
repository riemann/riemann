(ns riemann.mailgun
  "Forwards events to Mailgun"
  (:require [clj-http.client :as client]
            [riemann.common :refer [body subject]]))

(def ^:private event-url
  "https://api.mailgun.net/v2/%s/messages")

(defn- post
  "POST to the Mailgun events API."
  [mgun-opts msg-opts]
  (let [body (:body msg-opts)
        [body-type content] (if (= (:type body) :html)
                              [:html (:content body)]
                              [:text body])]
    (client/post (format event-url (:sandbox mgun-opts))
      {:basic-auth ["api" (:service-key mgun-opts)]
       :form-params
       {:from (format (:from msg-opts) (:sandbox mgun-opts))
        :to (:to msg-opts)
        :subject (:subject msg-opts)
        body-type content}
       :socket-timeout 5000
       :conn-timeout 5000
       :accept :json
       :throw-entire-message? true})))


(defn mailgun-event
  "Send an event, or a sequence of events, with the given smtp and msg
  options."
  [mgun-opts msg-opts events]
  ;TODO: error checking to ensure sandbox and service-key are set
  (let [events  (flatten [events])
        subject ((get msg-opts :subject subject) events)
        body    ((get msg-opts :body body) events)]
    (post
      mgun-opts
      (merge msg-opts {:subject subject :body body}))))


(defn mailgun
  "Returns a mailer, which is a function invoked with an address or a sequence
  of addresses and returns a stream. That stream is a function which takes a
  single event, or a sequence of events, and sends email about them.
  
  (def mailer (mailgun))
  (def email (mailer \"xerxes@trioptimum.org\" \"shodan@trioptimum.org\"))
 
  This mailer sends email out via mailgun using the mailgun http api. When used
  it outputs the http response recieved from mailgun.

  (changed :state
    #(info \"mailgun response\" (email %)))

  The first argument is a map of the mailgun options :sandbox and :service-key.
  The second argument is a map of default message options, like :from,
  :subject, or :body.
  
  (def email (mailgun {:sandbox \"mail.relay\" :service-key \"key\"}
                     {:from \"riemann@trioptimum.com\"}))

  If you provide a single map, the mailer will split the mailgun options out
  for you.

  (def email (mailgun {:sandbox \"mail.relay\"
                      :service-key \"foo\"
                      :from \"riemann@trioptimum.com\"}))
  
  By default, riemann uses (subject events) and (body events) to format emails.
  You can set your own subject or body formatter functions by including
  :subject or :body in msg-opts. These formatting functions take a sequence of
  events and return a string.

  (def email (mailgun {} {:body (fn [events] 
                                 (apply prn-str events))}))
  
  This api uses text body by default. If you want to use HTML body, you can set
  a body formatter function returns a map of fields :type and :content.

  (def email (mailgun {} {:body (fn [events] 
                                 {:type :html
                                  :content \"<h1>HTML Body</h1>\"})}))"
  ([] (mailgun {}))
  ([opts]
        (let [mg-keys #{:sandbox :service-key}
              mgun-opts (select-keys opts mg-keys)
              msg-opts  (select-keys opts (remove mg-keys (keys opts)))]
          (mailgun mgun-opts msg-opts)))
  ([mgun-opts msg-opts]
   (let [msg-opts (merge {:from "Riemann <riemann@%s>"} msg-opts)]
     (fn make-stream [& recipients]
       (fn stream [event]
         (let [msg-opts (if (empty? recipients)
                          msg-opts
                          (merge msg-opts {:to recipients}))]
           (mailgun-event mgun-opts msg-opts event)))))))

