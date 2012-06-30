(ns riemann.test.deps
  (:use [riemann.deps])
  (:use [clojure.test]))

(deftest normalize-class-test
         (is (= (normalize-class "riak")
                [:any "riak" "ok"]))

         (is (= (normalize-class ["riak"])
                [:any "riak" "ok"]))

         (is (= (normalize-class [:local "riak"])
                [:local "riak" "ok"]))

         (is (= (normalize-class ["h" "riak" "weird"])
                ["h" "riak" "weird"])))

(deftest member-test
         (is (member? [:any "riak" "ok"] 
                      {:service "riak" :state "ok"}))

         
         (is (member? [:any "riak" "ok"] 
                      {:host "a" :service "riak" :state "ok"}))

         (is (not (member? [:any "riak" "ok"]
                           {:host "a" :service "foo" :state "ok"})))

         (is (member? ["my" "cpu" :any]
                      {:host "my" :service "cpu" :state "weird"}))

         (is (member? [nil "cpu" "ok"]
                      {:service "cpu" :state "ok"}))

         (is (not (member? [nil "cpu" "ok"]
                           {:host "a" :service "cpu" :state "ok"}))))


(deftest class-query-test
         (is (= (class-query [:any "riak" "ok"] {})
                (list 'and true (list '= "riak" 'service) (list '= "ok" 'state))))

         (is (= (class-query [:local "cpu" nil] {:host "shodan"})
                (list 'and (list '= "shodan" 'host)
                           (list '= "cpu" 'service) 
                           (list '= nil 'state))))

         (is (= (class-query ["awesome" "cache" "ok"] {})
                (list 'and (list '= "awesome" 'host)
                           (list '= "cache" 'service) 
                           (list '= "ok" 'state)))))
