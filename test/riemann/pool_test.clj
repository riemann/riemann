(ns riemann.pool-test
  (:use riemann.pool
        [slingshot.slingshot :only [try+]]
        clojure.test))

(deftest claim-release-test
         (let [x (atom 0)
               pool (fixed-pool #(swap! x inc) {:size 2 :block-start true})
               ; Claim both elements
               a  (claim pool)
               b  (claim pool)
               ; Pool is empty; should throw
               c  (try+ (claim pool 2/1000)
                        (catch [:type :riemann.pool/timeout]
                          {:keys [message]}
                          message))
               a' (release pool a)
               ; Should re-acquire a
               d  (claim pool)
               ; Empty
               e  (try+ (claim pool)
                        (catch [:type :riemann.pool/timeout] _ :timeout))
               b' (release pool b)
               ; Re-acquire b
               f  (claim pool)]
           (is (= #{1 2} #{a b}))
           (is (= c "Couldn't claim a resource from the pool within 2 ms"))
           (is (= a d))
           (is (= :timeout e))
           (is (= b f))
           ; Shouldn't have (open)'d more than twice.
           (is (= 2 @x))))

(deftest claim-invalidate-test
         (let [x (atom 0)
               pool (fixed-pool #(swap! x inc) {:size 2 :block-start true})
               a  (claim pool)
               a' (invalidate pool a)
               b  (claim pool)

               b' (invalidate pool b)
               c  (claim pool 1)
               d  (claim pool 1)
               e  (try (claim pool 1) (catch Exception e nil))
               c' (invalidate pool c)
               d' (invalidate pool d)
               ; Invalidate nil should be a noop
               e' (invalidate pool e)]
           ; Wait for futures.
           (is a')
           (is b')
           (is c')
           (is d')
           (is (nil? e'))

           (dorun (map deref [a' b' c' d']))

           (is #{1 2} a)
           (is #{1 2 3} b)
           (is (= #{1 2 3 4} #{a b c d}))
           (is (nil? e))
           ; Should have opened twice to start and 4 times after invalidations.
           (is (= 6 @x))))

(deftest with-pool-test
         (let [x (atom 0)
               pool (fixed-pool #(swap! x inc) {:size 1 :block-start true})]

           ; Regular claim
           (let [a (with-pool [a pool] a)]
             (is (= 1 a))
             (is (= 1 @x)))

           ; With-pool should have released.
           (let [a (claim pool)]
             (is (= 1 a))
             (release pool a))

           ; Throwing errors
           (is (thrown? RuntimeException
                        (with-pool [b pool]
                                   (is (= 1 b))
                                   (throw (RuntimeException. "whoops")))))

           ; Pool should have regenerated.
           (Thread/sleep 250)
           (is (= 2 @x))))

(deftest ^:time unreliable-test
         (let [x (atom 0)
               size 5
               got-client (atom 0)
               no-client (atom 0)
               opens (atom 0)
               open-attempts (atom 0)
               open-failures (atom 0)
               invalidations (atom 0)
               closes (atom 0)
               pool (fixed-pool
                      (fn []
                        (swap! open-attempts inc)
                        (if (< 0.5 (rand))
                          (swap! opens inc)
                          (do
                            (swap! open-failures inc)
                            (throw (RuntimeException.)))))
                      (fn [_] (swap! closes inc))
                      {:size size
                       :regenerate-interval 0.1})]

           (let [workers 
                 (map (fn [_]
                        (future
                          (dotimes [i 100]
                            (try
                              (with-pool [x pool 0.1]
                                         (if x
                                           (do
                                             (when (< (rand) 0.1)
                                               (swap! invalidations inc)
                                               (throw (RuntimeException.)))
                                             (swap! got-client inc))
                                           (swap! no-client inc)))
                              (catch RuntimeException t)))))
                      (range 10))]
             (doseq [w workers] @w))

           ; Some of the time, multiple retries were needed.
           (is (< @invalidations (+ size @opens)))
           
           ; The pool should have made progress.
           (is (< 0 @got-client))

           ; Most of the runs had a client, but not all.
           (is (< 0 @no-client (/ @got-client 5)))

           ; Every invalidated client was closed.
           (is (= @closes @invalidations))
          
           ; The number of open clients did not exceed size.
           (is (<= 0 (- @opens @closes) size))
           
           ; Invalidations occurred.
           (is (< 0 @invalidations))

           ; Failed opens occurred.
           (is (< 0 @open-failures))

           ; Every invalidation and every failure opening led to another open
           ; (except for up to size futures in progress).
           (is (<= 0
                   (- @open-attempts @invalidations @open-failures)
                   size))                   

           ; Far fewer clients were opened than used.
           (is (< @opens (/ @got-client 5)))

           (prn @got-client "runs had a client")
           (prn @no-client "runs had no client")
           (prn @invalidations "clients were invalidated")
           (prn @opens "opened clients")
           (prn @closes "closed clients")
           (prn @open-failures "failures opening clients")
           (prn @open-attempts "attempts to open a client")))
