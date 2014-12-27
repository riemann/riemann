(ns riemann.test-utils
  "Utilities for writing tests.")

(defn stub
  "Returns a fn that records its arguments in an atom, using conj. Always
  returns ::stub."
  [a]
  (fn [& args]
    (swap! a conj args)
    ::stub))

(defmacro with-mock
  "For the duration of the body, turns var-symbol into a stub which simply
  records its arguments as a seq in an atom. That atom is bound to `a-sym` for
  the duration of the body. You can deref `a-sym` at any point to find a vector
  of argument lists, one for each call made to the var. Calls to var will
  return ::stub.

  (with-mock [x class] (class 3 5) (class 6) @x)
  ; => [(3 5) (6)]"
  [[a-sym var-symbol] & body]
  `(let [~a-sym (atom [])]
     (with-redefs [~var-symbol (stub ~a-sym)]
       ~@body)))
