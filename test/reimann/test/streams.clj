(ns reimann.test.streams
  (:use [reimann.streams])
  (:use [reimann.common])
  (:require [reimann.index :as index])
  (:use [clojure.test]))

(deftest match-test
         (let [r (ref nil)
               s (match :service "foo" (fn [e] (dosync (ref-set r e))))]
           (s {:service "bar"})
           (is (= nil (deref r)))

           (s {:service "foo"})
           (is (= {:service "foo"} (deref r))))
        
         ; Regex
         (let [r (ref nil)
               s (service? #"^f" (fn [e] (dosync (ref-set r e))))]
           (s {:service "bar"})
           (is (= nil (deref r)))

           (s {:service "foo"})
           (is (= {:service "foo"} (deref r)))))

(deftest where-event
         (let [r (ref [])
               s (where (or (= "good" (:service event))
                            (< 2 (:metric_f event)))
                        (fn [e] (dosync (alter r conj e))))
               events [{:service "good" :metric_f 0}
                       {:service "bad" :metric_f 0}
                       {:metric_f 1}
                       {:service "bad" :metric_f 1}
                       {:service "bad" :metric_f 3}]
               expect [{:service "good" :metric_f 0}
                       {:service "bad" :metric_f 3}]]
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

(deftest by-test
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
               (r {:metric_f 1 :time (unix-time)})))

           ; Give all futures time to complete
           (Thread/sleep (* 1000 interval))

           ; Verify output states
           (let [o (deref output)]
             
             ; All events recorded
             (is (approx-equal total (reduce + (map :metric_f o))))

             ; Middle events should have the correct rate.
             (is (every? (fn [measured-rate] 
                           (approx-equal gen-rate measured-rate))
                         (map :metric_f (drop 1 (drop-last o)))))
           
             ; First and last events should be complementary
             (let [first-last (+ (:metric_f (first o))
                                 (:metric_f (last o)))]
               (is (or (approx-equal (* 2 gen-rate) first-last)
                       (approx-equal gen-rate first-last))))

             )))

(deftest rate-fast
         (let [output (ref [])
               interval 1
               total 1000000
               threads 4
               r (rate interval
                        (fn [event] (dosync (alter output conj event))))
               t0 (unix-time)]

           (time
             (doseq [f (map (fn [t] (future
               (let [c (ref 0)]
                 (dotimes [i (/ total threads)]
                         (let [e {:metric_f 1.0 :time (unix-time)}]
                           (dosync (commute c + (:metric_f e))))))))
                            (range threads))]
               (deref f)))

           ; Generate events
           (doseq [f (map (fn [t] (future 
                                 (dotimes [i (/ total threads)]
                                   (r {:metric_f 1 :time (unix-time)}))))
                          (range threads))]
                   (deref f))
             
           ; Give all futures time to complete
           (Thread/sleep (* 1100 interval))

           (let [t1 (unix-time)
                 duration (- t1 t0)
                 o (dosync (deref output))]
           
             ; All events recorded
             (is (approx-equal total (reduce + (map :metric_f o))))

             )))

(deftest changed-test
         (let [output (ref [])
               r (changed :state
                          (fn [event] (dosync (alter output conj event))))
               states [:ok :bad :bad :ok :ok :ok :evil :bad]]
           
           ; Apply states
           (doseq [state states]
             (r {:state state}))

           ; Check output
           (is (= [:ok :bad :ok :evil :bad]
                  (vec (map (fn [s] (:state s)) (deref output)))))))
           
(deftest within-test
         (let [output (ref [])
               r (within [1 2]
                         (fn [e] (dosync (alter output conj e))))
               metrics [0.5 1 1.5 2 2.5]
               expect [1 1.5 2]]
           
           (doseq [m metrics] (r {:metric_f m}))
           (is (= expect (vec (map (fn [s] (:metric_f s)) (deref output)))))))

(deftest without-test
         (let [output (ref [])
               r (without [1 2]
                         (fn [e] (dosync (alter output conj e))))
               metrics [0.5 1 1.5 2 2.5]
               expect [0.5 2.5]]
           
           (doseq [m metrics] (r {:metric_f m}))
           (is (= expect (vec (map (fn [s] (:metric_f s)) (deref output)))))))

(deftest over-test
         (let [output (ref [])
               r (over 1.5
                         (fn [e] (dosync (alter output conj e))))
               metrics [0.5 1 1.5 2 2.5]
               expect [2 2.5]]
           
           (doseq [m metrics] (r {:metric_f m}))
           (is (= expect (vec (map (fn [s] (:metric_f s)) (deref output)))))))

(deftest under-test
         (let [output (ref [])
               r (under 1.5
                         (fn [e] (dosync (alter output conj e))))
               metrics [0.5 1 1.5 2 2.5]
               expect [0.5 1]]
           
           (doseq [m metrics] (r {:metric_f m}))
           (is (= expect (vec (map (fn [s] (:metric_f s)) (deref output)))))))

(deftest uupdate-test
         (let [i (index/index)
               s (update i)
               states [{:host 1 :state "ok"} 
                       {:host 2 :state "ok"} 
                       {:host 1 :state "bad"}]]
           (doseq [state states] (s state))
           (is (= (set (.values i))
                  #{{:host 1 :state "bad"}
                    {:host 2 :state "ok"}}))))

(deftest delete-from-test
         (let [i (index/index)
               s (update i)
               states [{:host 1 :state "ok"} 
                       {:host 2 :state "ok"} 
                       {:host 1 :state "bad"}]]
           (doseq [state states] (s state))
           (is (= (set (.values i))
                  #{{:host 1 :state "bad"}
                    {:host 2 :state "ok"}}))))

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
