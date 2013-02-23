(ns riemann.test.client
  (:use riemann.common
        riemann.core
        riemann.transport.tcp
        riemann.client
        [riemann.index :only [index]]
        [riemann.logging :only [suppress]]
        clojure.test))

(riemann.logging/init)

(deftest reconnect
         (suppress ["riemann.transport.tcp" "riemann.core"]
                   (let [server (tcp-server)
                         core   (transition! (core) {:services [server]})
                         client (tcp-client)]
                     (try
                       ; Initial connection works
                       (is (send-event client {:service "test"}))

                       ; Kill server; should fail.
                       (stop! core)
                       (.setMinimumReconnectInterval client 0)
                       (is (thrown? java.net.SocketException
                                    (send-event client {:service "test"})))

                       ; Restart server; should work
                       (start! core)
                       (try
                         (send-event client {:service "test"})
                         (finally
                           (stop! core)))

                       (finally
                         (close-client client)
                         (stop! core))))))

; Check that server error messages are correctly thrown.
(deftest server-errors
         (suppress ["riemann.transport.tcp" "riemann.core"]
           (let [index (index)
                 server (tcp-server core)
                 core   (transition! (core) {:services [server]
                                             :index index})
                 client (tcp-client)]

             (try
               (is (thrown? com.aphyr.riemann.client.ServerError
                            (query client "invalid!")))
               
               (let [e (try (query client "invalid!")
                      (catch com.aphyr.riemann.client.ServerError e e))]
                 (is (= "parse error: invalid term \"invalid\"" (.getMessage e))))
                 
               
               (finally
                 (close-client client)
                 (stop! core))))))
