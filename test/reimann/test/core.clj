(ns reimann.test.core
  (:require [reimann.server])
  (:require [reimann.streams])
  (:use [reimann.client])
  (:use [reimann.common])
  (:use [reimann.core])
  (:use reimann.index)
  (:use [clojure.contrib.generic.functor :only (fmap)])
  (:use [clojure.test]))

(defmacro tim
  "Evaluates expr and returns the time it took in seconds"
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     (/ (- (. System (nanoTime)) start#) 1000000000.0)))

(deftest query-test
         (let [core (core)
               index (index)
               server (reimann.server/tcp-server core)
               stream (reimann.streams/update index)
               client (reimann.client/tcp-client)]

           (try
             (dosync
               (alter (core :servers) conj server)
               (alter (core :streams) conj stream)
               (ref-set (core :index) index))

             ; Send events
             (send-event client {:metric_f 1 :time 1})
             (send-event client {:metric_f 2 :time 2})
             (send-event client {:service "miao" :host "cat" :time 3})

             (is (= (set (query client "host = nil or service = \"miao\""))
                    #{(state {:metric_f 2.0 :time 2}) 
                      (state {:service "miao" :host "cat" :time 3})}))

             (finally
               (close-client client)
               (stop core)))))

(deftest expires
         (let [core (core)
               index (index)
               res (ref nil)
               expired-stream (reimann.streams/expired? 
                                (fn [e] (dosync (ref-set res e))))
               stream (reimann.streams/update index)
               reaper (periodically-expire core 0.001)]

               (dosync
                 (ref-set (:index core) index)
                 (alter (:streams core) conj stream)
                 (alter (:streams core) conj expired-stream))

           ; Insert events
           (stream {:service 1 :ttl 0.00 :time (unix-time)})
           (stream {:service 2 :ttl 1 :time (unix-time)})

           ; Wait for reaper to eat them
           (Thread/sleep 5)

           ; Kill reaper
           (future-cancel reaper)
           
           ; Check that index does not contain these states
           (is (= [2] (map (fn [e] (:service e)) (.values index))))

           ; Check that expired-stream received them.
           (is (= (select-keys @res [:service :host :state])
                  {:service 1
                   :host nil
                   :state "expired"}))))

(deftest sum
         (let [core (core)
               done (ref [])
               server (reimann.server/tcp-server core)
               stream (reimann.streams/sum (reimann.streams/append done)) 
               client (reimann.client/tcp-client)]
           (try
             (dosync
               (alter (core :servers) conj server)
               (alter (core :streams) conj stream))

             ; Send some events over the network
             (send-event client {:metric_f 1})
             (send-event client {:metric_f 2})
             (send-event client {:metric_f 3})
             (close-client client)
             
             ; Confirm receipt
             (let [l (deref done)]
               (is (= [1 3 6] 
                      (map (fn [x] (:metric_f x)) l))))

             (finally
               (close-client client)
               (stop core)))))

(deftest percentiles
         (let [core (core)
               out (ref [])
               server (reimann.server/tcp-server core)
               stream (reimann.streams/percentiles 1 [0 0.5 0.95 0.99 1] 
                                                 (reimann.streams/append out))
               client (reimann.client/tcp-client)]
           (try
             (dosync
               (alter (core :servers) conj server)
               (alter (core :streams) conj stream))

             ; Wait until we aren't aligned... ugh, timing
             ;(Thread/sleep (- 1100 (* (mod (unix-time) 1) 1000)))

             ; Send some events over the network
             (doseq [n (shuffle (take 101 (iterate inc 0)))]
               (send-event client {:metric_f n :service "per"}))
             (close-client client)
             
             ; Wait for percentiles
             (Thread/sleep 1000)

             ; Get states
             (let [events (deref out)
                   states (fmap first (group-by :service events))]

               (is (= ((states "per 0.5") :metric_f) 50))
               (is (= ((states "per 0.95") :metric_f) 95))
               (is (= ((states "per 0.99") :metric_f) 99))
               (is (= ((states "per 1") :metric_f) 100)))

             (finally
               (close-client client)
               (stop core)))))
