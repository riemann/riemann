(ns riemann.pool)

(defmacro with-pool
  [[thingy pool timeout] & body]
  `(let [thingy# (claim ~pool ~timeout)
         ~thingy thingy#]
     (try
       (let [res# (do ~@body)]
         (release ~pool thingy#)
         res#)
       (catch Exception t#
         (invalidate ~pool thingy#)
         (throw t#)))))
