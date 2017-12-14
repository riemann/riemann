(ns riemann.keenio-test
  (:require [riemann.keenio :as k]
            [riemann.time :refer [unix-time]]
            [riemann.test-utils :refer [with-mock]]
            [cheshire.core :as json]
            [clojure.test :refer :all]
            [clj-http.client :as client]))

(deftest keenio-test
  (with-mock [calls client/post]
    (let [k (k/keenio "ships" "tau-ceti-v" "shodan")]
      (k {:host    "vonbraun"
          :service "the many"
          :description "the glory of the flesh"
          :state   "perfect"
          :time    12345678
          :ttl     300})
      (is (= 1 (count @calls)))
      (let [[url payload] (last @calls)]
        (is (= url "https://api.keen.io/3.0/projects/tau-ceti-v/events/ships"))
        (is (= (update-in payload [:body] json/parse-string)
               {:body {"description" "the glory of the flesh"
                      "service" "the many"
                      "time" 12345678
                      "state" "perfect"
                      "host" "vonbraun"
                      "ttl" 300}
               :query-params {"api_key" "shodan"}
               :socket-timeout 5000
               :conn-timeout 5000
               :content-type :json
               :accept :json
               :throw-entire-message? true}))))))
