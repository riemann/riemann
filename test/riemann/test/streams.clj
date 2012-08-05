(ns riemann.test.streams
  (:use [riemann.streams])
  (:use [riemann.common])
  (:use [riemann.folds :as folds])
  (:require [riemann.index :as index])
  (:use [clojure.test]))

(defmacro run-stream
  "Applies inputs to stream, and returns outputs."
  [stream inputs]
  `(let [out# (ref [])
         stream# (~@stream (append out#))]
     (doseq [e# ~inputs] (stream# e#))
     (deref out#)))

(defmacro run-stream-intervals
  "Applies a seq of alternating events and intervals (in seconds) between them to stream, returning outputs."
  [stream inputs-and-intervals]
  `(let [out# (ref [])
         stream# (~@stream (append out#))
         start-time# (ref (unix-time))
         next-time# (ref (deref start-time#))]
     (doseq [[e# interval#] (partition-all 2 ~inputs-and-intervals)]
       (stream# e#)
       (when interval#
         (dosync (ref-set next-time# (+ (deref next-time#) interval#)))
         (Thread/sleep (* 1000 (max 0 (- (deref next-time#) (unix-time)))))))
     (let [result# (deref out#)]
       ; close stream
       (stream# {:state "expired"})
       result#)))

(defmacro test-stream
  "Verifies that the given stream, taking inputs, forwards outputs to children."
  [stream inputs outputs]
  `(is (= (run-stream ~stream ~inputs) ~outputs)))

(defmacro test-stream-intervals
  "Verifies that run-stream-intervals, taking inputs/intervals, forwards outputs to chldren."
  [stream inputs-and-intervals outputs]
  `(is (= (run-stream-intervals ~stream ~inputs-and-intervals) ~outputs)))

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
         (let [r (ref nil)
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
           (is (= (deref r) {:metric 0}))))

(deftest counter-test
         (let [r      (ref [])
               s      (counter (append r))
               events [{:metric 2}
                       {}
                       {:metric 1}
                       {:metric 5}
                       {:tags ["reset"] :metric -1}
                       {:metric 2}]]
           (doseq [e events] (s e))
           
           (is (= (deref r)
                  [{:metric 2}
                   {:metric 3}
                   {:metric 8}
                   {:tags ["reset"] :metric -1}
                   {:metric 1}]))))

(deftest match-test
         (let [r (ref nil)
               s (match :service "foo" (fn [e] (dosync (ref-set r e))))]
           (s {:service "bar"})
           (is (= nil (deref r)))

           (s {:service "foo"})
           (is (= {:service "foo"} (deref r))))
        
         ; Regex
         (let [r (ref nil)
               s (match :service #"^f" (fn [e] (dosync (ref-set r e))))]
           (s {:service "bar"})
           (is (= nil (deref r)))

           (s {:service "foo"})
           (is (= {:service "foo"} (deref r)))))

(deftest tagged-test
         (let [r (ref [])
               s (tagged ["kitten" "cat"] (append r))
               events [{:tags ["kitten" "cat"]}
                       {:tags ["kitten", "cat", "meow"]}
                       {:tags ["dog" "cat"]}
                       {:tags ["cat"]}
                       {:tags []}
                       {}]]
           (doseq [e events] (s e))
           (is (= (deref r)
                  [{:tags ["kitten" "cat"]}
                   {:tags ["kitten", "cat", "meow"]}]))))

(deftest tagged-any-test
         (let [r (ref [])
               s (tagged-any ["kitten" "cat"] (append r))
               events [{:tags ["kitten" "cat"]}
                       {:tags ["cat", "dog"]}
                       {:tags ["kitten"]}
                       {:tags ["dog"]}
                       {:tags []}
                       {}]]
           (doseq [e events] (s e))
           (is (= (deref r)
                  [{:tags ["kitten" "cat"]}
                   {:tags ["cat", "dog"]}
                   {:tags ["kitten"]}]))))

(deftest where-event
         (let [r (ref [])
               s (where (or (= "good" (:service event))
                            (< 2 (:metric event)))
                        (fn [e] (dosync (alter r conj e))))
               events [{:service "good" :metric 0}
                       {:service "bad" :metric 0}
                       {:metric 1}
                       {:service "bad" :metric 1}
                       {:service "bad" :metric 3}]
               expect [{:service "good" :metric 0}
                       {:service "bad" :metric 3}]]
           (doseq [e events] (s e))
           (is (= expect (deref r)))))

(deftest where-field
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

(deftest where-tagged
         (let [r (ref [])
               s (where (tagged "foo") (append r))
               events [{}
                       {:tags []}
                       {:tags ["blah"]}
                       {:tags ["foo"]}
                       {:tags ["foo" "bar"]}]]
           (doseq [e events] (s e))
           (is (= (deref r)
                  [{:tags ["foo"]} {:tags ["foo" "bar"]}]))))

(deftest default-kv
         (let [r (ref nil)
               s (default :service "foo" (register r))]
           (s {:service nil})
           (is (= {:service "foo"} (deref r)))

           (s {:service "foo"})
           (is (= {:service "foo"} (deref r)))

           (s {:service "bar" :test "baz"})
           (is (= {:service "bar" :test "baz"} (deref r)))))

(deftest default-map
         (let [r (ref nil)
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
         ; Quick burst without crossing interval
         (is (= (map :metric (run-stream-intervals 
                               (ddt 0.1)
                               [{:metric 1} nil {:metric 2} nil {:metric 3}]))
                []))

         ; 1 event per interval
         (let [t0 (unix-time)]
           (is (= (map :metric (run-stream-intervals
                                 (ddt 0.1)
                                 [{:metric -1 :time t0} 0.1
                                  {:metric 0 :time (+ 1/10 t0)} 0.1
                                  {:metric -5 :time (+ 2/10 t0)} 0.1]))
                  [10 -50])))
        
         ; n events per interval
         (let [t0 (unix-time)]
           (is (= (map :metric (run-stream-intervals
                                 (ddt 0.1)
                                 [{:metric -1 :time t0} 0.01 ; counts
                                  {:metric 100 :time (+ 1/20 t0)} 0.05
                                  {:metric 1 :time (+ 2/20 t0)} 0.05
                                  {:metric nil :time (+ 3/20 t0)} 0.05
                                  {:metric -3 :time (+ 4/20 t0)} 0.05]))
                  [20 -40])))
         )

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
               (Thread/sleep (int (* 1000 gen-period)))
               (r {:metric 1 :time (unix-time)})))

           ; Give all futures time to complete
           (Thread/sleep (* 1000 interval))

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

(deftest rate-fast
         (let [output (ref [])
               interval 1
               total 10000
               threads 4
               r (rate interval
                        (fn [event] (dosync (alter output conj event))))
               t0 (unix-time)]

           (doseq [f (map (fn [t] (future
             (let [c (ref 0)]
               (dotimes [i (/ total threads)]
                       (let [e {:metric 1.0 :time (unix-time)}]
                         (dosync (commute c + (:metric e))))))))
                          (range threads))]
             (deref f))

           ; Generate events
           (doseq [f (map (fn [t] (future 
                                 (dotimes [i (/ total threads)]
                                   (r {:metric 1 :time (unix-time)}))))
                          (range threads))]
                   (deref f))
             
           ; Give all futures time to complete
           (Thread/sleep (* 1100 interval))

           (let [t1 (unix-time)
                 duration (- t1 t0)
                 o (dosync (deref output))]
           
             ; All events recorded
             (is (approx-equal total (reduce + (map :metric o))))

             )))

(deftest changed-test
         (let [output (ref [])
               r (changed :state
                          (fn [event] (dosync (alter output conj event))))
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
           (dosync (ref-set output []))
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

(deftest update-test
         (let [i (index/index)
               s (update-index i)
               states [{:host 1 :state "ok"} 
                       {:host 2 :state "ok"} 
                       {:host 1 :state "bad"}]]
           (doseq [state states] (s state))
           (is (= (set i)
                  #{{:host 1 :state "bad"}
                    {:host 2 :state "ok"}}))))

(deftest delete-from-index-test
         (let [i (index/index)
               s (update-index i)
               d (delete-from-index i)
               states [{:host 1 :state "ok"} 
                       {:host 2 :state "ok"} 
                       {:host 1 :state "bad"}]]
           (doseq [state states] (s state))
           (doseq [state states] (d state))
           (is (= (vec (seq i)) []))))

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

(deftest throttle-test
         (let [out (ref [])
               quantum 0.1
               stream (throttle 5 quantum (append out))
               t1 (unix-time)]
           
           (doseq [state (take 100000 (repeat {:state "foo"}))]
             (stream state))

           (let [dt (- (unix-time) t1)
                 slices (inc (quot dt quantum))
                 maxcount (* slices 5)
                 count (count (deref out))]

             ; Depending on whether we fell exactly on the interval boundary...
             ; ugh I hate testing this shit
             (is (approx-equal count maxcount 0.01))
             (is (zero? (mod count 5))))))

(deftest rollup-test
         (let [out (ref [])
               quantum 0.1
               stream (rollup 2 quantum (append out))
               t1 (unix-time)]
          
           (stream 1)
           (is (= (deref out) [[1]]))
           (stream 2)
           (is (= (deref out) [[1] [2]]))
           (stream 3)
           (is (= (deref out) [[1] [2]]))

           (Thread/sleep 110)
           (is (= (deref out) [[1] [2] [3]]))

           (stream 4)
           (is (= (deref out) [[1] [2] [3] [4]]))
           (stream 5)
           (stream 6)
           (stream 7)
           (is (= (deref out) [[1] [2] [3] [4]]))

           (Thread/sleep 110)
           (is (= (deref out) [[1] [2] [3] [4] [5 6 7]]))))

(deftest coalesce-test
         (let [out (ref [])
               s (coalesce (register out))
               a {:service 1 :state "ok" :time (unix-time)}
               b {:service 2 :state "ok" :time (unix-time)}
               c {:service 1 :state "bad" :time (unix-time)}
               d {:service 1 :state "ok" :time (unix-time) :ttl 0.01}
               e {:service 3 :state "ok" :time (unix-time)}]

           (s a)
           (is (= (set (deref out)) #{a}))

           (s b)
           (is (= (set (deref out)) #{a b}))

           (s c)
           (is (= (set (deref out)) #{b c}))

           (s d)
           (is (= (set (deref out)) #{b d}))

           ; Wait for ttl expiry of d
           (Thread/sleep 11)

           (s e)
           (is (= (set (deref out)) #{b e}))))

(deftest adjust-test
  (let [out (ref nil)
        s (adjust [:state str " 2"] (register out))]

    (s {})
    (is (= (deref out) {:state " 2"}))

    (s {:state "hey" :service "bar"})
    (is (= (deref out) {:state "hey 2" :service "bar"})))

  (let [out (ref nil)
        s (adjust #(assoc % :metric (count (:tags %)))
                  (register out))]

    (s {:service "a" :tags []})
    (is (= (deref out) {:service "a" :tags [] :metric 0}))

    (s {:service "a" :tags ["foo" "bar"]})
    (is (= (deref out) {:service "a" :tags ["foo" "bar"] :metric 2}))))
