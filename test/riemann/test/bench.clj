(ns riemann.test.bench
  (:use [riemann.core])
  (:use [riemann.common])
  (:use [riemann.server])
  (:use [riemann.client :only [tcp-client close-client send-event]])
  (:require [riemann.streams])
  (:use [clojure.test])
  (:use [clojure.java.shell])
  (:use [incanter core stats charts]))

(defn git-version
  "Returns a human-readable version name for this commit."
  []
  (if (re-matches #"^\r+$" (:out (sh "git" "status" "-s")))
    ; Unchanged commit.
    (str
      (:out (sh "git" "show" "-s" "--format=\"%ci\"" "HEAD"))
      " "
      (:out (sh "git" "rev-parse" "HEAD" :out "UTF-8")))

    ; Changed commit.
    "HEAD"))
(def git-version-memo (memoize git-version))

(defn now
  "Current high-res time, in ms"
  []
  (/ (. System nanoTime) 1000000.0))

(defmacro time* 
  "Evaluates expr and returns the time it took in ms"
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     (/ (- (. System (nanoTime)) start#) 1000000.0)))

(defn latencies
  "Returns [times, latencies] of calling f"
  ([f opts]
   (let [n    (or (:n opts) 100)
         t0   (now)]
     (loop [i n
            times []
            latencies []]
       (if (zero? i)
         [times latencies]
         (recur (dec i)
                (conj times (- (now) t0))
                (conj latencies (time* (f)))))))))

(defn latency-graph
  "Graphs throughput of repeated invocations of f, with options."
  ([f] (latency-graph f {}))
  ([f opts]
   (let [title (or (:title opts) "latency over time")
         file (str "bench/" (git-version-memo) "/" title ".png")
         [times latencies] (latencies f opts)]
     (sh "mkdir" "-p" (str "bench/" (git-version-memo)))
     (doto (scatter-plot times latencies
                         :title title
                         :x-label "Time (ms)"
                         :y-label "Latency (ms)")
       (set-stroke :width 1 ) ; huh
       (save file :width 1024))
     (println "Wrote " file))))

(defn core-package 
  ([] (core-package [{}]))
  ([opts]
   (let [core (core)
         servers [(tcp-server core)]
         streams (or (:streams opts) [])]
     (dosync
        (alter (core :servers) concat servers)
        (alter (core :streams) concat streams))
     {:core core
      :servers servers
      :streams streams})))

  (deftest drop-latency
         (let [{:keys [core]} (core-package)
               client (tcp-client)]
           (try
             (latency-graph (partial send-event client 
                                                 {:service "test"
                                                  :metric 0.1})
                                     {:title "drop events"
                                      :n 10000})
             (finally
               (stop core)))))

(comment
(deftest sum-test
         (let [final (ref nil)
               core (core)
               server (tcp-server core)
               stream (sum (register final))
               n 100
               threads 10
               events (take n (repeatedly (fn [] 
                        {:metric 1})))]

           (dosync
             (alter (core :servers) conj server)
             (alter (core :streams) conj stream))

           (doall events)

           (try 
             (time (threaded threads
                             (let [client (tcp-client)]
                                (doseq [e events]
                                  ; Send all events to server
                                  (send-event client e))
                               (close-client client))))
             
            (is (= (* threads n) (:metric (deref final)))) 

            (finally
              (stop core))))))
