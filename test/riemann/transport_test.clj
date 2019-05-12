(ns riemann.transport-test
  (:require [riemann.client :as client]
            [riemann.codec :as codec]
            [riemann.common :refer :all]
            [riemann.core :refer :all]
            [riemann.index :as index]
            [riemann.logging :as logging]
            [riemann.pubsub :as pubsub]
            [riemann.transport.sse :refer [sse-server]]
            [riemann.transport.tcp :refer :all]
            [riemann.transport.udp :refer :all]
            [riemann.transport.websockets :refer :all]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.java.io :as io]
            [clojure.test :refer :all])
  (:import (java.net Socket
                     SocketException
                     InetAddress)
           (java.io IOException
                    DataOutputStream)))

(logging/init)

(deftest ws-put-events-test
  (logging/suppress
    ["riemann.transport"
     "riemann.core"
     "riemann.pubsub"]
    (let [server (ws-server {:port 15556})
          uri    "http://127.0.0.1:15556/events"
          core   (transition! (core) {:services [server]})]
      ; Two simple events
      (let [res (http/put uri
                          {:body "{\"service\": \"foo\"}\n{\"service\": \"bar\"}\n"})]
        (is (= 200 (:status res)))
        (is (= "{}\n{}\n" (:body res))))

      ; A time
      (let [res (http/put uri
                          {:body "{\"service\": \"foo\", \"time\": \"2013-04-15T18:06:58-07:00\"}\n"})]
        (is (= 200 (:status res)))
        (is (= "{}\n" (:body res))))

      ; An invalid time
      (let [res (http/put uri
                          {:body "{\"time\": \"xkcd\"}"})]
        (is (= 200 (:status res)))
        (is (= {:error "Invalid format: \"xkcd\""}
               (json/parse-string (:body res) true))))

      (stop! core))))

(deftest sse-subscribe-events-test
  (logging/suppress ["riemann.transport"
                     "riemann.core"
                     "riemann.pubsub"]
   (let [s1       (tcp-server {:port 15555})
         s2       (sse-server {:port 15558})
         core     (core)
         index    (wrap-index (index/index) (:pubsub core))
         pubsub   (pubsub/pubsub-registry)
         core     (transition!
                   core
                   {:index    index
                    :pubsub   pubsub
                    :services [s1 s2]
                    :streams  [index]})
         client   (client/tcp-client {:port 15555})
         res      (http/get "http://127.0.0.1:15558/index?query=true"
                            {:as :stream})
         events   [{:host "h1" :service "s", :metric -6, :time 0}
                   {:host "h2" :service "s2" :metric 1.5, :time 10}]]

     ; Send events to server over TCP
     @(client/send-events client events)

     ; And read back via SSE
     (let [stream (line-seq (io/reader (:body res)))]
       (try
         (let [events' (->> stream
                            (take-nth 2) ; Delimiter is \n\n, why? --aphyr
                            (take 2)
                            (map (partial re-matches #"data: (.*)"))
                            (map second)
                            (map #(json/parse-string % true)))]
           (is (= (set events')
                  #{{:host "h1", :service "s", :state nil, :description nil,
                    :metric -6, :tags nil, :time "1970-01-01T00:00:00.000Z",
                    :ttl nil}
                   {:host "h2", :service "s2", :state nil, :description nil,
                    :metric 1.5, :tags nil, :time "1970-01-01T00:00:10.000Z",
                    :ttl nil}})))

         (finally
           ; Shut down server and close client stream
           (client/close! client)
           (stop! core)
           ; lol actually not safe to close the reader; apache httpclient
           ; explodes because .close calls .read and it's closed so fml
           ;(dorun stream)
           ))))))

(deftest udp-test
  (logging/suppress ["riemann.transport"
                     "riemann.core"
                     "riemann.pubsub"]
    (let [port   15555
          server (udp-server {:port port})
          sink   (promise)
          core   (transition! (core) {:services [server]
                                      :streams  [(partial deliver sink)]})
          client (client/udp-client {:port port})
          event  (event {:service "hi" :state "ok" :metric 1.23})]
      (try
        (client/send-event client event)
        (is (= (update event :time double) (deref sink 1000 :timed-out)))
        (finally
          (client/close! client)
          (stop! core))))))

(defn test-tcp-client
  [client-opts server-opts]
  (logging/suppress ["riemann.transport"
                     "riemann.core"
                     "riemann.pubsub"]
    (let [server (tcp-server server-opts)
          index (wrap-index (index/index))
          core (transition! (core) {:index index
                                    :services [server]
                                    :streams [index]})]
      (try
        (let [client (apply client/tcp-client (mapcat identity client-opts))]
          (try
            @(client/send-event client {:service "laserkat"})
            (is (= "laserkat" (-> client
                                (client/query "service = \"laserkat\"")
                                deref
                                first
                                :service)))
            (finally
              (client/close! client))))
        (finally
          (stop! core))))))

(deftest tls-test
  (let [server {:tls? true
                :key "test/data/tls/server.pkcs8"
                :cert "test/data/tls/server.crt"
                :ca-cert "test/data/tls/demoCA/cacert.pem"
                :port 15555}
        client {:tls? true
                :key "test/data/tls/client.pkcs8"
                :cert "test/data/tls/client.crt"
                :ca-cert "test/data/tls/demoCA/cacert.pem"
                :port 15555}]
    ; Works with valid config
    (test-tcp-client client server)

    (logging/suppress ["io.riemann.riemann.client.TcpTransport"]
      ; Fails with mismatching client key/cert
      (is (thrown? IOException
                   (test-tcp-client (assoc client :key (:key server))
                                    server)))

      ; Fails with non-CA client CA cert
      (is (thrown? IOException
                   (test-tcp-client (assoc client :ca-cert (:cert client))
                                    server)))
      ; Fails with mismatching server key/cert
      (is (thrown? IOException
                   (test-tcp-client client
                                    (assoc server :key (:key client)))))
      ; Fails with non-CA server CA cert
      (is (thrown? IOException
                   (test-tcp-client client
                                    (assoc server :ca-cert
                                           (:cert client))))))))

(deftest ignores-garbage
  (logging/suppress ["riemann.core"
                     "riemann.transport"
                     "riemann.pubsub"]
    (let [port   15555
          server (tcp-server {:port port})
          core   (transition! (core) {:services [server]})
          sock   (Socket. "localhost" port)]
      (try
        ; Write garbage
        (doto (.getOutputStream sock)
          (.write (byte-array (map byte (range -128 127))))
          (.flush))

        ; lmao, (.isClosed sock) is meaningless
        (is (= -1 (.. sock getInputStream read)))

        (finally
          (.close sock)
          (stop! core))))))

(deftest tcp-server-testing-test
  (let [server (tcp-server {:port 15555})]
    (binding [riemann.test/*testing* true]
      (riemann.service/start! server)
      (is (= nil @(:killer server))))))

(deftest udp-server-testing-test
  (let [server (udp-server {:port 15555})]
    (binding [riemann.test/*testing* true]
      (riemann.service/start! server)
      (is (= nil @(:killer server))))))

(deftest sse-server-testing-test
  (let [server (sse-server)]
    (binding [riemann.test/*testing* true]
      (riemann.service/start! server)
      (is (= nil @(:server server))))))

(deftest websocket-server-testing-test
  (let [server (ws-server)]
    (binding [riemann.test/*testing* true]
      (riemann.service/start! server)
      (is (= nil @(:server server))))))
