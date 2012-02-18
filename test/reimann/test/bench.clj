(ns reimann.test.bench
  (:use [reimann.core])
  (:use [reimann.common])
  (:use [reimann.server])
  (:use [reimann.client :only [tcp-client close-client send-event]])
  (:use [reimann.streams])
  (:use [clojure.test]))

(comment
(deftest sum-test
         (let [final (ref nil)
               core (core)
               server (tcp-server core)
               stream (sum (register final))
               n 100
               threads 10
               events (take n (repeatedly (fn [] 
                        {:metric 1})))]

           (dosync
             (alter (core :servers) conj server)
             (alter (core :streams) conj stream))

           (doall events)

           (try 
             (time (threaded threads
                             (let [client (tcp-client)]
                                (doseq [e events]
                                  ; Send all events to server
                                  (send-event client e))
                               (close-client client))))
             
            (is (= (* threads n) (:metric (deref final)))) 

            (finally
              (stop core))))))
