(ns riemann.test.core
  (:require [riemann.server])
  (:require [riemann.streams])
  (:use [riemann.client])
  (:use [riemann.common])
  (:use [riemann.core])
  (:use riemann.index)
  (:use [clojure.algo.generic.functor :only (fmap)])
  (:use [clojure.test]))

(defmacro tim
  "Evaluates expr and returns the time it took in seconds"
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     (/ (- (. System (nanoTime)) start#) 1000000000.0)))

(deftest serialization
         (let [core (core)
               out (ref [])
               server (riemann.server/tcp-server core)
               stream (riemann.streams/append out)
               client (riemann.client/tcp-client)
               events [{:host "shiiiiire!"}
                       {:service "baaaaaginnnns!"}
                       {:state "middling"}
                       {:description "well and truly fucked"}
                       {:tags ["oh" "sam"]}
                       {:metric -1000.0}
                       {:time 1234}
                       {:ttl 12.0}]]
           
           (try
             (dosync
               (alter (core :servers) conj server)
               (alter (core :streams) conj stream))

             ; Send events
             (doseq [e events] (send-event client e))

             (doseq [[in out] (map (fn [a b] [a b]) events (deref out))]
               (is (every? (fn [k] (= (in k) (out k))) (keys in))))

             (finally
               (close-client client)
               (stop core)))))

(deftest query-test
         (let [core (core)
               index (index)
               server (riemann.server/tcp-server core)
               client (riemann.client/tcp-client)]

           (try
             (dosync
               (alter (core :servers) conj server)
               (ref-set (core :index) index))

             ; Send events
             (update-index core {:metric 1 :time 1})
             (update-index core {:metric 2 :time 3})
             (update-index core {:host "kitten" 
                                 :tags ["whiskers" "paws"] :time 2})
             (update-index core {:service "miao" :host "cat" :time 3})

             (let [r (vec (query client "host = nil or service = \"miao\" or tagged \"whiskers\""))]
               (is (some (fn [e] (= e {:metric 2.0, :time 3})) r))
               (is (some (fn [e] (= e {:host "kitten" :tags ["whiskers" "paws"] :time 2})) r))
               (is (some (fn [e] (= e {:host "cat", :service "miao", :time 3})) r))
               (is (= 3 (count r))))

             (finally
               (close-client client)
               (stop core)))))

(deftest expires
         (let [core (core)
               index (index)
               res (ref nil)
               expired-stream (riemann.streams/expired 
                                (fn [e] (dosync (ref-set res e))))
               reaper (periodically-expire core 0.001)]

               (dosync
                 (ref-set (:index core) index)
                 (alter (:streams core) conj expired-stream))

           ; Insert events
           (update-index core {:service 1 :ttl 0.00 :time (unix-time)})
           (update-index core {:service 2 :ttl 1 :time (unix-time)})

           ; Wait for reaper to eat them
           (Thread/sleep 30)

           ; Kill reaper
           (future-cancel reaper)
           
           ; Check that index does not contain these states
           (is (= [2] (map (fn [e] (:service e)) (.values index))))

           ; Check that expired-stream received them.
           (is (= (select-keys @res [:service :host :state])
                  {:service 1
                   :host nil
                   :state "expired"}))))

(comment
  (deftest sum
         (let [core (core)
               done (ref [])
               server (riemann.server/tcp-server core)
               stream (riemann.streams/sum (riemann.streams/append done)) 
               client (riemann.client/tcp-client)]
           (try
             (dosync
               (alter (core :servers) conj server)
               (alter (core :streams) conj stream))

             ; Send some events over the network
             (send-event client {:metric 1})
             (send-event client {:metric 2})
             (send-event client {:metric 3})
             (close-client client)
             
             ; Confirm receipt
             (let [l (deref done)]
               (is (= [1 3 6] 
                      (map (fn [x] (:metric x)) l))))

             (finally
               (close-client client)
               (stop core))))))

(deftest percentiles
         (let [core (core)
               out (ref [])
               server (riemann.server/tcp-server core)
               stream (riemann.streams/percentiles 1 [0 0.5 0.95 0.99 1] 
                                                 (riemann.streams/append out))
               client (riemann.client/tcp-client)]
           (try
             (dosync
               (alter (core :servers) conj server)
               ; (alter (core :streams) conj prn)
               (alter (core :streams) conj stream))

             ; Wait until we aren't aligned... ugh, timing
             ;(Thread/sleep (- 1100 (* (mod (unix-time) 1) 1000)))

             ; Send some events over the network
             (doseq [n (shuffle (take 101 (iterate inc 0)))]
               (send-event client {:metric n :service "per"}))
             (close-client client)
             
             ; Wait for percentiles
             (Thread/sleep 1000)

             ; Get states
             (let [events (deref out)
                   states (fmap first (group-by :service events))]

               (is (= ((states "per 0.5") :metric) 50.0))
               (is (= ((states "per 0.95") :metric) 95.0))
               (is (= ((states "per 0.99") :metric) 99.0))
               (is (= ((states "per 1") :metric) 100.0)))

             (finally
               (close-client client)
               (stop core)))))
