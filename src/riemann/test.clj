(ns riemann.test
  "Fast, end-to-end, repeatable testing for entire Riemann configs. Provides a
  `tap` macro which taps the event stream and (in testing mode) records all
  events that flow through that stream. Provides a variant of deftest that
  initiates controlled time and sets up a fresh result set for taps, and a
  function `inject!` to apply events to streams and see what each tap
  received."
  (:require [riemann.time.controlled :as time.controlled]
            [riemann.time :as time]
            [riemann.index :as index]
            [riemann.service :as service]
            [riemann.streams :as streams]
            [clojure.test :as test]))

; ugggggggh state is the worst
;  and yet necessary

(def ^:dynamic *testing*
  "Are we currently in test mode?"
  false)

(def ^:dynamic *results*
  "A map of tap names to atoms of vectors of captured events." nil)

(def ^:dynamic *taps*
  "An atom to a map of tap names to information about the taps; e.g. file and
  line number, for preventing collisions." nil)

(def ^:dynamic *core*
  "The core used in test mode"
  nil)

(defn tap-stream
  "Called by `tap` to construct a stream which records events in *results*
  before forwarding to child."
  [name child]
  (fn stream [event]
    ; Record event
    (-> *results*
        (get name)
        (swap! conj event))

    ; Forward downstream
    (child event)))

(defmacro tap
  "A stream which records inbound events in the *results* map. Takes a globally
  unique name which identifies that tap in *results*.

  When *testing* is false (at compile time!), tap has no effect; it compiles
  directly into (sdo child-streams).

  When *testing* is true at compile time, tap records any events that arrive in
  the results map, and passes those events on to its children.

  (rate 5 (tap :graph prod-graph-stream))"
  [name & children]
  (if *testing*
    (let [name (eval name)
          file *file*
          line (:line (meta &env))
          column (:column (meta &env))]
      (locking *taps*
        ; Make sure no other tap conflicts
        (when-let [extant (contains? @*taps* name)]
          (throw (RuntimeException.
                   (str "Tap " name " (" file ":" line ")"
                        " already defined at "
                        (:file extant) ":" (:line extant)))))

        ; Remember this tap so we can avoid defining duplicates
        (swap! *taps* assoc name {:file   file
                                  :line   line
                                  :column column})

        ; Return a stream
        `(tap-stream ~name (streams/sdo ~@children))))

    ; Fallback: compile directly to child streams
    `(streams/sdo ~@children)))

(defmacro io
  "A stream which suppresses side effects in test mode. When *testing* is true
  at compile time, returns a function that discards any incoming events. When
  *testing* is false, compiles to (sdo child1 child2 ...).

  (io
    (fn [e]
      (http/put \"http://my.service/callback\", {:body (event->json e)})))"
  [& children]
  (if *testing*
    `streams/bit-bucket
    `(streams/sdo ~@children)))

(defn fresh-results
  "Given a map of tap-names to _, builds a map of tap names to atoms
  of empty vectors, ready to receive events."
  [taps]
  (->> taps
       keys
       (reduce (fn [results tap-name]
                 (assoc! results tap-name (atom [])))
               (transient {}))
       persistent!))

(defmacro with-test-env
  "Prepares a fresh set of taps, binds *testing* to true, initiates controlled
  time and resets the schedulerand runs body in an implicit do. Wrap your entire
  test suite (including defining the streams themselves) in this macro. Note that
  you'll have to use (eval) or (load-file), etc, in order for this to work
  because the binding takes effect at *run time*, not *compile time*--so make
  your compile time run time and wow
  this gets confusing.

  (with-test-env
    (eval '(let [s (tap :foo prn)]
             (run-test! [s] [:hi]))))

  prints :hi, and returns {:foo [:hi]}"
  [& body]
  `(binding [*testing* true
             *taps*    (atom {})]
     (time.controlled/with-controlled-time!
       (time.controlled/reset-time!)
       ~@body)))

(defn inject!
  "Takes a sequence of streams, initiates controlled time and resets the
  scheduler, applies a sequence of events to those streams, and returns a map
  of tap names to the events each tap received. Absolutely NOT threadsafe;
  riemann.time.controlled is global. Streams may be omitted, in which case
  inject! applies events to the *streams* dynamic var."
  ([events]
   (inject! (:streams *core*) events))
  ([streams events]
   ;; Apply events
   (doseq [e events]
     (when-let [t (:time e)]
       (time.controlled/advance! t))

     (doseq [stream streams]
       (stream e)))
     ;; Return captured events

   (->> *results*
        (reduce (fn [results [tap-name results-atom]]
                  (assoc! results tap-name @results-atom))
                (transient {}))
        persistent!)))

(defn lookup
  "Lookup an event by host/service in a vector of tapped events returned by
  inject!. If several matching events have passed through the tap, the last one
  will be returned."
  [events host service]
  (last
    (filter
      identity
      (map #(when
              (and (= host (:host %))
                   (= service (:service %))) %)
           events))))

(defmacro deftest
  "Like clojure.test deftest, but establishes a fresh time context and a fresh
  set of tap results for the duration of the body.

  (deftest my-tap
    (let [rs (test/inject! [{:time 2 :service \"bar\"}])]
      (is (= 1 (count (:some-tap rs))))))"
  [name & body]
  `(test/deftest ~name
     (binding [*results* (fresh-results @*taps*)]
    (when (:index *core*)
      (index/clear (:index *core*)))
    (time.controlled/with-controlled-time!
      (time.controlled/reset-time!)
      (dorun (pmap #(riemann.service/reload! % *core*) (:services *core*)))
      (dorun (pmap service/start! (:services *core*)))
      ~@body
      (dorun (pmap service/stop! (:services *core*)))))))

(defmacro tests
  "Declares a new namespace named [ns]-test, requires some clojure.test and
  riemann.test helpers, and evaluates body in the context of that namespace.
  Restores the original namespace afterwards."
  [& body]
  (let [old-ns (ns-name *ns*)
        new-ns (symbol (str old-ns "-test"))]
    `(do (ns ~new-ns
           ~'(:require [riemann.test :refer [deftest inject! io tap run-stream lookup]]
                       [riemann.streams :refer :all]
                       [riemann.folds :as folds]
                       [pjstadig.humane-test-output :as output]
                       [clojure.test :refer [is are]]))
         (output/activate!)
         ~@body
         (ns ~old-ns))))

(defmacro run-stream
  "Applies inputs to stream, and returns outputs."
  [stream inputs]
  `(let [out# (atom [])
         stream# (~@stream (streams/append out#))]
     (time.controlled/reset-time!)
     (doseq [e# ~inputs]
       (when-let [t# (:time e#)]
         (time.controlled/advance! t#))
       (stream# e#))
     (deref out#)))

(defmacro run-stream-intervals
  "Applies a seq of alternating events and intervals (in seconds) between them
  to stream, returning outputs."
  [stream inputs-and-intervals]
  `(do
     (time.controlled/reset-time!)
     (let [out# (atom [])
           stream# (~@stream (streams/append out#))
           start-time# (ref (time/unix-time))
           next-time# (ref (deref start-time#))]
       (doseq [[e# interval#] (partition-all 2 ~inputs-and-intervals)]
         (stream# e#)
         (when interval#
           (dosync (ref-set next-time# (+ (deref next-time#) interval#)))
           (time.controlled/advance! (deref next-time#))))
       (let [result# (deref out#)]
         ; close stream
         (stream# {:state "expired" :time (time/unix-time)})
         result#))))

(defmacro test-stream
  "Verifies that the given stream, taking inputs, forwards outputs to children."
  [stream inputs outputs]
  `(test/is (~'= ~outputs (run-stream ~stream ~inputs))))

(defmacro with-test-stream
  "Exposes a fake index, verifies that the given stream, taking inputs,
  forwards outputs to children"
  [sym stream inputs outputs]
  `(let [out#    (atom [])
         ~sym    (streams/append out#)
         stream# ~stream]
     (doseq [e# ~inputs] (stream# e#))
     (test/is (~'= (deref out#) ~outputs))))

(defmacro test-stream-intervals
  "Verifies that run-stream-intervals, taking inputs/intervals, forwards
  outputs to children."
  [stream inputs-and-intervals outputs]
  `(test/is (~'= (run-stream-intervals ~stream ~inputs-and-intervals) ~outputs)))
