(ns riemann.test.pubsub
  (:use riemann.pubsub
        clojure.test)
  (:require [riemann.logging :as logging]))

(deftest one-to-one
         (let [r   (pubsub-registry)
               out (atom [])
               id (subscribe! r :foo #(swap! out conj %))]

           (publish! r :foo 1)
           (publish! r :foo 2)
           (is (= @out [1 2]))))

(deftest one-to-many
         (let [r   (pubsub-registry)
               out1 (atom [])
               out2 (atom [])
               id1 (subscribe! r :foo #(swap! out1 conj %))
               id2 (subscribe! r :foo #(swap! out2 conj %))]

           (publish! r :foo 1)
           (publish! r :foo 2)
           (is (= @out1 @out2 [1 2]))))

(deftest unsub
         (let [r    (pubsub-registry)
               out1 (atom [])
               out2 (atom [])
               foo1 (subscribe! r :foo #(swap! out1 conj %))
               foo2 (subscribe! r :foo #(swap! out2 conj %))]
               
           (publish! r :foo 1)
          
           (unsubscribe! r foo1)
           (publish! r :foo 2)

           (unsubscribe! r foo2)
           (publish! r :foo 3)

           (is (= @out1 [1]))
           (is (= @out2 [1 2]))))

(deftest sweep-test
         (let [r (pubsub-registry)
               pers (atom [])
               temp (atom [])]
           (subscribe! r :foo #(swap! pers conj %) true)
           (subscribe! r :foo #(swap! temp conj %))

           (publish! r :foo 1)
           (logging/suppress "riemann.pubsub"
             (sweep! r))
           (publish! r :foo 2)

           (is (= @pers [1 2]))
           (is (= @temp [1]))))
