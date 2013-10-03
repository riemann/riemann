(ns riemann.transport.http-test
  (:require [riemann.transport.http :as http]
            [cheshire.core          :as json])
  (:use riemann.common
        [riemann.index :only [index,update]]
        [riemann.logging :only [suppress]]
        clojure.test))

(def empty-core (atom {:index (index)}))
(def empty-request  {:params {}})

(defn request-with [key value]
  (let [new-params (assoc (empty-request :params) (name key) (str value))]
    (assoc empty-request
      :params new-params)))

(deftest get-events-test
  (let
      [result (http/get-events empty-core empty-request)]
    (is (= (result :status) 200))
    (is (= ((result :headers) "Content-Type") "application/json"))
    ))

(deftest get-events-with-query-test
  (let [idx  (index)
        core (atom {:index idx})]
    (doall
      (map #(update idx {:host (str "host" % ".example.com") :service "thing"})
           (range 1 100)))
    (let [result (http/get-events core (request-with :q "host = \"host3.example.com\""))
          body (result :body)
          items ((json/decode body) "items")]
      (is (= (result :status) 200))
      (is (= (count items) 1))
      )))

(deftest get-events-paging-test
  (let [idx  (index)
        core (atom {:index idx})]
    (doall
      (map #(update idx {:host (str "host" % ".example.com") :service "thing"})
           (range 1 1000)))
    (let [result (http/get-events core (request-with :limit 200))
          body (result :body)
          items ((json/decode body) "items")]
      (is (= (count items) 200)))
    (let [result (http/get-events core (request-with :offset 989))
          body (result :body)
          items ((json/decode body) "items")]
      ; should be the last ten elements
      (is (= (count items) 10)))

    ))

(deftest get-events-host
  (let [idx  (index)
        core (atom {:index idx})]
    (doall
      (map #(update idx {:host (str "host" % ".example.com") :service "thing"})
           (range 1 10)))
    (let [result (http/get-events-by-host core empty-request "host2.example.com")
          body (result :body)
          items ((json/decode body) "items")]
      (is (= (count items) 1))
      (is (= (result :status) 200)))
    ))

(deftest get-events-host-service
  (let [idx  (index)
        core (atom {:index idx})]
    (update idx {:host "host1.example.com" :service "wibble"})
    (let [result (http/get-events-by-host-service core empty-request "host1.example.com" "wibble")
          body (result :body)
          event (json/decode body)]
      (is (= (event "host") "host1.example.com"))
      (is (= (event "service") "wibble"))
      (is (= (result :status) 200)))
    (let [result (http/get-events-by-host-service core empty-request "doesnotexist.example.com" "wibble")]
      (is (= (result :status) 404)))
    ))

(deftest get-events-returns-400
  (is (= ((http/get-events empty-core (request-with :limit "not value")) :status) 400))
  (is (= ((http/get-events empty-core (request-with :offset "-1")) :status) 400))
  )

(deftest integer-param-test
  (is (= (http/integer-param {"p" "123"} :p 321) 123))
  (is (= (http/integer-param {"missing" "123"} :p 321) 321))
  (is (thrown? IllegalArgumentException (http/integer-param {"p" "banana"} :p 1)))
  (is (thrown? IllegalArgumentException (http/integer-param {"p" "-1"} :p 1 pos?)))
  )


