(ns reimann.test.core
  (:require [reimann.server])
  (:require [reimann.streams])
  (:use [reimann.client])
  (:use [reimann.common])
  (:use [reimann.core])
  (:use reimann.index)
  (:use [clojure.algo.generic.functor :only (fmap)])
  ;(:use [clojure.contrib.generic.functor :only (fmap)])
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
               server (reimann.server/tcp-server core)
               stream (reimann.streams/append out)
               client (reimann.client/tcp-client)
               events [{:host "shiiiiire!"}
                       {:service "baaaaaginnnns!"}
                       {:state "middling"}
                       {:description "well and truly fucked"}
                       {:tags ["oh" "sam"]}
                       {:metric -1000}
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
               server (reimann.server/tcp-server core)
               stream (reimann.streams/update-index index)
               client (reimann.client/tcp-client)]

           (try
             (dosync
               (alter (core :servers) conj server)
               (alter (core :streams) conj stream)
               (ref-set (core :index) index))

             ; Send events
             (send-event client {:metric 1 :time 1})
             (send-event client {:metric 2 :time 3})
             (send-event client {:host "kitten" 
                                 :tags ["whiskers" "paws"] :time 2})
             (send-event client {:service "miao" :host "cat" :time 3})

             (let [r (vec (query client "host = nil or service = \"miao\" or tagged \"whiskers\""))]
               (is (some (fn [e] (= e {:metric_f 2.0, :metric 2.0, :time 3})) r))
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
               expired-stream (reimann.streams/expired 
                                (fn [e] (dosync (ref-set res e))))
               stream (reimann.streams/update-index index)
               reaper (periodically-expire core 0.001)]

               (dosync
                 (ref-set (:index core) index)
                 (alter (:streams core) conj stream)
                 (alter (:streams core) conj expired-stream))

           ; Insert events
           (stream {:service 1 :ttl 0.00 :time (unix-time)})
           (stream {:service 2 :ttl 1 :time (unix-time)})

           ; Wait for reaper to eat them
           (Thread/sleep 10)

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
               server (reimann.server/tcp-server core)
               stream (reimann.streams/sum (reimann.streams/append done)) 
               client (reimann.client/tcp-client)]
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
               server (reimann.server/tcp-server core)
               stream (reimann.streams/percentiles 1 [0 0.5 0.95 0.99 1] 
                                                 (reimann.streams/append out))
               client (reimann.client/tcp-client)]
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
