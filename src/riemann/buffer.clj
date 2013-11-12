(ns riemann.buffer
  "A mutable, deterministically time-ordered buffer of events, designed to
  accumulate writes before being applied in-order to streams."
  (:import (java.util Comparator)
           (java.util.concurrent ConcurrentSkipListSet)))

(defmacro order
  "Given two elements a and b, and a list of forms to apply to each, expands to
  an expression which orders a and b by each of forms, in order of decreasing
  precedence.

  Compare a and b by (compare (first a) (first b)), then (compare (second a)
  (second b)), then (compare (count a) (count b)).

  (ordering a b first second count)"
  [a b & forms]
  (if-let [form (first forms)]
    `(let [x# (compare (~form ~a) (~form ~b))]
       (if (zero? x#)
         (order ~a ~b ~@(rest forms))
         x#))
    0))

(defn ^java.util.Comparator compare-events
  "A comparator for event order."
  [e1 e2]
  (order e1 e2 :time :host :service :id))


(defprotocol Buffer
  (add! [this event] "Adds an event to the buffer."))

(defrecord CSLSBuffer [^ConcurrentSkipListSet a-set]
  Buffer
  (add! [this event] (.add a-set event)))

;  clojure.lang.Seqable
;  (seq [this]
;       (seq a-set))

(defn csls-buffer
  "A buffer backed by a ConcurrentSkipList"
  []
  (CSLSBuffer. (ConcurrentSkipListSet. compare-events)))
