(ns riemann.client-test
  (:require [riemann.client :refer :all]
            [riemann.common :refer :all]
            [riemann.core :refer :all]
            [riemann.index :refer [index]]
            [riemann.logging :refer [suppress]]
            [riemann.transport.tcp :refer :all]
            [clojure.test :refer :all]))

(riemann.logging/init)

(deftest reconnect
  (suppress ["riemann.transport.tcp" "riemann.core" "riemann.pubsub"]
    (let [server (tcp-server)
          core   (transition! (core) {:services [server]})
          client (tcp-client)]
      (.. client transport reconnectDelay (set 0))
      (try
        ; Initial connection works
        (is @(send-event client {:service "test"}))

        ; Kill server; should fail.
        (stop! core)
        (is (thrown? java.io.IOException
                     @(send-event client {:service "test"})))

        ; Restart server; should work
        (start! core)
        (Thread/sleep 200)

        (try
          @(send-event client {:service "test"})
          (finally
            (stop! core)))

        (finally
          (close! client)
          (stop! core))))))

; Check that server error messages are correctly thrown.
(deftest server-errors
  (suppress ["riemann.transport.tcp" "riemann.core" "riemann.pubsub"]
    (let [index (index)
          server (tcp-server)
          core   (transition! (core) {:services [server]
                                      :index index})
          client (tcp-client)]

      (try
        (is (thrown-with-msg?
              io.riemann.riemann.client.ServerError
              #"^mismatched input 'no' expecting \{<EOF>, 'and', 'or'\}$"
              @(query client "oh no not again")))
        (finally
          (close! client)
          (stop! core))))))
