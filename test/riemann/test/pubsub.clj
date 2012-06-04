(ns riemann.test.pubsub
  (:use riemann.pubsub)
  (:use clojure.test))

(defn pusher [out]
  "push events onto x"
  (fn [x] (dosync (alter out conj x))))

(deftest one-to-one
         (let [r   (pubsub-registry)
               out (ref [])
               id (subscribe r :foo (pusher out))]

           (publish r :foo 1)
           (publish r :foo 2)
           (is (= (deref out) [1 2]))))

(deftest one-to-many
         (let [r   (pubsub-registry)
               out1 (ref [])
               out2 (ref [])
               id1 (subscribe r :foo (pusher out1))
               id2 (subscribe r :foo (pusher out2))]

           (publish r :foo 1)
           (publish r :foo 2)
           (is (= (deref out1) (deref out2) [1 2]))))

(deftest unsub
         (let [r    (pubsub-registry)
               out1 (ref [])
               out2 (ref [])
               foo1 (subscribe r :foo (pusher out1))
               foo2 (subscribe r :foo (pusher out2))]
               
           (publish r :foo 1)
          
           ; Unsub with channel
           (unsubscribe r :foo foo1)
           (publish r :foo 2)

           ; Unsub without channel
           (unsubscribe r foo2)
           (publish r :foo 3)

           (is (= (deref out1) [1]))
           (is (= (deref out2) [1 2]))))
