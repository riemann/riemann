(ns riemann.test.client
  (:use riemann.common
        riemann.core
        riemann.server
        riemann.client
        riemann.index
        [riemann.logging :only [suppress]]
        clojure.test))

(deftest reconnect
         (suppress "riemann.server"
                   (let [server (tcp-server (core))
                         client (tcp-client)]
                     (try
                       ; Initial connection works
                       (is (send-event client {:service "test"}))

                       ; Kill server; should fail.
                       (server)
                       (.setMinimumReconnectInterval client 0)
                       (is (thrown? java.net.SocketException
                                    (send-event client {:service "test"})))

                       ; Restart server; should work
                       (let [server (tcp-server (core))]
                         (try
                           (send-event client {:service "test"})
                           (finally
                             (server))))

                       (finally
                         (close-client client)
                         (server))))))

; Check that server error messages are correctly thrown.
(deftest server-errors
         (suppress "riemann.server"
           (let [core (core)
                 index (index)
                 server (tcp-server core)
                 client (tcp-client)]

             (reset! (:index core) index)

             (try
               (is (thrown? com.aphyr.riemann.client.ServerError
                            (query client "invalid!")))
               
               (let [e (try (query client "invalid!")
                      (catch com.aphyr.riemann.client.ServerError e e))]
                 (is (= "parse error: invalid term \"invalid\"" (.getMessage e))))
                 
               
               (finally
                 (close-client client)
                 (server))))))
