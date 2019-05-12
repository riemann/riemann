(ns riemann.expiration-test
  (:require [riemann.expiration :refer :all]
            [riemann.time :refer [unix-time]]
            [riemann.time.controlled :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :once control-time!)
(use-fixtures :each reset-time!)

(deftest expiration-time-test
  ; Uncomment when we put in strict time requirements
;  (testing "no time or ttl"
;    (is (thrown? java.lang.AssertionError (expiration-time {}))))

;  (testing "no time"
;    (is (thrown? java.lang.AssertionError (expiration-time {:ttl 5}))))

  (testing "no ttl"
    (is (= Double/POSITIVE_INFINITY (expiration-time {:time 5}))))

  (testing "both time and ttl"
    (is (= 20 (expiration-time {:time 15 :ttl 5}))))

  (testing "expired already"
    (is (= 10 (expiration-time {:state "expired" :time 10})))))

(deftest expired?-test
  (advance! 10)
  (is (expired? {:time 5, :ttl 2}))
  (is (not (expired? {:time 5, :ttl 5})))
  (is (expired? {:time 5, :ttl 5, :state "expired"})))

(defmacro with-tracker
  "Resets time, binds tracker to the given symbol, evals body, shuts down
  tracker."
  [[tracker-name tracker-expr] & body]
  `(try
     (reset-time!)
     (let [~tracker-name ~tracker-expr]
       (try
         ~@body
         (finally (shutdown! ~tracker-name))))))

(deftest tracker-test
  (testing "simple expiration"
    (let [exp (atom nil)]
      (with-tracker [t (tracker! (partial reset! exp))]
        (update! t {:host :h, :service :s, :time 0, :ttl 1})
        (advance! 1)
        (is (= nil @exp))
        (advance! 2)
        (is (= {:host :h, :service :s, :time 1, :state "expired"} @exp))))

    (testing "delaying expiration"
      (let [exp (atom nil)]
        (with-tracker [t (tracker! (partial reset! exp))]
          (is (= 0 (unix-time)))
          (update! t {:host :h, :service :s, :time 0, :ttl 1})
          (advance! 1)
          (is (= nil @exp))
          (update! t {:host :h, :service :s, :time 1, :ttl 1})
          (advance! 2)
          (is (= nil @exp))
          (advance! 3)
          (is (= {:host :h, :service :s, :time 2, :state "expired"} @exp)))))

    (testing "overriding with an immediate expiration"
      (let [exp (atom nil)]
        (with-tracker [t (tracker! (partial reset! exp))]
          (is (= 0 (unix-time)))
          (update! t {:host :h, :service :s, :time 0, :ttl 5})
          (advance! 1)
          (is (= nil @exp))
          (update! t {:host :h, :service :s, :time 1, :ttl 1})
          (advance! 2)
          (is (= nil @exp))
          (advance! 3)
          (is (= {:host :h, :service :s, :time 2, :state "expired"} @exp)))))))
