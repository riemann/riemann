(ns riemann.keenio
  "Forwards events to Keen IO"
  (:require [clj-http.client :as client])
  (:require [cheshire.core :as json]))

(def ^:private event-url
  "https://api.keen.io/3.0/projects/")

(defn post
  "POST to Keen IO."
  [collection project-id write-key request]
  (let [final-event-url
       (str event-url project-id "/events/" collection)]
    (client/post final-event-url
                 {:body (json/generate-string request)
                  :query-params { "api_key" write-key }
                  :socket-timeout 5000
                  :conn-timeout 5000
                  :content-type :json
                  :accept :json
                  :throw-entire-message? true})))

(defn keenio
  "Creates a keen adapter. Takes your Keen project id and write key, and
  returns a function that accepts an event and sends it to Keen IO. The full
  event will be sent.

  (streams
    (let [kio (keenio \"COLLECTION_NAME\" \"PROJECT_ID\" \"WRITE_KEY\")]
      (where (state \"error\") kio)))"
  [collection project-id write-key]
  (fn [event]
    (post collection project-id write-key event)))
