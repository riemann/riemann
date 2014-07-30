(ns riemann.transport.websockets-test
  (:require [riemann.transport.websockets :as ws]
            [cheshire.core                :as json])
  (:use riemann.common
        [riemann.index :only [index,update]]
        [riemann.logging :only [suppress]]
        [riemann.time :only [unix-time]]
        [clj-http.util         :only [url-encode]]
        clojure.test))

(def empty-core {:index (index)})
(def empty-request  {:query-string nil :request-method :get})

(defn request-with [key value]
  (let [query-string (str (name key) "=" (url-encode (str value)))]
    (assoc empty-request
      :query-string query-string)))

(deftest get-events-test
  (let
      [result (ws/http-get-events empty-core empty-request)]
    (is (= (result :status) 200))
    (is (= ((result :headers) "Content-Type") "application/json"))
    ))

(deftest get-events-with-query-test
  (let [idx  (index)
        core {:index idx}]
    (doall
      (map #(update idx {:host (str "host" % ".example.com") :service "thing" :time (unix-time)})
           (range 1 100)))
    (let [result (ws/http-get-events core (request-with :query "host = \"host3.example.com\""))
          body (result :body)
          items ((json/decode body) "items")]
      (is (= (result :status) 200))
      (is (= (count items) 1))
      )))

(deftest get-events-paging-test
  (let [idx  (index)
        core {:index idx}]
    (doall
      (map #(update idx {:host (str "host" % ".example.com") :service "thing" :time (unix-time)})
           (range 1 1000)))
    (let [result (ws/http-get-events core (request-with :limit 200))
          body (result :body)
          items ((json/decode body) "items")]
      (is (= (count items) 200)))
    (let [result (ws/http-get-events core (request-with :offset 989))
          body (result :body)
          items ((json/decode body) "items")]
      ; should be the last ten elements
      (is (= (count items) 10)))

    ))

(deftest get-events-returns-400
  (is (= ((ws/http-get-events empty-core (request-with :limit "not value")) :status) 400))
  (is (= ((ws/http-get-events empty-core (request-with :offset "-1")) :status) 400))
  (is (= ((ws/http-get-events empty-core (request-with :query "not a query")) :status) 400))
  )

(deftest integer-param-test
  (is (= (ws/integer-param {"p" "123"} :p 321) 123))
  (is (= (ws/integer-param {"missing" "123"} :p 321) 321))
  (is (thrown? IllegalArgumentException (ws/integer-param {"p" "banana"} :p 1)))
  (is (thrown? IllegalArgumentException (ws/integer-param {"p" "-1"} :p 1 pos?)))
  )
