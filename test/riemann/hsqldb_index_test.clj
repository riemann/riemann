(ns riemann.hsqldb-index-test
  (:use riemann.query
        riemann.core
        riemann.index
        riemann.hsqldb-index
        [riemann.time :only [unix-time]]
        clojure.test))

(deftest translate-ast-simple
  (let [query-ast (ast "true")]
    (is (= true (translate-ast query-ast)))))

(deftest translate-ast-for-equals
  (let [query-ast (ast "host = nil")]
    (is (= {:statement "host IS NULL", :params nil} (translate-ast query-ast)))))

(deftest translate-ast-for-less-than
  (let [query-ast      (ast "metric < 3")
        translated-ast (translate-ast query-ast)]
    (is (= {:statement "((metric_sint64 IS NOT NULL AND metric_sint64 < ?) OR (metric_f IS NOT NULL AND metric_f < ?))", :params '(3 3)} translated-ast))))

(deftest translate-ast-for-and-and-less-then
  (let [query-ast (ast "host = nil and metric < 3")
        translated-ast (translate-ast query-ast)]
    (is (= {:statement "(host IS NULL) AND (((metric_sint64 IS NOT NULL AND metric_sint64 < ?) OR (metric_f IS NOT NULL AND metric_f < ?)))", :params '(3 3)} translated-ast))))

(deftest translate-ast-for-tagged
  (let [query-ast (ast "tagged \"cat\"")
        translated-ast (translate-ast query-ast)]
    (is (= {:statement "POSITION_ARRAY(? IN tags) != 0", :params '("cat")} translated-ast))))

(deftest translate-ast-for-not-equal
  (let [query-ast (ast "host != \"cat\"")
        translated-ast (translate-ast query-ast)]
    (is (=  {:statement "NOT (host = ?)", :params '("cat")} translated-ast))))

(deftest translate-ast-for-regexp
  (let [query-ast (ast "host ~= \"cat.*\"")
        translated-ast (translate-ast query-ast)]
    (is (= {:statement "REGEXP_SUBSTRING(host,?) IS NOT NULL", :params '("cat.*")} translated-ast))))


(deftest hsqldb-update
  (let [i (wrap-index (hsqldb-index))]
    (i {:host "1"})
    (i {:host "2"})
    (i {:host "1" :service "3" :state :ok})
    (i {:host "1" :service "3" :description "new"})

    (is (= (set (map #(select-keys % [:host :service :description]) i))
           #{{:host "1" :service nil :description nil}
             {:host "2" :service nil :description nil}
             {:host "1" :service "3" :description "new"}}))))

(deftest hsqldb-delete
  (let [i (wrap-index (hsqldb-index))]
    (i {:host "1"})
    (i {:host "2"})
    (delete i {:host "1" :service "1"})
    (delete i {:host "2" :state :ok})
    (is (= (set (map #(select-keys % [:host]) i))
           #{{:host "1"}}))))

(deftest hsqldb-search
  (let [i (wrap-index (hsqldb-index))]
    (i {:host "1"})
    (i {:host "2" :service "meow"})
    (i {:host "3" :service "mrrrow"})
    (is (= (set (map :host (search i (ast "host >= 2 and not service =~ \"%r%\""))))
           #{"2"}))))

(deftest hsqldb-expire
  (let [i (wrap-index (hsqldb-index))]
    (i {:host "1" :ttl 0 :time (- 1 (unix-time))})
    (i {:host "2" :ttl 10 :time (unix-time)})
    (i {:host "3" :ttl 20 :time (- (unix-time) 21)})

    (let [expired (expire i)]
      (is (= (set (map (fn [e] (:host e))
                       expired))
             #{"1" "3"})))

    (is (= (map (fn [e] (:host e)) i)
           ["2"]))))

(deftest hsqldb-read-index
  (let [i (wrap-index (hsqldb-index))]
    (i {:host "1" :service "1" :metric 5})
    (i {:host "1" :service "2" :metric 7})

    (is (= 5 (:metric (lookup i 1 1))))
    (is (= 7 (:metric (lookup i 1 2))))))


(defn random-event
  [& {:as event}]
  (merge {:host    (rand-int 100)
          :service (rand-int 100)
          :ttl     (rand-int 500)
          :time    (- (unix-time) (rand-int 30))}
         event))

(deftest ^:bench indexing-hsqldb-time
  (let [_        (println "building events, this might take some time")
        not-much (doall (repeatedly 100 random-event))
        a-few    (doall (repeatedly 100000 random-event))
        a-lot    (doall (repeatedly 1000000 random-event))
        i        (wrap-index (hsqldb-index))]
    (println "updating and expiring the same 100 events 10000 times:")
    (time (dotimes [iter 10000]
            (do (doseq [event not-much]
                  (i event)))))
    (println "expiring")
    (time (dotimes [iter 10000] (doall (expire i))))
    (clear i)

    (println "updating and expiring the same 100000 events 100 times:")
    (time (dotimes [iter 100]
            (do (doseq [event a-few]
                  (i event)))))
    (println "expiring")
    (time (dotimes [iter 100] (doall (expire i))))
    (clear i)

    (println "updating and expiring the same 10000000 events 10 times:")
    (time (dotimes [iter 10]
            (do (doseq [event a-lot]
                  (i event)))))
    (println "expiring")
    (time (dotimes [iter 10] (doall (expire i))))))
