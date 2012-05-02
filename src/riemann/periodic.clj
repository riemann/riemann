(ns riemann.periodic
  "Calls functions at regular intervals."
  (:import (java.util.concurrent Executors TimeUnit))
  (:use riemann.common))

(defn every
  "Calls f every interval seconds, until stopped. Returns a fn which stops."
  ([f] (every 1 0 f))
  ([interval f] (every interval 0 f))
  ([interval delay f]
   (let [s (Executors/newSingleThreadScheduledExecutor)]
     (.scheduleAtFixedRate s f 
                           (long (* 1000 delay)) 
                           (long (* 1000 interval))
                           TimeUnit/MILLISECONDS)
     (fn [] (.shutdownNow s)))))

(defn after
  "Calls f after interval seconds."
  ([f] (after 1 f))
  ([delay f]
   (let [s (Executors/newSingleThreadScheduledExecutor)]
     (.schedule s f (long (* 1000 delay)) TimeUnit/MILLISECONDS))))

(defn deferrable-every
  "Calls f every interval seconds, until stopped. Returns a map of fns {:stop,
  :defer). Can be deferred. I'll probably change this API."
  ; Ughhhhhh. I'm so sorry.
  ([interval delay f]
   (let [waterline (ref (+ delay (unix-time)))
         thread (Thread. 
                  (bound-fn []
                    (loop []
                      (when-let [target-time (deref waterline)]
                        (let [now (unix-time)
                              delay (- target-time now)]
                          (if (pos? delay)
                            ; Sleep
                            (Thread/sleep (* 1000 delay))
                            ; Run
                            (do
                              (dosync (ref-set waterline (+ interval now)))
                              (f)))
                          (recur))))))]
     (.start thread)
     {:stop (fn [] (dosync (ref-set waterline nil)))
      :defer (fn [seconds] 
               (dosync (ref-set waterline (+ (unix-time) seconds))))})))
