(ns riemann.test.client
  (:use [riemann.common])
  (:use [riemann.core])
  (:use [riemann.server])
  (:use [riemann.client])
  (:use [clojure.test]))

(deftest reconnect
  (let [server (tcp-server (core))
        client (tcp-client)]
    (try
      ; Initial connection works
      (is (send-event client {:service "test"}))

      ; Kill server; should fail.
      (server)
      (is (thrown? java.net.SocketException (send-event client {:service "test"})))
      
      (let [server (tcp-server (core))]
        (try
          (send-event client {:service "test"})
          (finally
            (server))))

      (finally
        (close-client client)
        (server)))))
