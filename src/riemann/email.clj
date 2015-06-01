(ns riemann.email
  "Send email about events. Create a mailer with (mailer opts), then create
  streams which send email with (your-mailer \"shodan@tau.ceti.five\"). Or
  simply call email-event directly."
  (:use [riemann.common :only [deprecated body subject human-uniq]])
  (:use postal.core)
  (:use [clojure.string :only [join]]))

(defn email-event
  "Send an event, or a sequence of events, with the given smtp and msg
  options."
  [smtp-opts msg-opts events]
  (let [events  (flatten [events])
        subject ((get msg-opts :subject subject) events)
        body    ((get msg-opts :body body) events)]
    (send-message
      smtp-opts
      (merge msg-opts {:subject subject :body body}))))

(defn mailer
  "Returns a mailer, which is a function invoked with an address or a sequence
  of addresses and returns a stream. That stream is a function which takes a
  single event, or a sequence of events, and sends email about them.
  
  (def email (mailer))
 
  This mailer uses the local sendmail.

  (changed :state
    (email \"xerxes@trioptimum.org\" \"shodan@trioptimum.org\"))

  The first argument are SMTP options like :host, :port, :user, :pass, :tls,
  and :ssl. The second argument is a map of default message options, like :from
  or :subject.
  
  (def email (mailer {:host \"mail.relay\"}
                     {:from \"riemann@trioptimum.com\"}))

  If you provide a single map, mailer will split the SMTP options out for you.

  (def email (mailer {:host \"mail.relay\"
                      :user \"foo\"
                      :pass \"bar\"
                      :from \"riemann@trioptimum.com\"}))
  
  smtp-opts and msg-opts are passed to postal. For more documentation, see
  https://github.com/drewr/postal
  
  By default, riemann uses (subject events) and (body events) to format emails.
  You can set your own subject or body formatter functions by including
  :subject or :body in msg-opts. These formatting functions take a sequence of
  events and return a string.

  (def email (mailer {} {:body (fn [events] 
                                 (apply prn-str events))}))"
  ([] (mailer {}))
  ([opts]
        (let [smtp-keys #{:host :port :user :pass :ssl :tls :sender}
              smtp-opts (select-keys opts smtp-keys)
              msg-opts  (select-keys opts (remove smtp-keys (keys opts)))]
          (mailer smtp-opts msg-opts)))
  ([smtp-opts msg-opts]
   (let [msg-opts (merge {:from "riemann"} msg-opts)]
     (fn make-stream [& [head & tail :as args]]
       (let [recipients (if (and (sequential? head) (empty? tail)) head args)]
         (assert (every? string? recipients)
                 (str "email was passed a recipient that wasn't a string: "
                      (pr-str recipients)
                      " if those are events, you'll wanna call (email \"someemail@example.com\")"))
         (fn stream [event]
           (let [msg-opts (if (empty? recipients)
                            msg-opts
                            (merge msg-opts {:to recipients}))]
             (email-event smtp-opts msg-opts event))))))))
