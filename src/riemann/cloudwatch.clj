(ns riemann.cloudwatch
  "Forwards riemann events to Amazon CloudWatch"
  (:import org.joda.time.DateTime)
  (:require [amazonica.core :as core]
            [amazonica.aws.cloudwatch :as cloudwatch]))

(defn generate-datapoint
  "Accepts riemann event and converts it into cloudwatch datapoint."
  [event]
  (conj []
        {:metric-name (:service event)
         :timestamp (DateTime. )
         :value (:metric event)
         :dimensions [{:name "Host"
                       :value (:host event)}]}))

(defn cloudwatch
  "Returns a function which accepts an event and sends it to cloudwatch.
  Usage:

  (cloudwatch {:access-key \"AKJALPVWYQ6BFMVLSZDA\"
               :secret-key \"ZFEemkafy0paNMx5JcinMUiOC4dcMKhxXCL85DhM\"})
  
  (cloudwatch {}) will make use of IAM Instance Profile.

  Options:

  :access-key  AWS access key of your AWS Account.

  :secret-key  AWS secret key for the above access key.

  :endpoint    AWS Endpoint for posting metrics(changes with AWS region).

  :namespace   AWS CloudWatch namespace."
  [opts]
  (let [opts (if (or (contains? opts :access-key) (contains? opts :secret-key))
               (merge 
                    {:access-key "aws-access-key"
                     :secret-key "aws-secret-key"
                     :endpoint   "monitoring.us-east-1.amazonaws.com"
                     :namespace  "Riemann"} opts)
               (merge 
                    {:endpoint   "monitoring.us-east-1.amazonaws.com"
                     :namespace  "Riemann"} opts))]
    (fn [event]
      (when (:metric event)
        (when (:service event)
          (let [datapoint (generate-datapoint event)]
            (cloudwatch/put-metric-data opts
                                        :namespace (:namespace opts)
                                        :metric-data datapoint)))))))
