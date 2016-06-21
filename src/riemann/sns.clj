(ns riemann.sns
  "Publish to AWS SNS topic(s) about events. Create a publisher with
  (sns-publisher opts), then create streams which publish to topic(s) with
  (your-publisher \"your::arn\"). Or simply call sns-publish or
  sns-publish-async directly."
  (:import (com.amazonaws AmazonWebServiceClient)
           (com.amazonaws.auth BasicAWSCredentials
                               DefaultAWSCredentialsProviderChain)
           [com.amazonaws.regions RegionUtils]
           [com.amazonaws.handlers AsyncHandler]
           (com.amazonaws.services.sns AmazonSNS
                                       AmazonSNSClient
                                       AmazonSNSAsyncClient)
           [com.amazonaws.services.sns.model PublishRequest])
  (:use [clojure.set :only [union]]
        [clojure.string :only [join]]
        riemann.common))

(def max-subject-bytes 100)
(def max-body-bytes 8092)

(defn- truncate-subject [s]
  (truncate-bytes s max-subject-bytes))

(defn- truncate-body [s]
  (truncate-bytes s max-body-bytes))

(defn- try-formatting [msg-opts events key default-formatter]
  (let [formatter (get msg-opts key default-formatter)]
    (if (ifn? formatter)
      (formatter events)
      formatter)))

(defn- compose-message [msg-opts events]
  (let [events  (flatten [events])
        subject (try-formatting msg-opts events :subject riemann.common/subject)
        body    (try-formatting msg-opts events :body riemann.common/body)
        msg-opts (merge msg-opts {:subject subject :body body})]
    {:arns (flatten [(:arn msg-opts)])
     :subject (truncate-subject (:subject msg-opts))
     :body (truncate-body (:body msg-opts))}))

(defn- aws-region
  "Get a Region instance by name, e.g. \"us-gov-west-1\" \"us-east-1\""
  [name]
  (RegionUtils/getRegion name))

(defn- aws-credentials [credentials]
  (if (or (nil? (:access-key credentials))
          (nil? (:secret-key credentials)))
    (DefaultAWSCredentialsProviderChain.)
    (BasicAWSCredentials.
     (:access-key credentials)
     (:secret-key credentials))))

(defn- aws-client
  [klass opts]
  (let [aws-creds (aws-credentials opts)
        client ^AmazonWebServiceClient (clojure.lang.Reflector/invokeConstructor
                                         klass (into-array Object [aws-creds]))]
    (when-let [region (:region opts)]
      (.setRegion client (aws-region region)))
    client))

(defn- aws-sns-client [opts]
  (aws-client AmazonSNSClient opts))

(defn- aws-sns-client-async [opts]
  (aws-client AmazonSNSAsyncClient opts))

(defn- aws-sns-publish [^AmazonSNS client arn body subject]
  (.publish client (PublishRequest. arn body subject)))

(defn- aws-sns-publish-async
  ([^AmazonSNSAsyncClient client arn body subject]
     (.publishAsync client (PublishRequest. arn body subject)))
  ([^AmazonSNSAsyncClient client arn body subject success error]
     (.publishAsync
      client
      (PublishRequest. arn body subject)
      (reify AsyncHandler
             (onSuccess [_ request result]
                        (success request result))
             (onError [_ exception]
                      (error exception))))))

(defn sns-publish
  "Synchronously publish an event, or a sequence of events,
  with the given aws and msg options."
  [aws-opts msg-opts events]
  (let [client (aws-sns-client aws-opts)
        {arns :arns subject :subject body :body} (compose-message msg-opts events)]
    (doseq [arn arns]
       (aws-sns-publish client arn body subject))))

(defn sns-publish-async
  "Asynchronously publish an event, or a sequence of events,
  with the given aws, msg and async options."
  [aws-opts msg-opts events & [async-opts]]
  (let [client (aws-sns-client-async aws-opts)
        {arns :arns subject :subject body :body} (compose-message msg-opts events)
        publish (if (and async-opts (:success async-opts) (:error async-opts))
                  #(aws-sns-publish-async client % body subject (:success async-opts) (:error async-opts))
                  #(aws-sns-publish-async client % body subject))]
    (doseq [arn arns]
       (publish arn))))

(def aws-credential-keys #{:access-key :secret-key :region})
(def aws-async-keys #{:async :success :error})
(def aws-keys (union aws-credential-keys aws-async-keys))

(defn- sift-opts [opts]
  {:aws-opts (select-keys opts aws-credential-keys)
   :async-opts (select-keys opts aws-async-keys)
   :msg-opts  (select-keys opts (remove aws-keys (keys opts)))})

(defn sns-publisher
  "Returns a publisher, which is a function invoked with a topic ARN or a
  sequence of ARNs and returns a stream. That stream is a function which takes a
  single event, or a sequence of events, and publishes a message about them.

  (def sns (sns-publisher))

  (changed :state
    (sns \"arn:aws:sns:region:id:xerxes\" \"arn:aws:sns:region:id:shodan\"))

  The first argument is a map of AWS credentials:

    :access-key ; required
    :secret-key ; required
    :region     ; optional

  The :region value is passed to com.amazonaws.regions.RegionUtils/getRegion.
  For a list of region names that you can use, see:
  https://github.com/aws/aws-sdk-java/blob/master/src/main/java/com/amazonaws/regions/Regions.java

  (Note: `getRegion` expects the value of the `name` instance variable,
  not the enum type name.)

  The second argument is a map of default message options, like :body or
  :subject.

  (def sns (sns-publisher {:access-key \"my-access-key\"
                           :secret-key \"my-secret-key\"}
                          {:subject \"something is ok\"}))

  The third is an optional map specifying async options:

    :async   ; optional true / false (default)
    :success ; optional callback invoked on success
             ; e.g. (fn [req res] ...)
    :error   ; optional callback invoked on error
             ; e.g. (fn [exception] ...)
             ; you must specify both :success and :error
             ; or else, none at all

  If you provide a single map, they will be split out for you.

  (def sns (sns-publisher {:access-key \"your-access-key\"
                           :secret-key \"your-secret-key\"
                           :subject \"something went wrong\"
                           :async true}))

  By default, riemann uses (riemann.common/subject events) and
  (riemann.common/body events) to format messages.
  You can set your own subject or body formatter functions by including
  :subject or :body in msg-opts. These formatting functions take a sequence of
  events and return a string.

  (def sns (sns-publisher {} {:body (fn [events]
                                      (apply prn-str events))}))"
  ([] (sns-publisher {}))
  ([opts]
     (let [{aws-opts   :aws-opts
            async-opts :async-opts
            msg-opts   :msg-opts} (sift-opts opts)]
       (sns-publisher aws-opts msg-opts async-opts)))
  ([aws-opts msg-opts & [async-opts]]
     (if (and async-opts (:async async-opts))
       (assert (or
                (and (nil? (:error async-opts)) (nil? (:success async-opts)))
                (and (:error async-opts) (:success async-opts)))
               "You must specify both callbacks, or none at all"))
     (let [publish (if (and async-opts (:async async-opts))
                     (fn [msg-opts event]
                       (sns-publish-async aws-opts msg-opts event async-opts))
                     (fn [msg-opts event]
                       (sns-publish aws-opts msg-opts event)))]
       (fn make-stream [& recipients]
         (fn stream [event]
           (let [msg-opts (if (empty? recipients)
                            msg-opts
                            (merge msg-opts {:arn recipients}))]
             (publish msg-opts event)))))))
