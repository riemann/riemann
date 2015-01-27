(ns riemann.boundary
  "Forwards events to Boundary Premium."
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.string :as s]))

(def ^:private base-uri "Boundary API base URI."
  "https://premium-api.boundary.com")
(def ^:private version "Boundary API version to use."
  "v1")
(def ^:private sync-path "Path part for syncrhonous communication."
  "measurements")
  (def ^:private async-path "Path part for asyncrhonous
  communication."
    "measurementsAsync")

(defn ^:private boundarify
  "As of Boundary's specs, metric ids can only contain characters
  matching \"[A-Z0-9_]\", thus all other characters will be stripped
  and the remaining ones will be upcased.

  To preserve structure, unacceptable characters will be removed
  *after* substituting spaces with underscores.

  Should an organization name be provided, it will be placed before
  the name of the service.

  Last but not least, if after all the manipulation of the string, no
  characters remain (i.e. empty string), an exception is thrown.

  Examples:

  (boundarify \"foo\") => \"FOO\"
  (boundarify \"foo bar\") => \"FOO_BAR\"
  (boundarify \"foo@\") => \"FOO\"
  (boundarify \"foo@bar\") => \"FOOBAR\"
  (boundarify \"foo\" \"org\") => \"ORG_FOO\"
  (boundarify \"!#@\") => exception
  (boundarify \"!#@\" \"org\") => exception
  "
  [service & [organization]]
  (let [good-ones (-> service
                      (s/replace #"\s+" "_")
                      s/upper-case
                      (s/replace #"[^A-Z0-9_]" ""))]
    (when (empty? good-ones)
      (throw (RuntimeException.
              (str "can't accept the given service string \""
                   service "\" as metric id"))))
    (if (nil? organization)
      good-ones
      (str (s/upper-case organization) "_" good-ones))))

(defn ^:private packer-upper
  "Returns a function packs up the events in a form suitable for
  Boundary's API.

  If a metric-id is given, it will be used for all the events in the
  pack. Otherwise, every single event service is \"boundarified\". In
  both cases, organization is prepended if given."
  [{:keys [metric-id org]}]
  (let [helper
        #(vector
          (:host %)
          (if (nil? metric-id)
            (boundarify (:service %) org)
            (boundarify metric-id org))
          (:metric %)
          (:time %))]
    (fn [events]
      (mapv helper events))))

(defn boundary
  "Returns a function used to generate specific senders (like mailer)
  that takes three optional named arguments, namely :metric-id
  :org and :async, that modify which metric the events are sent to.

  Specifically, if :metric-id is supplied, every single event is sent
  to that metric, otherwise the event's :service field is used to
  construct the destination Boundary's metric id. In both cases,
  organization is prepended if non nil. The last argument, :async,
  switches the endpoint to the asynchronous one (default is sync).

  Examples:

  (def bdry (boundary eml tkn))
  (when :foo (bdry)) => builds the destination metric id with :service
  (when :foo (bdry {:async true})) => same as previous, but async
  (when :foo (bdry {:metric-id \"METRIC_ID\"})) => sends to METRIC_ID"
  [email token]
  (fn b
    ([] (b {}))
    ([{:keys [metric-id org async]}]
     (let [pack-up (packer-upper {:metric-id metric-id :org org})
           path (if async async-path sync-path)]
       (fn [events]
         (let [pack (pack-up events)
               req-map {:scheme :https
                        :basic-auth [email token]
                        :headers {"Content-Type" "application/json"}
                        :body (json/generate-string pack)}]
           (client/post (s/join "/" [base-uri version path])
                        req-map)))))))
