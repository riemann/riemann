(ns riemann.test.transport
  (:use riemann.common
        riemann.core
        riemann.transport.tcp
        riemann.transport.udp
        riemann.logging
        clojure.test
        lamina.core
        aleph.tcp
        aleph.udp
        gloss.core)
  (:import (org.jboss.netty.buffer ChannelBuffers)))

(riemann.logging/init)

(deftest udp
         (riemann.logging/suppress ["riemann.transport" "riemann.core"]
           (let [server (udp-server)
                 core   (transition! (core) {:services [server]})
                 client (wait-for-result (udp-socket {}))
                 msg (ChannelBuffers/wrappedBuffer
                       (encode {:ok true}))]

             (try
               (enqueue client {:host "localhost" 
                                :port 5555 
                                :message msg})
               (Thread/sleep 100)
               (finally
                 (close client)
                 (stop! core))))))

(deftest ignores-garbage
         (riemann.logging/suppress ["riemann.transport" "riemann.core"]
            (let [server (tcp-server)
                  core   (transition! (core) {:services [server]})
                  client (wait-for-result 
                           (aleph.tcp/tcp-client 
                             {:host "localhost" 
                              :port 5555
                              :frame (finite-block :int32)}))]

              (try
                (enqueue client 
                         (java.nio.ByteBuffer/wrap 
                           (byte-array (map byte [0 1 2]))))
                (is nil? (wait-for-message client))
                (is (closed? client))
                (finally
                  (close client)
                  (stop! core))))))
