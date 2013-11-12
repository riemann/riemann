(ns riemann.test.buffer
  (:import (java.util.concurrent CountDownLatch
                                 TimeUnit))
  (:use riemann.buffer
        clojure.test
        [riemann.common :only [event]]
        [criterium.core :only [with-progress-reporting bench]]))

(defn random-event []
  (event {:time    (rand Long/MAX_VALUE)
          :service (rand 64)
          :host    (rand 64)}))

(defn benchmark [buffer]
  (with-progress-reporting
    (bench
      (dotimes [i 1000]
        (add! buffer (random-event))))))

(deftest csls-buffer-test
         (benchmark (csls-buffer)))
