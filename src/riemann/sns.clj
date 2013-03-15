(ns riemann.sns
  "Publish to AWS SNS topic(s) about events. Create a publisher with
  (sns-publisher opts), then create streams which publish to topic(s) with
  (your-publisher \"your::arn\"). Or simply call sns-publish directly."
  (:require clj-aws.core
            clj-aws.sns)
  (:use [clojure.string :only [join]]
        riemann.common))

(def max-subject-bytes 100)
(def max-body-bytes 8092)

(defn- truncate-subject [s]
  (truncate-bytes s max-subject-bytes))

(defn- truncate-body [s]
  (truncate-bytes s max-body-bytes))

(defn sns-publish
  "Send an event, or a sequence of events, with the given aws and msg
  options."
  [aws-opts msg-opts events]
  (let [
        creds (clj-aws.core/credentials (:access_key aws-opts) (:secret_key aws-opts))
        client (if (nil? (:region aws-opts))
                 (clj-aws.sns/client creds)
                 (clj-aws.sns/client creds :region (:region aws-opts)))
        events  (flatten [events])
        subject ((get msg-opts :subject subject) events)
        body    ((get msg-opts :body body) events)
        msg-opts (merge msg-opts {:subject subject :body body})
        arns    (flatten [(:arn msg-opts)])
        subject (truncate-subject (:subject msg-opts))
        body    (truncate-body (:body msg-opts))]
    (doseq [arn arns]
       (clj-aws.sns/publish client arn subject body))))

(defn sns-publisher
  "Returns a publisher, which is a function invoked with a topic ARN or a
  sequence of ARNs and returns a stream. That stream is a function which takes a
  single event, or a sequence of events, and publishes a message about them.

  (def sns (sns-publisher))

  (changed :state
    (sns \"arn:aws:sns:region:id:xerxes\" \"arn:aws:sns:region:id:shodan\"))

  The first argument is a map of AWS credentials:

    :access_key ; required
    :secret_key ; required
    :region     ; optional

  The second argument is a map of default message options, like :body or
  :subject.

  (def sns (sns-publisher {:access_key \"my-access-key\"
                           :secret_key \"my-secret-key\"}
                          {:subject \"something is ok\"}))

  If you provide a single map, AWS options will be split out for you.

  (def sns (sns-publisher {:access_key \"your-access-key\"
                           :secret_key \"your-secret-key\"
                           :subject \"something went wrong\"}))
  
  aws-opts and msg-opts are passed to clj-aws. For more documentation, see
  https://github.com/pingles/clj-aws
  
  By default, riemann uses (subject events) and (body events) to format emails.
  You can set your own subject or body formatter functions by including
  :subject or :body in msg-opts. These formatting functions take a sequence of
  events and return a string.

  (def sns (sns-publisher {} {:body (fn [events] 
                                      (apply prn-str events))}))"
  ([] (sns-publisher {}))
  ([opts]
        (let [aws-keys #{:access_key :secret_key :region}
              aws-opts (select-keys opts aws-keys)
              msg-opts  (select-keys opts (remove aws-keys (keys opts)))]
          (sns-publisher aws-opts msg-opts)))
  ([aws-opts msg-opts]
     (fn make-stream [& recipients]
       (fn stream [event]
         (let [msg-opts (if (empty? recipients)
                          msg-opts
                          (merge msg-opts {:arn recipients}))]
           (sns-publish aws-opts msg-opts event))))))
