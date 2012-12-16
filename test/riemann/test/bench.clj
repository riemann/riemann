(ns riemann.test.bench
  (:use riemann.core
        riemann.common
        riemann.logging
        riemann.transport.udp
        riemann.transport.tcp
        clojure.test
        clojure.java.shell
        [clojure.string :only [trim-newline]]
        incanter core charts
        [riemann.client :only [tcp-client udp-client close-client send-event]])
  (:require riemann.streams)

(defn git-version
  "Returns a human-readable version name for this commit."
  []
  (if (re-matches #"^\n*$" (:out (sh "git" "status" "-s")))
    ; Unchanged commit.
    (str
      (trim-newline (:out (sh "git" "show" "-s" "--format=%ci" "HEAD")))
      " "
      (trim-newline (:out (sh "git" "rev-parse" "HEAD" :out "UTF-8"))))

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

(defn record
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

(defn throughput
  "Returns [times, throughputs] of tape"
  ([tape opts]
   (let [[times latencies] tape
         samples (min (dec (count times)) (max 1 (or (:samples opts) 1000)))
         sample-size (/ (dec (count times)) samples)
         selected-times (take-nth sample-size times)
         throughputs (map (fn [[t1 t2]]
                            (/ sample-size (- t2 t1)))
                          (partition 2 1 selected-times))]
     [(drop-last selected-times) throughputs])))

(defn latencies
  [tape opts]
  tape)

(defn save-graph [graph opts]
  (let [file (str "bench/" (git-version-memo) "/" (:title opts) ".png")]
    (sh "mkdir" "-p" (str "bench/" (git-version-memo)))
    (save graph file :width 1024)
    (println "Wrote" file)))

(defn latency-graph
  "Graphs latencies, with options."
  ([tape] (latency-graph tape {}))
  ([tape opts]
   (let [title (str (:title opts) " latency")
         [times latencies] (latencies tape opts)]
     (doto (scatter-plot (map #(/ % 1000) times)
                         latencies
                         :title title
                         :x-label "Time (s)"
                         :y-label "Latency (ms)")
       (set-stroke :width 1) ; huh
       (save-graph {:title title})))))

(defn throughput-graph
  "Graphs throughput of tape, with options."
  ([tape] (throughput-graph tape {}))
  ([tape opts]
   (let [title (str (:title opts) " throughput")
         [times throughput] (throughput tape opts)]
     (doto (scatter-plot (map #(/ % 1000) times) 
                         (map (partial * 1000) throughput)
                         :title title
                         :x-label "Time (s)"
                         :y-label "Reqs/s")
       (save-graph {:title title})))))

(defn multigraph [f opts]
  (let [tape (record f opts)]
    (latency-graph tape opts)
    (throughput-graph tape opts)))

(defn core-package 
  ([] (core-package [{}]))
  ([opts]
   (let [servers [(tcp-server core) (udp-server core)]
         streams (or (:streams opts) [])
         core    (suppress "riemann.core"
                           (transition! (core) {:streams streams 
                                                :services servers}))]
     {:core core
      :servers servers
      :streams streams})))

(deftest ^:bench drop-tcp-events
         (let [{:keys [core]} (core-package)
               client (tcp-client)]
           (try
             (multigraph
               #(send-event client {:service "test" :metric 0.1})
               {:title "drop tcp events"
                :n 100000})
             (finally
               (stop! core)))))

(deftest ^:bench drop-udp-events
         (let [{:keys [core]} (core-package)
               client (udp-client)]
           (try
             (multigraph
               #(send-event client {:service "test" :metric 0.1} false)
               {:title "drop udp events"
                :n 100000})
             (finally
               (stop! core)))))

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
