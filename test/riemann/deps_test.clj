(ns riemann.deps-test
  (:use riemann.deps
        riemann.index
        riemann.core
        [riemann.common :only [event]]
        clojure.test))

(defn context [events]
  (let [i (wrap-index (nbhm-index))]
    (doseq [e events]
      (i (event e)))
    i))

(deftest hash-match
         ; No states
         (is (not (match {:service "foo"}
                         (context [])
                         nil)))
         ; Single state
         (is (match {:state "ok"}
                    (context [{:state "ok"}])
                    nil))
         ; Wrong state
         (is (not (match {:state "ok"}
                         (context [{:state "critical"}])
                         nil))))

(deftest localhost-match
         (let [r (localhost {:service "memcache" :state "ok"})]
           (is (match r
                      (context [{:host 1 :service "memcache" :state "ok"}])
                      {:host 1}))
           (is (not (match r
                      (context [{:host 1 :service "memcache" :state "ok"}])
                      {:host 2})))
           (is (not (match r
                      (context [{:host 1 :service "memcache" :state "false"}])
                      {:host 1})))
           ))

(deftest depends-match
         ; Different service is always true
         (is (match (depends {:service "x"} {:service "y"})
                    (context [])
                    {:service "z"}))

         ; Single dep
         (let [r (depends {:service "x"} {:service "y" :state "ok"})]
           (is (match r (context [{:service "y" :state "ok"}])
                      {:service "x"}))
           (is (not (match r (context [{:service "y" :state "no"}])
                           {:service "x"})))
           (is (not (match r (context [])
                           {:service "x"})))
           ))

(deftest all-match
         (let [r (all {:service "x"} {:service "y"})]
           (is (match r (context [{:service "x"} {:service "y"}]) nil))
           (is (not (match r (context []) nil)))
           (is (not (match r (context [{:service "x"}]) nil)))))

(deftest any-match
         (let [r (any {:service "x"} {:service "y"})]
           (is (match r (context [{:service "x"} {:service "y"}]) nil))
           (is (not (match r (context []) nil)))
           (is (match r (context [{:service "x"}]) nil))))

(deftest real-match
         (let [r (all (depends {:service "lbapp"}
                               (any {:service "riak 1" :state "ok"}
                                    {:service "riak 2" :state "ok"}))
                      (depends {:service "api"}
                               (all
                                 (localhost
                                   (any
                                     {:service "memcached" :state "ok"}
                                     {:service "redis" :state "ok"})
                                   (any
                                     {:service "cpu" :state "ok"}
                                     {:service "cpu" :state "warning"}))
                                 {:host "db" :service "postgres" :state "ok"})))
               c (context [{:service "riak 1" :state "ok"}
                           {:service "riak 2" :state "warning"}
                           {:service "memcached" :host 1 :state "ok"}
                           {:service "memcached" :host 2 :state "critical"}
                           {:service "memcached" :host 3 :state "ok"}
                           {:service "memcached" :host 4 :state "critical"}
                           {:service "redis" :host 1 :state "ok"}
                           {:service "redis" :host 2 :state "ok"}
                           {:service "redis" :host 3 :state "critical"}
                           {:service "redis" :host 4 :state "critical"}
                           {:service "cpu" :host 1 :state "ok"}
                           {:service "cpu" :host 2 :state "warning"}
                           {:service "cpu" :host 3 :state "warning"}
                           {:service "cpu" :host 4 :state "ok"}
                           {:host "db" :service "postgres" :state "ok"}])]

           (is (match r c {:service "lbapp"}))
           (is (match r c {:service "api" :host 1}))
           (is (match r c {:service "api" :host 2}))
           (is (match r c {:service "api" :host 3}))
           (is (not (match r c {:service "api" :host 4})))
           (is (not (match r c {:service "api"})))
           (is (not (match r c {:service "api" :host :invisible})))))

(deftest tag-test
         (let [rule (depends {:service "x"} {:service "y"})
               index (wrap-index (nbhm-index))
               out (atom #{})
               append-out (partial swap! out conj)
               s (deps-tag index rule append-out)]

           (is (= #{} @out))
           (reset! out #{})

           ; Pass through unrelated events.
           (s {})
           (s {:service "other"})
           (is (= #{{:deps-satisfied? true}
                    {:deps-satisfied? true :service "other"}}
                  @out))
           ))

; Someday.
(comment
(deftest suppress-test
         (let [rule (depends {:service "x"} {:service "y"})
               index (wrap-index (nbhm-index))
               out (atom #{})
               append-out (partial swap! out conj)
               s (suppress-dependent-failures {:index index
                                               :interval 0.1
                                               :rule rule}
                                              append-out)]

           (is (= #{} @out))
           (reset! out #{})

           ; Should pass through unrelated events.
           (s {})
           (s {:service "other"})
           (Thread/sleep 100)
           (is (= #{{} {:service "other"}} @out))
           (reset! out #{})
           ; Should hold on to unsatisfied events until dependencies are met.
           (s {:service "x"})
           (Thread/sleep 100)
           (is (= #{} @out))

           (reset! out #{})

           (index {:service "y"})
           (Thread/sleep 150)
           (is (= #{{:service "x"}} @out))

           (reset! out #{})

           ; Should allow unsatisfied events to be updated.
           (delete index {:service "y"})
           (s {:service "x" :state 1})
           (Thread/sleep 100)
           (s {:service "x" :state 2})
           (Thread/sleep 100)
           (is (= #{} @out))

           (reset! out #{})

           (index {:service "y"})
           (Thread/sleep 150)
           (is (= #{{:service "x" :state 2}}))
           )))
