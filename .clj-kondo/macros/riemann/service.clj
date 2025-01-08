(ns riemann.service)

(defmacro all-equal?
  [a b & forms]
  (let [asym (gensym "a__")
        bsym (gensym "b__")]
  `(let [~asym ~a
         ~bsym ~b]
     (and ~@(map (fn [[fun & args]]
                  `(= (~fun ~asym ~@args) (~fun ~bsym ~@args)))
                forms)))))
