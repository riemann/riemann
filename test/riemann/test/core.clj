(ns riemann.test.core
  (:require riemann.transport.tcp
            riemann.streams
            [riemann.logging :as logging])
  (:use riemann.client
        riemann.common
        riemann.index
        riemann.time.controlled
        riemann.core
        clojure.test
        [clojure.algo.generic.functor :only [fmap]]
        [riemann.service :only [Service]]
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

(deftest blank-test
         (let [c (core)]
           (is (= [] (:streams c)))
           (is (= [] (:services c)))
           (is (= nil (:index c)))
           (is (:pubsub c))))

(defrecord TestService [id running core]
  Service
  (start!  [_]   (reset! running true))
  (stop!   [_]   (reset! running false))
  (reload! [_ c] (reset! core c))
  (equiv?  [a b] (= (:id a) (:id b))))

(deftest start-transition-stop
         (logging/suppress 
           "riemann.core"
           (let [old-running    (atom nil)
                 old-core       (atom nil)
                 same-1-running (atom nil)
                 same-1-core    (atom nil)
                 same-2-running (atom nil)
                 same-2-core    (atom nil)
                 new-running    (atom nil)
                 new-core       (atom nil)
                 old-service    (TestService. :old  old-running    old-core)
                 same-service-1 (TestService. :same same-1-running same-1-core)
                 same-service-2 (TestService. :same same-2-running same-2-core)
                 new-service    (TestService. :new  new-running    new-core)
                 old {:services [old-service same-service-1]}
                 new {:services [new-service same-service-2]}]

             (start! old)
             (is (= [old-service same-service-1] (:services old)))
             (is        @old-running)
             (is        @same-1-running)
             (is (not   @same-2-running))
             (is (not   @new-running))
             (is (= old @old-core))
             (is (= old @same-1-core))
             (is (= nil @same-2-core))
             (is (= nil @new-core))

             ; Should preserve the same-1 service from the old core
             (let [final (transition! old new)]
               (is (not= new final))
               (is (= [new-service same-service-1] (:services final)))
               (is (not     @old-running))
               (is          @same-1-running)
               (is (not     @same-2-running))
               (is          @new-running)
               (is (= old   @old-core))
               (is (= final @same-1-core))
               (is (= nil   @same-2-core))
               (is (= final @new-core))

               (stop! final)
               (is (= [new-service same-service-1] (:services final)))
               (is (not     @old-running))
               (is (not     @same-1-running))
               (is (not     @same-2-running))
               (is (not     @new-running))
               (is (= old   @old-core))
               (is (= final @same-1-core))
               (is (= nil   @same-2-core))
               (is (= final @new-core))))))

(deftest serialization
         (let [out (ref [])
               server (riemann.transport.tcp/tcp-server)
               stream (riemann.streams/append out)
               core   (logging/suppress ["riemann.transport.tcp"
                                         "riemann.core"]
                                        (transition! (core)
                                                     {:services [server]
                                                      :streams [stream]}))
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
             ; Send events
             (doseq [e events] (send-event client e))

             (doseq [[in out] (map (fn [a b] [a b]) events (deref out))]
               (is (every? (fn [k] (= (k in) (k out))) (keys in))))

             (finally
               (close-client client)
               (logging/suppress ["riemann.core" "riemann.transport.tcp"] 
                                 (stop! core))))))

(deftest query-test
         (let [index  (index)
               server (riemann.transport.tcp/tcp-server)
               core   (logging/suppress ["riemann.core"
                                         "riemann.transport.tcp"]
                                        (transition! (core)
                                                     {:services [server]
                                                      :index index}))
               client (riemann.client/tcp-client)]

           (try
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
                 (stop! core))))))

(deftest expires
         (let [index (index)
               res (atom nil)
               expired-stream (riemann.streams/expired 
                                (fn [e] (reset! res e)))
               reaper (reaper 0.001)
               core (logging/suppress 
                      ["riemann.core" "riemann.transport.tcp"]
                      (transition! (core) {:services [reaper]
                                           :streams [expired-stream]
                                           :index index}))]

           ; Insert events
           (update-index core {:service 1 :ttl 0.01 :time (unix-time)})
           (update-index core {:service 2 :ttl 1 :time (unix-time)})

           (advance! 0.011)

           ; Wait for reaper to eat them
           (Thread/sleep 100)

           ; Kill reaper
           (logging/suppress "riemann.core"
                             (stop! core))
           
           ; Check that index does not contain these states
           (is (= [2] (map (fn [e] (:service e)) index)))

           ; Check that expired-stream received them.
           (is (= @res
                  {:service 1
                   :host nil
                   :time 0.011
                   :state "expired"}))))

(deftest percentiles
         (let [out (ref [])
               server (riemann.transport.tcp/tcp-server)
               stream (riemann.streams/percentiles 1 [0 0.5 0.95 0.99 1] 
                                                 (riemann.streams/append out))
               core   (logging/suppress 
                        ["riemann.core" "riemann.transport.tcp"]
                        (transition! (core) {:services [server]
                                             :streams [stream]}))
               client (riemann.client/tcp-client)]
           (try
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
               (logging/suppress ["riemann.transport.tcp" "riemann.core"]
                                 (stop! core))))))
