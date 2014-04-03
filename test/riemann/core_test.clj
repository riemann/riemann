(ns riemann.core-test
  (:require riemann.transport.tcp
            riemann.streams
            [riemann.logging :as logging])
  (:use riemann.client
        riemann.common
        [riemann.index :only [index]]
        riemann.time.controlled
        riemann.core
        clojure.test
        [clojure.algo.generic.functor :only [fmap]]
        [riemann.service :only [Service ServiceEquiv]]
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
           (is (= (map :name (:services c))
                  [:riemann.core/instrumentation]))
           (is (= nil (:index c)))
           (is (:pubsub c))))

(defrecord TestService [id running core]
  Service
  (conflict? [a b] (and (instance? TestService b)
                        (= id (:id b))))
  (start!  [_]   (reset! running true))
  (stop!   [_]   (reset! running false))
  (reload! [_ c] (reset! core c))
  ServiceEquiv
  (equiv?  [a b] (= (:id a) (:id b))))

(deftest conj-service-test
         ; Verify that we can't associate two services which conflict
         (let [ts1  (TestService. 1 (atom nil) (atom nil))
               ts1' (TestService. 1 (atom nil) (atom nil))
               ts2  (TestService. 2 (atom nil) (atom nil))
               c (-> (core)
                   (assoc :services [])
                   (conj-service ts1))]
           (is (thrown? IllegalArgumentException
                        (conj-service c ts1')))

           ; But we can force it!
           (is (= (:services (conj-service c ts1' :force))
                  [ts1']))

           ; It should be fine to associate something that doesn't conflict:
           (is (= (:services (conj-service c ts2))
                  [ts1 ts2]))))

(deftest start-transition-stop
         (logging/suppress
           ["riemann.core"
            "riemann.pubsub"]
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

(deftest transition-index
         (logging/suppress
           ["riemann.core" "riemann.pubsub"]
           (testing "Different indexes"
                    (let [old-running (atom nil)
                          old-core    (atom nil)
                          new-running (atom nil)
                          new-core    (atom nil)
                          old-index (TestService. :old old-running old-core)
                          new-index (TestService. :new new-running new-core)
                          old {:index old-index}
                          new {:index new-index}]

                      (start! old)
                      (is @old-running)
                      (is (not @new-running))
                      (is (= old @old-core))
                      (is (= nil @new-core))

                      (let [final (transition! old new)]
                        (is (not= new final))
                        (is (= new-index (:index final)))
                        (is (not     @old-running))
                        (is          @new-running)
                        (is (= final @new-core))
                        (is (= old   @old-core))

                        (stop! final)
                        (is (= new-index (:index final)))
                        (is (= old-index (:index old)))
                        (is (not @old-running))
                        (is (not @new-running))
                        (is (= final @new-core))
                        (is (= old   @old-core)))))

           (testing "The same index"
                    (let [old-running (atom nil)
                          old-core    (atom nil)
                          new-running (atom nil)
                          new-core    (atom nil)
                          old-index (TestService. :same old-running old-core)
                          new-index (TestService. :same new-running new-core)
                          old {:index old-index}
                          new {:index new-index}]

                      (start! old)
                      (is @old-running)
                      (is (not @new-running))
                      (is (= old @old-core))
                      (is (= nil @new-core))

                      (let [final (transition! old new)]
                        (is (not= new final))
                        (is (= old-index (:index final)))
                        (is          @old-running)
                        (is (not     @new-running))
                        (is (= final @old-core))
                        (is (nil?    @new-core))

                        (stop! final)
                        (is (= old-index (:index final)))
                        (is (not @old-running))
                        (is (not @new-running))
                        (is (= nil   @new-core))
                        (is (= final @old-core)))))))

(deftest serialization
         (let [out (atom [])
               server (riemann.transport.tcp/tcp-server)
               stream (riemann.streams/append out)
               core   (logging/suppress ["riemann.transport.tcp"
                                         "riemann.core"
                                         "riemann.pubsub"]
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
               (logging/suppress ["riemann.core"
                                  "riemann.transport.tcp"
                                  "riemann.pubsub"]
                                 (stop! core))))))

(deftest query-test
         (let [index  (wrap-index (index))
               server (riemann.transport.tcp/tcp-server)
               core   (logging/suppress ["riemann.core"
                                         "riemann.transport.tcp"
                                         "riemann.pubsub"]
                                        (transition! (core)
                                                     {:services [server]
                                                      :index index}))
               client (riemann.client/tcp-client)]

           (try
             ; Send events
             (index {:metric 1 :time 1})
             (index {:metric 2 :time 3})
             (index {:host "kitten"
                     :tags ["whiskers" "paws"] :time 2})
             (index {:service "miao" :host "cat" :time 3})

             (let [r (->> "metric = 2 or service = \"miao\" or tagged \"whiskers\""
                       (query client)
                       set)]
               (is (= r
                      #{(event {:metric 2, :time 3})
                        (event {:host "kitten" :tags ["whiskers" "paws"] :time 2})
                        (event {:host "cat", :service "miao", :time 3})})))

             (finally
               (close-client client)
               (logging/suppress ["riemann.core"
                                  "riemann.transport.tcp"
                                  "riemann.pubsub"]
                 (stop! core))))))

(deftest expires
         (let [index (wrap-index (index))
               res (atom nil)
               expired-stream (riemann.streams/expired
                                (fn [e] (reset! res e)))
               reaper (reaper 0.001)
               core (logging/suppress
                      ["riemann.core"
                       "riemann.transport.tcp"
                       "riemann.pubsub"]
                      (transition! (core) {:services [reaper]
                                           :streams [expired-stream]
                                           :index index}))]

           ; Insert events
           (index {:service 1 :ttl 0.01 :time (unix-time)})
           (index {:service 2 :ttl 1 :time (unix-time)})

           (advance! 0.011)

           ; Wait for reaper to eat them
           (Thread/sleep 100)

           ; Kill reaper
           (logging/suppress ["riemann.core" "riemann.pubsub"]
                             (stop! core))

           ; Check that index does not contain these states
           (is (= [2] (map (fn [e] (:service e)) index)))

           ; Check that expired-stream received them.
           (is (= @res
                  {:service 1
                   :time 0.011
                   :state "expired"}))))

(deftest reaper-keep-keys
         (let [index (wrap-index (index))
               res (atom nil)
               expired-stream (riemann.streams/expired
                                (partial reset! res))
               reaper (reaper 0.001 {:keep-keys [:tags]})
               core (logging/suppress
                      ["riemann.core"
                       "riemann.transport.tcp"
                       "riemann.pubsub"]
                      (transition! (core) {:services [reaper]
                                           :streams [expired-stream]
                                           :index index}))]

           (index {:service 1 :ttl 0.01 :time 0 :tags ["hi"]})
           (advance! 2)
           (Thread/sleep 100)
           (is (= @res {:tags ["hi"]
                        :time 2
                        :state "expired"}))))

(deftest ensures-event-times
  (let [out (promise)
        server (riemann.transport.tcp/tcp-server)
        core   (logging/suppress
                 ["riemann.core"
                  "riemann.transport.tcp"
                  "riemann.pubsub"]
                 (transition! (core) {:services [server]
                                      :streams  [(partial deliver out)]}))
        client (riemann.client/tcp-client)
        t1     (/ (System/currentTimeMillis) 1000)]
    (try
      (send-event client {:service "hi" :time nil})
      (let [event (deref out 1000 :timeout)
            t2    (/ (System/currentTimeMillis) 1000)]
        (is (= "hi" (:service event)))
        (is (<= t1 (:time event) t2)))
      (finally
        (close-client client)
        (logging/suppress ["riemann.transport.tcp"
                           "riemann.core"
                           "riemann.pubsub"]
                          (stop! core))))))
(deftest percentiles
         (let [out (atom [])
               server (riemann.transport.tcp/tcp-server)
               stream (riemann.streams/percentiles 1 [0 0.5 0.95 0.99 1]
                                                 (riemann.streams/append out))
               core   (logging/suppress
                        ["riemann.core"
                         "riemann.transport.tcp"
                         "riemann.pubsub"]
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
               (logging/suppress ["riemann.transport.tcp"
                                  "riemann.core"
                                  "riemann.pubsub"]
                                 (stop! core))))))
