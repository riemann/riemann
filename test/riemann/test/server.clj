(ns riemann.test.server
  (:use [riemann.common])
  (:use [riemann.core])
  (:use [riemann.server])
  (:use [riemann.logging])
  (:use [clojure.test])
  (:use [lamina.core])
  (:use [aleph.tcp])
  (:use [aleph.udp])
  (:use [gloss.core])
  (:import (org.jboss.netty.buffer ChannelBuffers))
  )

;(riemann.logging/init)

(deftest udp
         (let [server (udp-server (core))
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
               (server)))))

(deftest ignores-garbage
  (let [server (tcp-server (core))
        client (wait-for-result 
                 (aleph.tcp/tcp-client {:host "localhost" 
                                        :port 5555
                                        :frame (finite-block :int32)}))]
    
    (try
      (enqueue client 
               (java.nio.ByteBuffer/wrap (byte-array (map byte [0 1 2]))))
      (is nil? (wait-for-message client))
      (is (closed? client))
      (finally
        (close client)
        (server)))))
