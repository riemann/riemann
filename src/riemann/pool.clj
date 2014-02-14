(ns riemann.pool
  "A generic thread-safe resource pool."
  (:use clojure.tools.logging
        [slingshot.slingshot :only [throw+]])
  (:import [java.util.concurrent LinkedBlockingQueue TimeUnit]))

; THIS IS A MUTABLE STATE OF AFFAIRS. WHICH IS TO SAY, IT IS FUCKING TERRIBLE.

(defprotocol Pool
  (grow [pool]
    "Adds an element to the pool.")
  (claim [pool] [pool timeout]
    "Take a thingy from the pool. Timeout in seconds; if unspecified, 0.
     Returns nil if no thingy available.")
  (release [pool thingy]
    "Returns a thingy to the pool.")
  (invalidate [pool thingy]
    "Tell the pool a thingy is no longer valid."))

(defrecord FixedQueuePool [queue open close regenerate-interval]
  Pool
  (grow [this]
        (loop []
          (if-let [thingy (try (open) (catch Throwable t nil))]
            (.put ^LinkedBlockingQueue queue thingy)
            (do
              (Thread/sleep (* 1000 regenerate-interval))
              (recur)))))

  (claim [this]
         (claim this nil))

  (claim [this timeout]
         (let [timeout (* 1000 (or timeout 0))]
           (or
             (try
               (.poll ^LinkedBlockingQueue queue timeout TimeUnit/MILLISECONDS)
               (catch java.lang.InterruptedException e
                 nil))
             (throw+
               {:type ::timeout
                :message (str "Couldn't claim a resource from the pool within "
                              timeout " ms")}))))

  (release [this thingy]
           (when thingy
             (.put ^LinkedBlockingQueue queue thingy)))

  (invalidate [this thingy]
              (when thingy
                (try (close thingy)
                  (catch Throwable t
                    (warn t "Closing" thingy "threw")))
                (future (grow this)))))

(defn fixed-pool
  "A fixed pool of thingys. (open) is called to generate a thingy. (close
  thingy) is called when a thingy is invalidated. When thingys are invalidated,
  the pool will immediately try to open a new one; if open throws or returns
  nil, the pool will sleep for regenerate-interval seconds before retrying
  (open).

  :regenerate-interval    How long to wait between retrying (open).
  :size                   Number of thingys in the pool.
  :block-start            Should (fixed-pool) wait until the pool is full
                          before returning?

  Note that fixed-pool is correct only if every successful (claim) is followed
  by exactly one of either (invalidate) or (release). If calls are unbalanced;
  e.g. resources are not released, doubly released, or released *and*
  invalidated, starvation or unbounded blocking could occur. (with-pool)
  provides this guarantee."
  ([open]
   (fixed-pool open {}))
  ([open opts]
   (fixed-pool open identity opts))
  ([open close opts]
   (let [^int size            (or (:size opts) (* 2 (.availableProcessors
                                                      (Runtime/getRuntime))))
         regenerate-interval  (or (:regenerate-interval opts) 5)
         block-start          (get opts :block-start true)
         pool (FixedQueuePool.
                (LinkedBlockingQueue. size)
                open
                close
                regenerate-interval)
         openers (doall
                   (map (fn open-pool [_]
                          (future (grow pool)))
                        (range size)))]
     (when block-start
       (doseq [worker openers] @worker))
     pool)))

(defmacro with-pool
  "Evaluates body in a try expression with a symbol 'thingy claimed from the
  given pool, with specified claim timeout. Releases thingy at the end of the
  body, or if an exception is thrown, invalidates them and rethrows. Example:

  ; With client, taken from connection-pool, waiting 5 seconds to claim, send
  ; client a message.
  (with-pool [client connection-pool 5]
    (send client a-message))"
  [[thingy pool timeout] & body]
  ; Destructuring bind could change nil to a, say, vector, and cause
  ; unbalanced claim/release.
  `(let [thingy# (claim ~pool ~timeout)
         ~thingy thingy#]
     (try
       (let [res# (do ~@body)]
         (release ~pool thingy#)
         res#)
       (catch Throwable t#
         (invalidate ~pool thingy#)
         (throw t#)))))
