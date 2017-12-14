(ns riemann.logentries-test
  (:require [riemann.logentries :refer [logentries event-to-le-format]]
            [riemann.logging :as logging]
            [riemann.time :refer :all]
            [clojure.test :refer :all])
  (:import (java.net Socket
                     ServerSocket)
           (java.io BufferedReader
                    InputStreamReader)))

(logging/init)

(def host "localhost")
(def port 8273)
(def token "my-token-123")

(defn test-event [opts]
  (merge {:host "localhost"
          :service "logentries test good"
          :description "Testing a log entry with ok state"
          :state "ok"
          :time (unix-time)}
         opts))

(defprotocol LogentriesStub
  (open [client]
        "Creates a Logentries stub")
  (close [client]
         "Cleans up (closes sockets etc.)"))

(defrecord DefaultLogentriesStub [^int port]
  LogentriesStub
  (open [this]
    (let [server-socket (ServerSocket. port)
          client-socket (promise)
          input         (promise)]
      (future
        (let [socket (.accept server-socket)
              in (BufferedReader. (InputStreamReader. (.getInputStream socket)))]
          (deliver client-socket socket)
          (deliver input in)))
      (assoc this
             :server-socket server-socket
             :client-socket client-socket
             :input input)))
  (close [this]
    (.close ^BufferedReader @(:input this))
    (.close ^Socket @(:client-socket this))
    (.close ^ServerSocket (:server-socket this))))

(deftest event-to-le-format-test
  (let [message (event-to-le-format {:description "New user" :service "production front" :state "ok"})]
    (is (= message
           "New user, service='production front' state='ok'"))))

(deftest test-logentries
  (logging/suppress ["riemann.logentries"]
    (let [le-stub (open (DefaultLogentriesStub. port))
          le (logentries {:host host
                          :port port
                          :token token
                          :pool-size 1
                          :claim-timeout 1})]
      (le (test-event {:description "Test event 1"}))
      (le (test-event {:description "Test event 2"}))
      (let [in @(:input le-stub)
            line-1 (.readLine in)
            line-2 (.readLine in)]
        (is (.startsWith line-1 "Test event 1"))
        (is (.startsWith line-2 "Test event 2"))
        (is (.endsWith line-1 token))
        (is (.endsWith line-2 token)))
      (close le-stub))))
