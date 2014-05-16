(ns riemann.streams-test
  (:use riemann.streams
        [riemann.common :exclude [match]]
        riemann.time.controlled
        riemann.time
        clojure.test)
  (:require [riemann.index :as index]
            [riemann.folds :as folds]
            [riemann.logging :as logging])
  (:import (java.util.concurrent Executor
                                 CountDownLatch)))

(logging/init)

(use-fixtures :once control-time!)
(use-fixtures :each reset-time!)

(defmacro run-stream
  "Applies inputs to stream, and returns outputs."
  [stream inputs]
  `(let [out# (atom [])
         stream# (~@stream (append out#))]
     (doseq [e# ~inputs] (stream# e#))
     (deref out#)))

(defmacro run-stream-intervals
  "Applies a seq of alternating events and intervals (in seconds) between them
  to stream, returning outputs."
  [stream inputs-and-intervals]
  `(let [out# (atom [])
         stream# (~@stream (append out#))
         start-time# (ref (unix-time))
         next-time# (ref (deref start-time#))]
     (doseq [[e# interval#] (partition-all 2 ~inputs-and-intervals)]
       (stream# e#)
       (when interval#
         (dosync (ref-set next-time# (+ (deref next-time#) interval#)))
         (advance! (deref next-time#))))
     (let [result# (deref out#)]
       ; close stream
       (stream# {:state "expired" :time (unix-time)})
       result#)))

(defmacro test-stream
  "Verifies that the given stream, taking inputs, forwards outputs to children."
  [stream inputs outputs]
  `(is (~'= ~outputs (run-stream ~stream ~inputs))))

(defmacro with-test-stream
  "Exposes a fake index, verifies that the given stream, taking inputs,
  forwards outputs to children"
  [sym stream inputs outputs]
  `(let [out#    (atom [])
         ~sym    (append out#)
         stream# ~stream]
     (doseq [e# ~inputs] (stream# e#))
     (is (~'= (deref out#) ~outputs))))

(defmacro test-stream-intervals
  "Verifies that run-stream-intervals, taking inputs/intervals, forwards
  outputs to chldren."
  [stream inputs-and-intervals outputs]
  `(is (~'= (run-stream-intervals ~stream ~inputs-and-intervals) ~outputs)))

(defn evs
  "Generate events based on the given event, with given metrics"
  [base-event & metrics]
  (vec (map #(assoc base-event :metric %)
            metrics)))

(defn em
  "Generate events with given metrics"
  [& metrics]
  (vec (map (fn [m] {:metric m}) metrics)))

(deftest combine-test
  (logging/suppress
    ["riemann.streams"]
    (let [r (atom nil)
          sum (combine folds/sum (register r))
          min (combine folds/minimum (register r))
          max (combine folds/maximum (register r))
          mean (combine folds/mean (register r))
          median (combine folds/median (register r))
          events [{:metric 1}
                  {:metric 0}
                  {:metric -2}]]
      (sum events)
      (is (= (deref r) {:metric -1}))
      (min events)
      (is (= (deref r) {:metric -2}))
      (max events)
      (is (= (deref r) {:metric 1}))
      (mean events)
      (is (= (deref r) {:metric -1/3}))
      (median events)
      (is (= (deref r) {:metric 0})))))

(deftest smap*-test
         (testing "passes nil values"
                  (test-stream (smap* identity)
                               [1 nil false 3]
                               [1 nil false 3])))

(deftest smap-test
         (testing "increment"
                  (test-stream (smap inc) [6 3 -1] [7 4 0]))

         (testing "ignores nil values"
                  (test-stream (smap identity)
                               [1 nil false 3]
                               [1 false 3])))

(deftest smapcat-test
  (testing "doubles"
    (test-stream (smapcat #(vector % %)) [0 1 2 3]
                 [0 0 1 1 2 2 3 3])))

(deftest sdo-test
  (let [vals1   (atom [])
        vals2   (atom [])
        add1    #(swap! vals1 conj %)
        add2    #(swap! vals2 conj %)]
    (run-stream (sdo add1 add2) [1 2 3])
    (is (= @vals1 [1 2 3]))
    (is (= @vals2 [1 2 3]))))

(deftest exception-stream-test
         (testing "direct fn"
                  (logging/suppress 
                    ["riemann.streams" "riemann.streams-test"]

                    (let [out        (atom [])
                          exceptions (atom [])
                          stream (exception-stream
                                   #(swap! exceptions conj %)
                                   (fn [e]
                                     (swap! out conj e)
                                     (throw 
                                       (RuntimeException. "foo"))))]
                      (stream :hi)

                      (is (= @out [:hi]))
                      (is (= 1 (count @exceptions)))
                      (let [e (first @exceptions)]
                        (is (= (:service e) "riemann exception"))
                        (is (= (:state e)   "error"))
                        (is (= (:time e)    0))
                        (is (re-find #".*RuntimeException.*" (:description e)))
                        (is (= #{"exception" "java.lang.RuntimeException"} 
                               (set (:tags e))))))))

         (testing "scheduled fn"
                  (logging/suppress 
                    "riemann.streams"
                    (reset-time!)
                    (let [e     (atom nil)
                          s (exception-stream #(reset! e %)
                                              (rate 1
                                                    (fn [e] (/ 1 0))))]
                      (s {:metric 2})
                      (is (nil? @e))
                      (advance! 1)
                      (is (= "error" (:state @e))))))

         (testing "exception handling takes care of Throwables"
                  (logging/suppress
                   "riemann.streams"
                   (let [exceptions (atom [])]
                     (binding [riemann.streams/*exception-stream* #(swap! exceptions conj %)]
                       (run-stream (sdo #(throw (Throwable. %)))
                                   ["boo!"]))
                     (is (= 1 (count @exceptions)))))))

(deftest execute-on-test
         (let [output (atom [])
               n      100
               events (range n)
               latch  (CountDownLatch. n)
               stream (execute-on
                        (reify Executor
                          (execute [this task]
                                   (future
                                     (task)
                                     (.countDown latch))))
                        (fn [event]
                          (swap! output conj event)))]

           ; Run streams against events.
           (dorun (map stream events))

           ; Check that our event was applied to the conj stream.
           (.await latch)
           (is (= (set @output) (set events)))))

(deftest sreduce-test
         (testing "explicit value"
                  (test-stream (sreduce + 1) [1 2 3] [2 4 7]))

         (testing "implicit value"
                  (test-stream (sreduce +) [1 2 3 4] [3 6 10])))

(deftest counter-test
         (testing "passes through events without metrics"
                  (test-stream (counter)
                               [{} {:state "expired"} {:service "foo"}]
                               [{} {:state "expired"} {:service "foo"}]))

         (testing "counts"
                  (test-stream (counter)
                               [{:metric 2} {} {:metric 3}]
                               [{:metric 2} {} {:metric 5}])

                  (test-stream (counter 100)
                               [{:metric 2} {} {:metric 3}]
                               [{:metric 102} {} {:metric 105}]))

         (testing "resets"
                  (test-stream (counter 100)
                               [{:metric 1} 
                                {:metric 200 :tags ["reset"]}
                                {:metric 5}]
                               [{:metric 101} 
                                {:metric 200 :tags ["reset"]}
                                {:metric 205}])))

(deftest match-test
         ; Regular strings.
         (test-stream (match :service "foo")
                      [{}
                       {:service "bar"}
                       {:service "foo"}]
                      [{:service "foo"}])

         ; Sets
         (test-stream (match :metric #{0 2})
                      [{}
                       {:metric 1}
                       {:metric 2}]
                      [{:metric 2}])

         ; Regexen
         (test-stream (match :state #"^mi")
                      [{}
                       {:state "migas"}
                       {:state "other breakfast foods"}]
                      [{:state "migas"}])

         ; Functions
         (test-stream (match identity 2)
                      [1 2 3]
                      [2])
         
         ; Maps
         (test-stream (match identity {:state #"^mi", :host "other"})
                      [{}
                       {:state "migas"}
                       {:state "migas", :host "other"}]
                      [{:state "migas", :host "other"}])

  ; Nils
  (test-stream (match :host nil)
               [{}
                {:host nil}
                {:host :foo}]
               [{}
                {:host nil}]))

(deftest tag-test
         ; Single tag
         (test-stream (tag "foo")
                      [{}
                       {:service :a :tags ["foo"]}
                       {:service :b :tags ["bar" "baz"]}]
                      [{:tags ["foo"]}
                       {:service :a :tags ["foo"]}
                       {:service :b :tags ["foo" "bar" "baz"]}])

         ; Multiple tags
         (test-stream (tag ["foo" "bar"])
                      [{}
                       {:service :a :tags ["foo"]}
                       {:service :b :tags ["foo" "baz"]}]
                      [{:tags ["foo" "bar"]}
                       {:service :a :tags ["foo" "bar"]}
                       {:service :b :tags ["foo" "bar" "baz"]}]))

(deftest tagged-all-test
         (test-stream (tagged-all ["kitten" "cat"])
                      [{:tags ["kitten" "cat"]}
                       {:tags ["kitten", "cat", "meow"]}
                       {:tags ["dog" "cat"]}
                       {:tags ["cat"]}
                       {:tags []}
                       {}]
                      [{:tags ["kitten" "cat"]}
                       {:tags ["kitten", "cat", "meow"]}])

         (test-stream (tagged-all "meow")
                      [{:tags ["meow" "bark"]}
                       {:tags ["meow"]}
                       {:tags ["bark"]}
                       {}]
                      [{:tags ["meow" "bark"]}
                       {:tags ["meow"]}])
         
         (testing "return values"
                  (is (true? ((tagged-all ["meow" "bark"])
                                {:tags ["meow" "bark" "grr"]})))
                  (is (nil? ((tagged-all ["meow" "bar"])
                               {:tags ["meow"]})))))

(deftest tagged-any-test
         (test-stream (tagged-any ["kitten" "cat"])
                      [{:tags ["kitten" "cat"]}
                       {:tags ["cat", "dog"]}
                       {:tags ["kitten"]}
                       {:tags ["dog"]}
                       {:tags []}
                       {}]
                      [{:tags ["kitten" "cat"]}
                       {:tags ["cat", "dog"]}
                       {:tags ["kitten"]}])

         (test-stream (tagged-any "meow")
                      [{:tags ["meow" "bark"]}
                       {:tags ["meow"]}
                       {:tags ["bark"]}
                       {}]
                      [{:tags ["meow" "bark"]}
                       {:tags ["meow"]}])

         (testing "return values"
                  (is (true? ((tagged-any ["meow" "bark"])
                                {:tags ["meow" "moo"]})))
                  (is (nil? ((tagged-any ["meow" "bar"])
                               {:tags ["moo"]})))))

(deftest split*-test
         (testing "with one branch"
                  (test-stream (split* identity)
                               [true false nil 2]
                               [true 2]))

         (testing "with a default stream"
                  ;; dispatch with default value
                  (let [sup    (fn [threshold] #(< threshold (:metric %)))
                        res    (atom [])
                        events [{:metric 15} {:metric 8} {:metric 2}]
                        expect [{:metric 15 :state :critical}
                                {:metric 8 :state :warn}
                                {:metric 2 :state :ok}]
                        stream (split* (sup 10) (with :state :critical
                                                      (partial swap! res conj))
                                       (sup 5) (with :state :warn 
                                                     (partial swap! res conj))
                                       (with :state :ok
                                             (partial swap! res conj)))]
                    (dorun (map stream events))
                    (is (= expect @res))))

         (testing "with no default stream"
                  (let [sup    (fn [threshold] #(< threshold (:metric %)))
                        res    (atom [])
                        events [{:metric 15} {:metric 8} {:metric 2}]
                        expect [{:metric 15 :state :critical}
                                {:metric 8 :state :warn}]
                        stream (split* 
                                 (sup 10) (with :state :critical
                                                (partial swap! res conj))
                                 (sup 5) (with :state :warn
                                               (partial swap! res conj)))]
                    (dorun (map stream events))
                    (is (= expect @res)))))

(deftest split-test
         ;; same test as above, using implicit rewrites
         (testing "with a default stream"
                  (let [sup    (fn [threshold] #(< threshold (:metric %)))
                        res    (atom [])
                        events [{:metric 15} {:metric 8} {:metric 2}]
                        expect [{:metric 15 :state :critical}
                                {:metric 8 :state :warn}
                                {:metric 2 :state :ok}]
                        stream (split
                                 (> metric 10) (with :state :critical
                                                     (partial swap! res conj))
                                 (> metric 5) (with :state :warn
                                                    (partial swap! res conj))
                                 (with :state :ok
                                       (partial swap! res conj)))]
                    (dorun (map stream events))
                    (is (= expect @res))))
         
         (testing "evaluates streams once"
                  (let [res (atom [])
                        stream (split 
                                 true
                                 (let [state (atom 0)]
                                   (fn [_]
                                     (swap! res conj
                                            (swap! state inc)))))]
                    (stream :x)
                    (stream :x)
                    (stream :x)
                    (is (= @res [1 2 3])))))

(deftest splitp-test
         ;; same test as above, using splitp
         (testing "basics"
                  (let [res    (atom [])
                        events [{:metric 15} {:metric 8} {:metric 2}]
                        expect [{:metric 15 :state :crit}
                                {:metric 8 :state :warn}
                                {:metric 2 :state :ok}]
                        stream (splitp <= metric
                                       10 (with :state :crit 
                                                (partial swap! res conj))
                                       5  (with :state :warn 
                                                (partial swap! res conj))
                                       (with :state :ok 
                                             (partial swap! res conj)))]
                    (dorun (map stream events))
                    (is (= expect @res))))

         (testing "Without a default"
                  (is (thrown? IllegalArgumentException
                               ((splitp = true
                                        false :doesnt-happen)
                                  :foo))))
         
         (testing "Evaluates child streams once at creation time"
                  (let [a (atom 0)
                        b (atom 0)
                        s (splitp = state
                                  :foo (do (swap! a inc) identity)
                                       (do (swap! b inc) identity))]
                    (is (= 1 @a))
                    (is (= 1 @b))
                    (s {:state :foo})
                    (s {:state :foo})
                    (s {:state :bar})
                    (s {:state :baz})
                    (is (= 1 @a))
                    (is (= 1 @b)))))

(deftest where*-test
         (test-stream (where* identity)
                      [true false nil 2]
                      [true 2])

         (test-stream (where* expired?)
                      [{:time -1 :ttl 0.5}
                       {:time 0 :ttl 1}]
                      [{:time -1 :ttl 0.5}])

         ; Complex closure with else clause
         (let [good (atom [])
               bad  (atom [])
               s (where* (fn [event]
                           (or (= "good" (:service event))
                               (< 2 (:metric event))))
                         (partial swap! good conj)
                         (else (partial swap! bad conj)))
               events [{:service "good" :metric 0}
                       {:service "bad" :metric 0}
                       {:metric 1}
                       {:service "bad" :metric 1}
                       {:service "bad" :metric 3}]]

           ; Run stream
           (doseq [e events] (s e))

           (is (= @good 
                  [{:service "good" :metric 0}
                   {:service "bad" :metric 3}]))
           (is (= @bad
                  [{:service "bad" :metric 0}
                   {:metric 1}
                   {:service "bad" :metric 1}])))

         (testing "evaluates children once"
                  ; Where should evaluate its children exactly once.
                  (let [x (atom 0)
                        s (where* (fn [e] true) (do (swap! x inc) identity))]
                    (is (= @x 1))
                    (s {:service "test"})
                    (is (= @x 1))
                    (s {:service "test"})
                    (is (= @x 1)))))

(deftest where*-return-value
         ; Where*'s return value should be whether the predicate matched.
         (is (= true ((where* expired? (fn [e] "hi"))
                        (expire {}))))
         (is (= 2 ((where* (constantly 2)) :zoom))))

(deftest where-test
         (testing "field"
                  (let [r (ref [])
                        s (where (or (state "ok" "good")
                                     (= "weird" state))
                                 (fn [e] (dosync (alter r conj e))))
                        events [{:state "ok"}
                                {:state "good"}
                                {:state "weird"}
                                {:state "error"}]
                        expect [{:state "ok"}
                                {:state "good"}
                                {:state "weird"}]]
                    (doseq [e events] (s e))
                    (is (= expect (deref r)))))

         (testing "event binding"
           (test-stream (where (= event 2))
                        [1 2 3 2]
                        [2 2]))

         (testing "regex"
                  (test-stream (where (service #"^foo"))
                               [{}
                                {:service "foo"}
                                {:service "food"}]
                               [{:service "foo"}
                                {:service "food"}]))
        
         (testing "functions"
                  (test-stream (where (and metric (even? metric)))
                               [{}
                                {:metric 1}
                                {:metric 2}]
                               [{:metric 2}]))

         (testing "functions as values"
                  ; May not support this later. I don't think it's all that
                  ; useful given you can just use (even? metric).
                  (test-stream (where (and metric (metric even?)))
                               [{}
                                {:metric 1}
                                {:metric 2}]
                               [{:metric 2}]))

         (testing "using sets as predicates"
                  (test-stream (where (service #{"service1" "service2"}))
                               [{:service "service1"}
                                {:service "service2"}
                                {:service "service-doesnt-match"}]
                               [{:service "service1"}
                                {:service "service2"}]))


         (testing "variable"
                  ; Verify that the macro allows variables to be used in
                  ; predicates.
                  (let [regex #"cat"]
                    (test-stream (where (service regex))
                                 [{:service "kitten"}
                                  {:service "cats"}]
                                 [{:service "cats"}])))


         (testing "tagged (one tag)"
                  (let [r (atom [])
                        s (where (tagged "foo") (append r))
                        events [{}
                                {:tags []}
                                {:tags ["blah"]}
                                {:tags ["foo"]}
                                {:tags ["foo" "bar"]}]]
                    (doseq [e events] (s e))
                    (is (= (deref r)
                           [{:tags ["foo"]} {:tags ["foo" "bar"]}]))))

         (testing "tagged-all (one tag)"
                  (let [r (atom [])
                        s (where (tagged-all "foo") (append r))
                        events [{}
                                {:tags []}
                                {:tags ["blah"]}
                                {:tags ["foo"]}
                                {:tags ["foo" "bar"]}]]
                    (doseq [e events] (s e))
                    (is (= (deref r)
                           [{:tags ["foo"]} {:tags ["foo" "bar"]}]))))

         (testing "tagged-any (one tag)"
                  (let [r (atom [])
                        s (where (tagged-any "foo") (append r))
                        events [{}
                                {:tags []}
                                {:tags ["blah"]}
                                {:tags ["foo"]}
                                {:tags ["foo" "bar"]}]]
                    (doseq [e events] (s e))
                    (is (= (deref r)
                           [{:tags ["foo"]} {:tags ["foo" "bar"]}]))))

         (testing "tagged (multiple tags)"
                  (let [r (atom [])
                        s (where (tagged ["foo" "bar"]) (append r))
                        events [{}
                                {:tags []}
                                {:tags ["blah"]}
                                {:tags ["foo"]}
                                {:tags ["foo" "bar"]}
                                {:tags ["foo" "bar" "baz"]}]]
                    (doseq [e events] (s e))
                    (is (= (deref r)
                           [{:tags ["foo" "bar"]} {:tags ["foo" "bar" "baz"]}]))))

         (testing "tagged-all (multiple tags)"
                  (let [r (atom [])
                        s (where (tagged-all ["foo" "bar"]) (append r))
                        events [{}
                                {:tags []}
                                {:tags ["blah"]}
                                {:tags ["foo"]}
                                {:tags ["foo" "bar"]}
                                {:tags ["foo" "bar" "baz"]}]]
                    (doseq [e events] (s e))
                    (is (= (deref r)
                           [{:tags ["foo" "bar"]} {:tags ["foo" "bar" "baz"]}]))))

         (testing "tagged-any (multiple tags)"
                  (let [r (atom [])
                        s (where (tagged-any ["foo" "bar"]) (append r))
                        events [{}
                                {:tags []}
                                {:tags ["blah"]}
                                {:tags ["foo"]}
                                {:tags ["foo" "bar"]}
                                {:tags ["baz" "bar"]}]]
                    (doseq [e events] (s e))
                    (is (= (deref r)
                           [{:tags ["foo"]} {:tags ["foo" "bar"]} {:tags ["baz" "bar"]}]))))

         (testing "else"
                  ; Where should take an else clause.
                  (let [a (atom [])
                        b (atom [])]
                    (run-stream
                      (where (service #"a")
                             #(swap! a conj (:service %))
                             (else #(swap! b conj (:service %))))
                      [{:service "cat"}
                       {:service "dog"}
                       {:service nil}
                       {:service "badger"}])
                    (is (= @a ["cat" "badger"]))
                    (is (= @b ["dog" nil]))))

         (testing "evaluates children once"
                  ; Where should evaluate its children exactly once.
                  (let [x (atom 0)
                        s (where true (do (swap! x inc) identity))]
                    (is (= @x 1))
                    (s {:service "test"})
                    (is (= @x 1))
                    (s {:service "test"})
                    (is (= @x 1))))

         (testing "return value"
                  ; Where's return value should be whether the predicate
                  ; matched.
                  (is (= true  ((where (service "foo")) {:service "foo"})))
                  (is (= false ((where (service "foo")) {:service "bar"})))

                  (is (= true  ((where (tagged "foo")) {:tags ["foo"]})))
                  (is (= false ((where (tagged "foo")) {:tags ["bar"]})))
                  (is (= true  ((where (tagged-all "foo")) {:tags ["foo"]})))
                  (is (= false ((where (tagged-all "foo")) {:tags ["bar"]})))
                  (is (= true  ((where (tagged-any "foo")) {:tags ["foo"]})))
                  (is (= false ((where (tagged-any "foo")) {:tags ["bar"]})))

                  (is (= true  ((where (tagged ["foo" "bar"])) {:tags ["foo" "bar"]})))
                  (is (= false ((where (tagged ["foo" "bar"])) {:tags ["bar"]})))
                  (is (= true  ((where (tagged-all ["foo" "bar"])) {:tags ["foo" "bar"]})))
                  (is (= false ((where (tagged-all ["foo" "bar"])) {:tags ["bar"]})))
                  (is (= true ((where (tagged-any ["foo" "bar"])) {:tags ["foo" "bar"]})))
                  (is (= false ((where (tagged-any ["foo" "bar"])) {:tags ["blah"]})))

                  (is (= true ((where (service "foo") 
                                      (fn [event] 2)) 
                                 {:service "foo"})))
                  (is (= false ((where (service "foo")
                                       (else (fn [event] 2)))
                                  {:service "bar"})))

                  (is (= 2 ((where 2) :wheeee!)))
                  (is (= nil ((where nil) :zoooom!)))))

(deftest default-kv
         (let [r (atom nil)
               s (default :service "foo" (register r))]
           (s {:service nil})
           (is (= {:service "foo"} (deref r)))

           (s {:service "foo"})
           (is (= {:service "foo"} (deref r)))

           (s {:service "bar" :test "baz"})
           (is (= {:service "bar" :test "baz"} (deref r)))))

(deftest default-map
         (let [r (atom nil)
               s (default {:service "foo" :state nil} (register r))]
           (s (event {:service nil}))
           (is (= "foo" (:service (deref r))))
           (is (= nil (:state (deref r))))

           (s (event {:service "foo"}))
           (is (= "foo" (:service (deref r))))
           (is (= nil (:state (deref r))))

           (s (event {:service "bar" :host "baz" :state "evil"}))
           (is (= "bar" (:service (deref r))))
           (is (= "baz" (:host (deref r))))
           (is (= "evil" (:state (deref r))))))

(deftest with-kv
         (let [r (ref nil)
               s (with :service "foo" (fn [e] (dosync (ref-set r e))))]
           (s {:service nil})
           (is (= {:service "foo"} (deref r)))

           (s {:service "foo"})
           (is (= {:service "foo"} (deref r)))

           (s {:service "bar" :test "baz"})
           (is (= {:service "foo" :test "baz"} (deref r)))))

(deftest with-map
         (let [r (ref nil)
               s (with {:service "foo" :state nil} (fn [e] (dosync (ref-set r e))))]
           (s (event {:service nil}))
           (is (= "foo" (:service (deref r))))
           (is (= nil (:state (deref r))))

           (s (event {:service "foo"}))
           (is (= "foo" (:service (deref r))))
           (is (= nil (:state (deref r))))

           (s (event {:service "bar" :test "baz" :state "evil"}))
           (is (= "foo" (:service (deref r))))
           (is (= nil (:state (deref r))))))

(deftest by-single
         ; Each test stream keeps track of the first host it sees, and confirms
         ; that each subsequent event matches that host.
         (let [i (ref 0)
               s (by :host
                     (let [host (ref nil)]
                       (fn [event]
                         (dosync
                           (alter i inc)
                           (when (nil? (deref host))
                             (ref-set host (event :host)))
                           (is (= (deref host) (event :host)))))))
               events (map (fn [h] {:host h}) [:a :a :b :a :c :b])]
           (doseq [event events]
             (s event))
           (is (= (count events) (deref i)))))

(deftest by-multiple
         ; Each test stream keeps track of the first host/service it sees, and
         ; confirms that each subsequent event matches that host.
         (let [i (ref 0)
               s (by [:host :service]
                     (let [host (ref nil)
                           service (ref nil)]
                       (fn [event]
                         (dosync
                           (alter i inc)

                           (when (nil? (deref host))
                             (ref-set host (event :host)))
                           (when (nil? (deref service))
                             (ref-set service (event :service)))

                           (is (= (deref host) (event :host)))
                           (is (= (deref service) (event :service)))))))

               events (map (fn [h] {:host (first h)
                                    :service (last h)})
                           [[1 :a]
                            [1 :b]
                            [1 :a]
                            [2 :a]
                            [2 :a]
                            [1 :a]
                            [2 :a]
                            [1 :b]])]
           (doseq [event events]
             (s event))
           (is (= (count events) (deref i)))))

(deftest by-evaluates-children-once-per-branch
         (let [i (atom 0)
               s (by :metric (do (swap! i inc) identity))]
           (is (= @i 0))
           (s {:metric 1})
           (is (= @i 1))
           (s {:metric 2})
           (is (= @i 2))
           (s {:metric 1})
           (is (= @i 2))))

(deftest pipe-test
  (testing "One stage"
    (test-stream
      (pipe !)
      [1 2 3]
      [1 2 3]))

  (testing "Chained stages"
    (test-stream
      (pipe ! (smap inc !) (smap (partial * 2) !))
      [1 2 3]
      [4 6 8]))

  (testing "Multiple markers"
    (test-stream
      (pipe - (splitp < metric
                      0.9 (with :state :critical -)
                      0.5 (with :state :warning  -)
                          (with :state :ok       -))
            (smap identity -))
      [{:metric 0.1}
       {:metric 0.7}
       {:metric 1.0}]
      [{:metric 0.1 :state :ok}
       {:metric 0.7 :state :warning}
       {:metric 1.0 :state :critical}]))

  (testing "Rebinding a marker"
    (test-stream
      (pipe - (let [! -
                    - clojure.core/-]
                (smap - !)))
      [-1 0 1]
      [1 0 -1])))

(deftest fill-in-test
         (test-stream-intervals
           (fill-in 0.01 {:metric 0})
           []
           [])

         ; Quick succession
         (is (= (map :metric (run-stream-intervals
                               (fill-in 0.01 {:metric 0})
                               (interpose nil (em 1 2 3))))
                [1 2 3]))

         ; With a gap and expiry
         (is (= (map :metric (run-stream-intervals
                               (fill-in 0.05 {:metric 0})
                               [{:metric 1} 0.06
                                {:metric 2} nil
                                {:metric 3} 0.08
                                {:metric 4 :state "expired"} 0.06
                                {:metric 5}]))
                [1 0 2 3 0 4 5]))
         )

(deftest fill-in-last-test
  (test-stream-intervals
    (fill-in-last 0.01 {:metric 0})
    []
    [])

  ; Quick succession
  (let [output (run-stream-intervals
                 (fill-in-last 0.01 {:metric 0})
                 (interpose nil (evs {:host "foo" :service "bar"}
                                     1 2 3)))]
    (is (= (map :metric output)
           [1 2 3]))
    (is (= (map :host output)
           ["foo" "foo" "foo"]))
    (is (= (map :service output)
           ["bar" "bar" "bar"])))

  ; With a gap and expiry
  (let [output (run-stream-intervals
                 (fill-in-last 0.05 {:metric 0})
                 [{:host "a" :metric 1} 0.06
                  {:host "b" :metric 2} nil
                  {:host "c" :metric 3} 0.08
                  {:host "d" :metric 4 :state "expired"} 0.06
                  {:host "e" :metric 5}])]
    (is (= (map :metric output)
           [1 0 2 3 0 4 5]))
    (is (= (map :host output)
           ["a" "a" "b" "c" "c" "d" "e"]))))

(deftest interpolate-constant-test
         (test-stream-intervals 
           (interpolate-constant 0.01)
           []
           [])

         ; Should forward a single state
         (is (= (map :metric (run-stream-intervals
                               (interpolate-constant 0.1)
                               [{:metric 1} 0.05]))
                [1]))

         ; Should forward first state and ignore immediate successors
         (is (= (map :metric (run-stream-intervals
                               (interpolate-constant 0.1)
                               [{:metric 1} 0.05 
                                {:metric 2} nil
                                {:metric 3}]))
                [1]))

         ; Should fill in missing states regularly
         (is (= (map :metric (run-stream-intervals 
                               (interpolate-constant 0.1)
                               (interpose 0.22 (em 1 2 3 4))))
                [1 1 1 2 2 3 3]))
         
         ; Should forward final "expired" state.
         (is (= (map :metric (run-stream-intervals
                               (interpolate-constant 0.1)
                               (interpose 0.22
                                          [{:metric 1}
                                           {:metric 2}
                                           {:metric 3}
                                           {:metric 4 :state "expired"}])))
                [1 1 1 2 2 3 3 4]))

         ; Should not fill during expired times.
         (is (= (map :metric (run-stream-intervals
                               (interpolate-constant 0.05)
                               [{:metric 1 :state "expired"}
                                0.12
                                {:metric 2}
                                0.12
                                {:metric 3 :state "expired"}
                                0.12]))
                [1 2 2 2 3]))
         )

(deftest ddt-immediate-test
         ; Empty -> empty
         (test-stream (ddt) [] [])
         ; Ignore stream without metrics
         (test-stream (ddt) [{} {} {} {}] [])
         ; Do nothing the first time
         (test-stream (ddt) [{:metric 1 :time 0}] [])
         ; Differentiate
         (test-stream (ddt) 
                      [{:metric 0 :time 0}
                       {:metric 0 :time 1}
                       {:metric 2 :time 2}
                       {:metric -4 :time 4}]
                      [{:metric 0 :time 1}
                       {:metric 2 :time 2}
                       {:metric -3 :time 4}]))

(deftest ddt-interval-test
         (testing "Quick burst without crossing interval"
                  (reset-time!)
                  (is (= (map :metric (run-stream-intervals 
                                        (ddt 0.1)
                                        [{:metric 1} nil
                                         {:metric 2} nil 
                                         {:metric 3}]))
                         [])))

         (testing "1 event per interval"
                  (reset-time!)
                  (test-stream-intervals
                    (ddt 1)
                    ; Note that the swap occurs just prior to events at time 1.
                    [{:time 0 :metric -1} 99/100
                     {:time 1 :metric 0} 1
                     {:time 2 :metric -5} 1]
                    [{:time 1 :metric 1}
                     {:time 2 :metric -5}]))
        
         (testing "n events per interval"
                  (reset-time!)
                  (test-stream-intervals 
                    (ddt 1)
                    [{:time 0 :metric -1} 1/100
                     {:time 1/2 :metric 100} 1/2 ; Ignored
                     {:time 2/2 :metric 1} 1/2
                     {:time 3/2 :metric nil} 1/2 ; Ignored
                     {:time 4/2 :metric -3} 1/2]
                    [{:time 2/2 :metric 2}
                     {:time 4/2 :metric -4}]))

         (testing "emits zeroes when no events arrive in an interval"
                  (reset-time!)
                  (test-stream-intervals (ddt 2)
                                         [{:time 0 :metric 0} 1
                                          {:time 1 :metric 1} 2
                                          {:time 3 :metric 2} 3]
                                         [{:time 2 :metric 1}
                                          {:time 4 :metric 1/2}
                                          {:time 6 :metric 0}])))

(deftest rate-slow-even
         (let [output (ref [])
               interval 1
               intervals 5
               gen-rate 10
               total (* gen-rate intervals)
               gen-period (/ interval gen-rate)
               r (rate interval
                        (fn [event] (dosync (alter output conj event))))]

           ; Generate events
           (dotimes [_ intervals]
             (dotimes [_ gen-rate]
               (advance! (+ (unix-time) gen-period))
               (r {:metric 1 :time (unix-time)})))

           ; Give all futures time to complete
           (advance! (+ (unix-time) gen-period))

           ; Verify output states
           (let [o (deref output)]
             
             ; All events recorded
             (is (approx-equal total (reduce + (map :metric o))))

             ; Middle events should have the correct rate.
             (is (every? (fn [measured-rate] 
                           (approx-equal gen-rate measured-rate))
                         (map :metric (drop 1 (drop-last o)))))
           
             ; First and last events should be complementary
             (let [first-last (+ (:metric (first o))
                                 (:metric (last o)))]
               (is (or (approx-equal (* 2 gen-rate) first-last)
                       (approx-equal gen-rate first-last))))

             )))

(deftest rate-threaded
         (let [output (atom nil)
               interval 5/2
               total 10000
               threads 4
               r (rate interval
                       (fn [event] (dosync (reset! output event))))

               ; Generate events
               workers (map (fn [t] (future 
                                      (dotimes [i (/ total threads)]
                                        (r {:metric 1 :time (unix-time)}))))
                            (range threads))]

           ; Wait for workers
           (dorun (map deref workers))
           (advance! interval)

           ; All events recorded
           (is (= (/ total interval) (:metric @output)))))

(deftest rate-without-input
         (test-stream-intervals
           (rate 1)
           [{:metric 1 :service "foo"} 0.5
            {:metric 1 :service "bar"} 0.5
            {:metric 1 :service "baz" :ttl 3} 3
            {:state "expired"}]
           [{:time 1 :metric 2 :service "bar"}
            {:time 2 :metric 1 :service "baz" :ttl 3}
            {:time 3 :metric 0 :service "baz" :ttl 2}
            {:time 4 :metric 0 :service "baz" :ttl 1}]))

(deftest rate-expiration
         (test-stream-intervals
           (rate 1)
           [{:time 0 :metric 1 :ttl 3} 5]
           [{:time 1 :metric 1 :ttl 3}
            {:time 2 :metric 0 :ttl 2}
            {:time 3 :metric 0 :ttl 1}]))

(deftest fold-interval-test
         (test-stream-intervals
           (riemann.streams/fold-interval 1 :metric (partial apply +))
           [{:metric 2} 0.1
            {:metric 4} 0.2
            {:metric 2} 0.3
            {:metric 4} 1.0
            {:metric 100} 0.1
            {:metric 100} 1.0]
           (em 12 200)))

(deftest fold-interval-metric-test
         (test-stream-intervals
           (riemann.streams/fold-interval-metric 1 (partial apply +))
           [{:metric 2} 0.1
            {:metric 4} 0.2
            {:metric 2} 0.3
            {:metric 4} 1.0
            {:metric 100} 0.1
            {:metric 100} 1.0]
           (em 12 200)))

(deftest changed-test
         (let [output (atom [])
               r (changed :state (append output))
               r2 (changed :state {:init :ok}
                           (append output))
               states [:ok :bad :bad :ok :ok :ok :evil :bad]]

           ; Apply states
           (doseq [state states]
             (r {:state state}))

           ; Check output
           (is (= [:ok :bad :ok :evil :bad]
                  (vec (map (fn [s] (:state s)) (deref output)))))

           ; Test with init
           (reset! output [])
           (doseq [state states]
             (r2 {:state state}))

           (is (= [:bad :ok :evil :bad]
                  (vec (map (fn [s] (:state s)) (deref output)))))))

(deftest changed-state-test
         ; Each test stream keeps track of the first host/service it sees, and
         ; confirms that each subsequent event matches that host, and that
         ; each event is different from the previous state.
         (let [i (ref 0)
               s (changed-state
                   (let [host (ref nil)
                         service (ref nil)
                         state (ref nil)]
                     (fn [event]
                       (dosync
                         (alter i inc)

                         (is (not= (deref state) (:state event)))
                         (ref-set state (:state event))

                         (when (nil? (deref host))
                           (ref-set host (event :host)))
                         (when (nil? (deref service))
                           (ref-set service (event :service)))

                         (is (= (deref host) (event :host)))
                         (is (= (deref service) (event :service)))))))

               events [{:host 1 :service 1 :state 1}
                       {:host 2 :service 1 :state 1}
                       {:host 1 :service 1 :state 1}
                       {:host 1 :service 1 :state 2}
                       {:host 2 :service 1 :state 2}
                       {:host 2 :service 2 :state 1}
                       {:host 2 :service 1 :state 1}]]

           (doseq [event events]
             (s event))
           (is (= 6 (deref i)))))
(deftest within-test
         (let [output (ref [])
               r (within [1 2]
                         (fn [e] (dosync (alter output conj e))))
               metrics [0.5 1 1.5 2 2.5]
               expect [1 1.5 2]]
           
           (doseq [m metrics] (r {:metric m}))
           (is (= expect (vec (map (fn [s] (:metric s)) (deref output)))))))

(deftest without-test
         (let [output (ref [])
               r (without [1 2]
                         (fn [e] (dosync (alter output conj e))))
               metrics [0.5 1 1.5 2 2.5]
               expect [0.5 2.5]]
           
           (doseq [m metrics] (r {:metric m}))
           (is (= expect (vec (map (fn [s] (:metric s)) (deref output)))))))

(deftest over-test
         (let [output (ref [])
               r (over 1.5
                         (fn [e] (dosync (alter output conj e))))
               metrics [0.5 1 1.5 2 2.5]
               expect [2 2.5]]
           
           (doseq [m metrics] (r {:metric m}))
           (is (= expect (vec (map (fn [s] (:metric s)) (deref output)))))))

(deftest under-test
         (let [output (ref [])
               r (under 1.5
                         (fn [e] (dosync (alter output conj e))))
               metrics [0.5 1 1.5 2 2.5]
               expect [0.5 1]]
           
           (doseq [m metrics] (r {:metric m}))
           (is (= expect (vec (map (fn [s] (:metric s)) (deref output)))))))

(deftest ewma-timeless-test
         (test-stream (ewma-timeless 0)
                      (em 1 10 20 -100 4)
                      (em 0 0  0   0   0))
         (test-stream (ewma-timeless 1)
                      (em 5 13 1 -10 3)
                      (em 5 13 1 -10 3))
         (test-stream (ewma-timeless 1/2)
                      (em 1   1   1   1     1    )
                      (em 1/2 3/4 7/8 15/16 31/32)))

(deftest ewma-test
  (let [output1 (atom [])
        output2 (atom [])
        output5 (atom [])
        seconds 50
        s1 (ewma 1
          (fn [event] (swap! output1 conj event)))
        s2 (ewma 2
          (fn [event] (swap! output2 conj event)))
        s5 (ewma 5
          (fn [event] (swap! output5 conj event)))]

    ; Generate enough events to allow the means to converge
    (dotimes [n seconds]
      (s1 {:metric 1 :time (inc n)})
      (s2 {:metric 1 :time (inc n)})
      (s5 {:metric 1 :time (inc n)}))

    ; Verify that the means have converged
    (is (every? (fn [measured-rate]
                  (approx-equal 1 measured-rate))
                  (map :metric (drop 10 @output1))))
    (is (every? (fn [measured-rate]
                  (approx-equal 1 measured-rate))
                  (map :metric (drop 15 @output2))))
    (is (every? (fn [measured-rate]
                  (approx-equal 1 measured-rate))
                  (map :metric (drop 40 @output5))))

    ; Verify halflives with spaced out :metric 0 events
    (s1 {:metric 0 :time (+ 1 seconds)})
    (s1 {:metric 0 :time (+ 2 seconds)})
    (is (approx-equal 1/2 (:metric (last (drop-last @output1)))))
    (is (approx-equal 1/4 (:metric (last @output1))))

    (s2 {:metric 0 :time (+ 2 seconds)})
    (s2 {:metric 0 :time (+ 4 seconds)})
    (is (approx-equal 1/2 (:metric (last (drop-last @output2)))))
    (is (approx-equal 1/4 (:metric (last @output2))))

    (s5 {:metric 0 :time (+ 5 seconds)})
    (s5 {:metric 0 :time (+ 10 seconds)})
    (is (approx-equal 1/2 (:metric (last (drop-last @output5)))))
    (is (approx-equal 1/4 (:metric (last @output5)))))

  ; Verify that metrics are weighted by :time, regardless of order of arrival
  (test-stream-intervals (ewma 1)
    [{:metric 1 :time 0} 1
     {:metric 1 :time 2} 1
     {:metric 1 :time 1} 1]
    [{:metric 0.5 :time 0}
     {:metric 0.625 :time 2}
     {:metric 0.875 :time 2}]))

(deftest top-test
  (let [e (fn [s m & tags] {:service s :metric m :tags tags})
        a (fn [m & tags] (apply e :a m tags))
        b (fn [m & tags] (apply e :b m tags))
        c (fn [m & tags] (apply e :c m tags))
        d (fn [m & tags] (apply e :d m tags))]

           ; A single event
           (test-stream (top 1 :metric)
                        [(a 1)]
                        [(a 1)])

           ; Repeating the same service
           (test-stream (top 1 :metric)
                        [(a 1) (a 2) (a 1) (a 3)]
                        [(a 1) (a 2) (a 1) (a 3)])

           ; Displacing a smaller event
           (test-stream (top 2 :metric)
                        [(a 1) (b 2) (c 3) (a 1)          (b 2)]
                        [(a 1) (b 2) (c 3) (expire (a 1)) (b 2)])
         
           ; Allowing in a smaller event when there's room
           (test-stream (top 2 :metric)
                        [(a 2) (b 2) (c 1)          (a 5) (c 1)          (a 0) (c 1)]
                        [(a 2) (b 2) (expire (c 1)) (a 5) (expire (c 1)) (a 0) (c 1)])

           ; Ignoring smaller events
           (test-stream (top 2 :metric)
                        [(a 1) (b 2) (c 3)         (d 1)          (a 2)          (b nil) (d 2)]
                        [(a 1) (b 2) (c 3) (expire (d 1)) (expire (a 2)) (expire (b nil)) (d 2)])

           ; Events without metrics
           (test-stream (top 1 :metric)
                        [(a nil)          (b 1) (a nil)]
                        [(expire (a nil)) (b 1) (expire (a nil))])

           ; Events without metrics displace previous events
           (test-stream (top 1 :metric)
                        [(b 2) (b nil)          (a 1)]
                        [(b 2) (expire (b nil)) (a 1)])

           ; Expiring a nonexistent event
           (test-stream (top 2 :metric)
                        [(expire (a 2))]
                        [(expire (a 2))])

           ; Expiring an existing event
           (test-stream (top 1 :metric)
                        [(a 2) (expire (a 1)) (b 1)]
                        [(a 2) (expire (a 1)) (b 1)])

           ; Ring
           (test-stream (top 2 :metric)
                        [(a 1) (b 2) (c 3) (d 4)         (a 2)          (b 3)  (c 4) (d 5)]
                        [(a 1) (b 2) (c 3) (d 4) (expire (a 2)) (expire (b 3)) (c 4) (d 5)])

           ; Demotion
           (with-test-stream index
             (top 1 :metric index bit-bucket true)
             [(a 1) (b 2)]
             [(a 1) (expire (a 1)) (b 2)])))

(deftest throttle-test
         (test-stream-intervals (throttle 3 2)
                                [{:state "1"} 0
                                 {:state "2"} 0
                                 {:state "3"} 1       ; t = 0 
                                 {:state "4"} 1       ; t = 1
                                 {:state "5"} 1       ; t = 2
                                 {:state "expired"} 0 ; t = 3
                                 {:state "expired"} 0 ; t = 3
                                 {:state "expired"} 2 ; t = 3
                                 {:state "expired"} 100] ; t = 5
                                [{:state "1"}
                                 {:state "2"}
                                 {:state "3"}
                                 
                                 {:state "5"}
                                 {:state "expired"}
                                 {:state "expired"}
                                 
                                 {:state "expired"}]))

(deftest rollup-test
         (testing "single output event per interval"
           (test-stream-intervals
             (rollup 1 2)
             [:a 0 :b 0 :c 2 :d 0 :e 0 :f 2]
             [[:a] [:b :c] [:d :e :f]]))

         (testing "basic rollups"
                  (test-stream-intervals 
                    (rollup 2 1)
                    [ 1 0 2 0 3 1   4 0 5 0 6 0 7 1       :foo 10]
                    [[1] [2]   [3] [4]           [5 6 7] [:foo]  ]))
         ;          [[1] [2]   [3] [4] [5]       [6 7] [:foo]] 

         (testing "expired events"
                  (reset-time!)
                  (test-stream-intervals
                    (rollup 2 3)
                    [ 1 0 {:state "expired"} 0 :a 1 :b 1 :c 1 ]
                    [[1] [{:state "expired"}]              [:a :b :c]])

                  (reset-time!)
                  (let [e {:state "expired"}]
                    (test-stream-intervals
                      (rollup 2 2)
                      [ e 0 e 0 e 1 e 1]
                      [[e] [e]       [e e]]))))

(deftest batch-test
  (testing "nothing"
    (test-stream-intervals (batch 2 3) [] []))

  (testing "incomplete batches"
    (reset-time!)
    (test-stream-intervals
      (batch 2 3)
      [:a 3 :b 1 :c 2 :d 3]
      [[:a] [:b :c] [:d]]))

  (testing "overflowing batches"
    (reset-time!)
    (test-stream-intervals
      (batch 2 3)
      [:a 1 :b 1 :c 1 :d 1 :e 1 :f 1]
      [[:a :b] [:c] [:d :e] [:f]])))

(deftest coalesce-test
         (let [out (atom [])
               s (coalesce #(reset! out %))
               a1 {:service :a :state "one" :time 0}
               b1 {:service :b :state "one" :time 0}
               a2 {:service :a :state "two" :time 3 :ttl 2}
               c1 {:service :c :state "one" :time 0}
               b2 {:service :b :state "two" :time 0}]

           (s a1)
           (advance! 1.1)
           (is (= (set @out) #{a1}))

           (s b1)
           (advance! 2.1)
           (is (= (set @out) #{a1 b1}))

           (s a2)
           (advance! 3.1)
           (is (= (set @out) #{a2 b1}))

           ; Wait for ttl expiry of a2
           ; Should receive expired a2 once

           ; a2 arrived at 3, expired at 5,
           ; and the final expiry flush happens at 6
           ; (due to the batching in coalesce)
           (s c1)
           (advance! 6.1)
           (is (= (set @out) #{a2 b1 c1}))
           
           (s b2)
           (advance! 7.1)
           (is (= (set @out) #{b2 c1}))))

(deftest stable-test
         ; Doesn't emit until dt seconds have passed.
         (test-stream (stable 3 :x)
                      [{:x 1 :time 0} {:x 1 :time 1} {:x 1 :time 2}]
                      [])
 
         ; Constant values are emitted after dt seconds
         (test-stream (stable 3 :x)
                      [{:x 1 :time 0} {:x 1 :time 1} {:x 1 :time 3}]
                      [{:x 1 :time 0} {:x 1 :time 1} {:x 1 :time 3}])

         ; Ignores spikes
         (test-stream (stable 3 :x)
                      [{:x 0 :time 0}
                       {:x 0 :time 3}
                       {:x 1 :time 4}
                       {:x 1 :time 5}
                       {:x 0 :time 6}
                       {:x 0 :time 9}]
                      [{:x 0 :time 0}
                       {:x 0 :time 3}
                       ; spike elided
                       {:x 0 :time 6}
                       {:x 0 :time 9}])

         ; Ignores flapping
         (test-stream (stable 3 :x)
                      [{:x 0 :time 0}
                       {:x 0 :time 10}
                       {:x 1 :time 11}
                       {:x 0 :time 11}
                       {:x 1 :time 12}
                       {:x 5 :time 13}
                       {:x 2 :time 14}
                       {:x 2 :time 17}]
                      [{:x 0 :time 0}
                       {:x 0 :time 10}
                       {:x 2 :time 14}
                       {:x 2 :time 17}])

         ; Triggers after dt seconds of stability, even without new events.
         (test-stream-intervals (stable 10 :x)
                                [{:x 0 :time 0} 1
                                 {:x 1 :time 1} 10
                                 {:x 2 :time 11} 1]
                                [{:x 1 :time 1}])

         ; Triggers after dt seconds with new events.
         (reset-time!)
         (test-stream-intervals (stable 10 :x)
                                [{:x 0 :time 0} 1
                                 {:x 0 :time 1} 4
                                 {:x 0 :time 5} 6
                                 {:x 1 :time 11} 1]
                                [{:x 0 :time 0}
                                 {:x 0 :time 1}
                                 {:x 0 :time 5}]))

(deftest project-test
         ; Empty -> empty
         (test-stream (project [(service :foo) (service :bar)])
                      []
                      [])

         ; Without anything to project to, does nothing
         (test-stream (project [])
                      [1 2 3]
                      [])

         ; Basic test: ignores non-matching events, updates state properly
         (test-stream (project [(service "foo") (service "bar")])
                      [{:service "cat"}
                       {:service "foo" :a 1}
                       {:service "foo" :a 2}
                       {:service "meow"}
                       {:service "bar" :b 3}
                       {:service "foo" :b 4}]
                      [[{:service "foo" :a 1} nil]
                       [{:service "foo" :a 2} nil]
                       [{:service "foo" :a 2} {:service "bar" :b 3}]
                       [{:service "foo" :b 4} {:service "bar" :b 3}]])

         ; Passes on initially expired events correctly
         (test-stream
           (project [(service "foo") (service "bar")])
           [{:service "foo" :state "expired"}
            {:service "foo" :state "expired"}
            {:service "cat"}]
           [[{:service "foo" :time 0 :state "expired"} nil]
            [{:service "foo" :time 0 :state "expired"} nil]])

         ; Expires existing events
         (test-stream
           (project [(service "foo") (service "bar")])
           [{:service "foo" :state "ok"}
            {:service "bar" :state "ok"}
            {:service "bar" :state "expired"}
            {:service "foo" :state "expired"}
            {:service "bar" :state "expired"}]
           [[{:service "foo" :state "ok"}
             nil]
            [{:service "foo" :state "ok"} 
             {:service "bar" :state "ok"}]
            [{:service "foo" :state "ok"} 
             {:service "bar" :state "expired" :time 0}]
            [{:service "foo" :state "expired" :time 0}
             nil]
            [nil 
             {:service "bar" :state "expired" :time 0}]])

         ; Expiration test: expires own events when the time comes.
         (test-stream-intervals
           (project [(service "foo") (service "bar")])
           [{:service "foo" :state "ok" :time 0 :ttl 1}
            2
            {:service "bar" :state "ok"}
            1
            {:service "bar" :state "ok2"}]
           [[{:service "foo" :state "ok" :time 0 :ttl 1} nil]
            [{:service "foo" :state "expired" :time 2}
             {:service "bar" :state "ok"}]
            [nil {:service "bar" :state "ok2"}]]))

(deftest adjust-test
  (let [out (atom nil)
        s (adjust [:state str " 2"] (register out))]

    (s {})
    (is (= (deref out) {:state " 2"}))

    (s {:state "hey" :service "bar"})
    (is (= (deref out) {:state "hey 2" :service "bar"})))

  (let [out (atom nil)
        s (adjust #(assoc % :metric (count (:tags %)))
                  (register out))]

    (s {:service "a" :tags []})
    (is (= (deref out) {:service "a" :tags [] :metric 0}))

    (s {:service "a" :tags ["foo" "bar"]})
    (is (= (deref out) {:service "a" :tags ["foo" "bar"] :metric 2}))))

(deftest moving-event-window-test
         ; Zero-width windows.
         (test-stream (moving-event-window 0) [] [])
         (test-stream (moving-event-window 0) [1 2] [[] []])

         ; n-width windows
         (test-stream (moving-event-window 2) [1 2 3] [[1] [1 2] [2 3]]))

(deftest fixed-event-window-test
         ; Zero-width windows.
         (test-stream (fixed-event-window 0) [] [])
         (test-stream (fixed-event-window 0) [1 2] [])

         ; n-width windows
         (test-stream (fixed-event-window 2) [1] [])
         (test-stream (fixed-event-window 2) [1 2] [[1 2]])
         (test-stream (fixed-event-window 2) [1 2 3 4 5] [[1 2] [3 4]]))

(deftest runs-test
         ;; Zero-width runs
         (test-stream (runs 0 :state)
                      []
                      [])

         (test-stream (runs 0 :state)
                      [{:state 2} {:state 3} {:state 4}]
                      [])

         ;; n-width runs
         (test-stream (runs 3 :state)
                      [{:state 1} {:state 2} {:state 2}]
                      [])

         (test-stream (runs 3 :state)
                      [{:state 1} {:state 1} {:metric "0.5" :state 2} {:metric "0.6" :state 2} {:state 3} {:state 4 :metric "5.0"} {:state 4 :metric "5.1"} {:state 4 :metric "5.2"}]
                      [{:state 4 :metric "5.2"}]
                      )
  )

(deftest moving-time-window-test
         ; Zero-second windows.
         (test-stream (moving-time-window 0) [] [])
         (test-stream (moving-time-window 0) [{:time 1} {:time 2}] [])

         ; n-width windows
         (test-stream (moving-time-window 2) [] [])
         (test-stream (moving-time-window 2) [{:time 1}] [[{:time 1}]])
         (test-stream (moving-time-window 2) 
                      [{:time 1} {:time 2} {:time 3} {:time 4}]
                      [[{:time 1}]
                       [{:time 1} {:time 2}]
                       [{:time 2} {:time 3}]
                       [{:time 3} {:time 4}]])

         ; With out-of-order events
         (test-stream (moving-time-window 2)
                      [{:time 5}
                       {:time 1}
                       {:time 2}
                       {:time 6}
                       {:time 3}
                       {:time 8}
                       {:time 4}
                       {:time 8}
                       {:time 5}
                       {:time 9}]
                      [[{:time 5}]
                       [{:time 5} {:time 6}]
                       [{:time 8}]
                       [{:time 8} {:time 8}]
                       [{:time 8} {:time 8} {:time 9}]]))

(deftest fixed-time-window-test
         ; Zero-time windows.
         (is (thrown? IllegalArgumentException (fixed-time-window 0)))

         ; n-width windows
         (test-stream (fixed-time-window 2) [] [])
         (test-stream (fixed-time-window 2) [{:time 1}] [])
         (test-stream (fixed-time-window 2) 
                      [{:time 1} {:time 2} {:time 3} {:time 4} {:time 5}]
                      [[{:time 1} {:time 2}]
                       [{:time 3} {:time 4}]])

         ; With a gap
         (test-stream (fixed-time-window 2) [{:time 1} {:time 7}] 
                      [[{:time 1}] [] []])

         ; With out-of-order events
         (test-stream (fixed-time-window 2)
                      [{:time 5}
                       {:time 1}
                       {:time 2}
                       {:time 6}
                       {:time 3}
                       {:time 8}
                       {:time 4}
                       {:time 8}
                       {:time 5}
                       {:time 9}
                       {:time 11}]
                      [[{:time 5} {:time 6}]
                       [{:time 8} {:time 8}]
                       [{:time 9}]]))

(deftest part-time-simple-test
         ; Record windows of [start-time e1 e2 e3 end-time]
         (advance! 1)
         (let [effects (atom [])
               p (part-time-simple
                   5
                   (fn [_] [])
                   conj
                   (fn [window start end] (swap! effects conj
                                                 (concat [start] window [end]))))]
           (advance! 1) (p :t1)
           (advance! 2) (p :t2)
           (advance! 4) (p :t4)
           (advance! 5) (p :t5)
           (advance! 6) (p :t6)
           (advance! 8) (p :t8)
           (advance! 9) (p :t9)
           (advance! 99) (p :t99)
           (advance! 100) (p :t100)
           (advance! 101) (p :t101)
           (advance! 102) (p :t102)
           (advance! 200)
           (is (= @effects
                  [[1 :t1 :t2 :t4 :t5 6]
                  [6 :t6 :t8 :t9 11]
                  [96 :t99 :t100 101]
                  [101 :t101 :t102 106]]))))

(deftest apdex-test
         (test-stream-intervals
           (apdex 1 (state "ok") (state "warning"))
           (concat
             (interpose 0 (shuffle (concat (repeat 5 {:state "ok"})
                                           (repeat 7 {:state "warning"})
                                           (repeat 3 {:state "expired"})
                                           (repeat 1 {:state "fail"}))))
             [0 {:state "final"} 3])
           [{:state "final" :metric (/ (+ 5 (/ 7 2)) (+ 5 7 2))}]))

(deftest ^:focus clock-skew-test
         (test-stream-intervals (clock-skew)
                                [; foo bar baz
                                 {:time 1 :host :foo} 1
                                 ; 2
                                 {:time 3 :host :bar} 1
                                 ; 3 4
                                 {:time 9 :host :baz} 1
                                 ; 4 5 10
                                 {:time 1 :host :foo} 1
                                 ; 2 6 11
                                 {:time 100 :host :foo}]
                                [{:time 1 :host :foo :metric 0}
                                 {:time 3 :host :bar :metric 0}
                                 {:time 9 :host :baz :metric 5}
                                 {:time 1 :host :foo :metric -4}
                                 {:time 100 :host :foo :metric 89}]))
