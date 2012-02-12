(ns reimann.test.client
  (:use [reimann.common])
  (:use [reimann.core])
  (:use [reimann.server])
  (:use [reimann.client])
  (:use [clojure.test]))

(deftest reconnect
  (let [server (tcp-server (core))
        client (tcp-client)]
    (try
      ; Initial connection works
      (is (send-event client {:service "test"}))

      ; Kill server; should fail.
      (server)
      (is false? (send-event client {:service "test"}))

      
      (let [server (tcp-server (core))]
        (try
          (send-event client {:service "test"})
          (finally
            (server))))

      (finally
        (close-client client)
        (server)))))
