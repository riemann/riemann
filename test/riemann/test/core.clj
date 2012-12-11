(ns riemann.test.core
  (:require riemann.transport.tcp
            riemann.streams
            [riemann.logging :as logging])
  (:use riemann.client
        riemann.common
        riemann.core
        riemann.index
        clojure.test
        riemann.time
        riemann.time.controlled
        [clojure.algo.generic.functor :only [fmap]]
        [riemann.time :only [unix-time]]))

(logging/init)
(use-fixtures :each reset-time!)
(use-fixtures :once control-time!)

(defmacro tim
  "Evaluates expr and returns the time it took in seconds"
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     (/ (- (. System (nanoTime)) start#) 1000000000.0)))

(deftest serialization
         (let [core (core)
               out (ref [])
               server (logging/suppress "riemann.transport.tcp"
                                        (riemann.transport.tcp/tcp-server core))
               stream (riemann.streams/append out)
               client (riemann.client/tcp-client)
               events [{:host "shiiiiire!"}
                       {:service "baaaaaginnnns!"}
                       {:state "middling"}
                       {:description "quite dire, really"}
                       {:tags ["oh" "sam"]}
                       {:metric -1000.0}
                       {:metric Double/MAX_VALUE}
                       {:metric Long/MIN_VALUE}
                       {:time 1234}
                       {:ttl 12.0}]]
           
           (try
             (swap! (core :servers) conj server)
             (swap! (core :streams) conj stream)

             ; Send events
             (doseq [e events] (send-event client e))

             (doseq [[in out] (map (fn [a b] [a b]) events (deref out))]
               (is (every? (fn [k] (= (k in) (k out))) (keys in))))

             (finally
               (close-client client)
               (logging/suppress ["riemann.core" "riemann.transport.tcp"] 
                                 (stop core))))))

(deftest query-test
         (let [core (core)
               index (index)
               server (logging/suppress "riemann.transport.tcp"
                                        (riemann.transport.tcp/tcp-server core))
               client (riemann.client/tcp-client)]

           (try
             (swap! (core :servers) conj server)
             (reset! (core :index) index)

             ; Send events
             (update-index core {:metric 1 :time 1})
             (update-index core {:metric 2 :time 3})
             (update-index core {:host "kitten" 
                                 :tags ["whiskers" "paws"] :time 2})
             (update-index core {:service "miao" :host "cat" :time 3})

             (let [r (set (query client "metric = 2 or service = \"miao\" or tagged \"whiskers\""))]
               (is (= r
                      #{(event {:metric 2, :time 3})
                        (event {:host "kitten" :tags ["whiskers" "paws"] :time 2})
                        (event {:host "cat", :service "miao", :time 3})} r)))

             (finally
               (close-client client)
               (logging/suppress ["riemann.core" "riemann.transport.tcp"]
                 (stop core))))))

(deftest expires
         (let [core (core)
               index (index)
               res (ref nil)
               expired-stream (riemann.streams/expired 
                                (fn [e] (dosync (ref-set res e))))
               reaper (periodically-expire core 0.001)]

                (reset! (:index core) index)
                (swap! (:streams core) conj expired-stream)

           ; Insert events
           (update-index core {:service 1 :ttl 0.01 :time (unix-time)})
           (update-index core {:service 2 :ttl 1 :time (unix-time)})

           (advance! 0.011)

           ; Wait for reaper to eat them
           (Thread/sleep 30)

           ; Kill reaper
           (future-cancel reaper)
           
           ; Check that index does not contain these states
           (is (= [2] (map (fn [e] (:service e)) index)))

           ; Check that expired-stream received them.
           (is (= @res
                  {:service 1
                   :host nil
                   :time 0.011
                   :state "expired"}))))

(comment
  (deftest sum
         (let [core (core)
               done (ref [])
               server (riemann.transport.tcp/tcp-server core)
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
               server (logging/suppress "riemann.transport.tcp"
                        (riemann.transport.tcp/tcp-server core))
               stream (riemann.streams/percentiles 1 [0 0.5 0.95 0.99 1] 
                                                 (riemann.streams/append out))
               client (riemann.client/tcp-client)]
           (try
             (swap! (core :servers) conj server)
             (swap! (core :streams) conj stream)

             ; Send some events over the network
             (doseq [n (shuffle (take 101 (iterate inc 0)))]
               (send-event client {:metric n :service "per"}))
             (close-client client)
             
             ; Wait for percentiles
             (advance! 1)

             ; Get states
             (let [events (deref out)
                   states (fmap first (group-by :service events))]

               (is (= (:metric (states "per 0.5")) 50))
               (is (= (:metric (states "per 0.95")) 95))
               (is (= (:metric (states "per 0.99")) 99))
               (is (= (:metric (states "per 1")) 100)))

             (finally
               (close-client client)
               (logging/suppress ["riemann.server" "riemann.core"]
                                 (stop core))))))
