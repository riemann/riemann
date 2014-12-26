(ns riemann.transport-test
  (:use riemann.common
        riemann.core
        riemann.transport.tcp
        riemann.transport.udp
        riemann.transport.websockets
        riemann.logging
        clojure.test
        lamina.core
        aleph.tcp
        aleph.udp
        gloss.core)
  (:require [clj-http.client :as http]
            [riemann.pubsub :as pubsub]
            [riemann.transport.sse :refer [sse-server]]
            [aleph.http :refer [http-request]]
            [aleph.formats :as formats]
            [cheshire.core :as json]
            [riemann.client :as client]
            [riemann.index :as index])
  (:import (java.net Socket
                     InetAddress)
           (org.jboss.netty.buffer ChannelBuffers)
           (java.io IOException)))

(deftest ws-put-events-test
  (riemann.logging/suppress
    [;"riemann.transport"
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
  (riemann.logging/suppress [;"riemann.transport"
                             "riemann.core" "riemann.pubsub"]
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
         convert  (comp json/parse-string
                        second
                        (partial re-matches #"data: (.*)\n\n")
                        formats/bytes->string)
         response @(http-request
                     {:method :get
                      :url    "http://127.0.0.1:15558/index?query=true"})]
     (try
       (client/send-event client {:service "service1"})
       (client/send-event client {:service "service2"})
       (let [[r2 r1] (->> response
                          :body
                          (take* 2)
                          (map* convert)
                          channel->lazy-seq)]
         (is (#{"service1" "service2"} (get r1 "service")))
         (is (#{"service1" "service2"} (get r2 "service"))))
       (finally
         (stop! core))))))

(deftest udp-test
  (riemann.logging/suppress [;"riemann.transport"
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
        (is (= event (deref sink 1000 :timed-out)))
        (finally
          (client/close-client client)
          (stop! core))))))

(defn test-tcp-client
  [client-opts server-opts]
  (riemann.logging/suppress [;"riemann.transport"
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
            (client/send-event client {:service "laserkat"})
            (is (= "laserkat" (-> client
                                (client/query "service = \"laserkat\"")
                                first
                                :service)))
            (finally
              (client/close-client client))))
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

    (riemann.logging/suppress ["com.aphyr.riemann.client.TcpTransport"]
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
  (riemann.logging/suppress ["riemann.core"];"riemann.transport"
                             ;"riemann.core"
                             ;"riemann.pubsub"]
    (let [port   15555
          server (tcp-server {:port port})
          core   (transition! (core) {:services [server]})
          sock   (Socket. "localhost" port)]
      (try
        ; Write garbage
        (prn "writing")
        (doto (.getOutputStream sock)
          (.write (int 1))
          (.write (int 0))
          (.write (int 0))
          (.write (int 1))
          (.write (int -128))
          (.flush))
        (prn "wrote")

        ; Should close socket
        (is (.isClosed sock))
;        (is (thrown? java.lang.IllegalStateException
;                     (.. sock getInputStream read)))
        (catch Throwable t
          (prn "Caught!")
          (.printStackTrace t))
        (finally
          (prn "Shutting down")
          (.close sock)
          (stop! core))))))
