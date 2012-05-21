(ns riemann.common
  "Utility functions. Time/date, some flow control constructs, protocol buffer
  definitions and codecs, some vector set ops, etc."

  (:import [java.util Date])
  (:require gloss.io)
  (:require clojure.set)
  (:require [clojure.java.io :as io])
  (:use clojure.tools.logging)
  (:use protobuf.core)
  (:use gloss.core)
  (:use clojure.math.numeric-tower))

; Don't mangle underscores into dashes. <sigh>
(protobuf.core.PersistentProtocolBufferMap/setUseUnderscores true)

; Protobufs
(def Msg (protodef com.aphyr.riemann.Proto$Msg))
(def Query (protodef com.aphyr.riemann.Proto$Query))
(def State (protodef com.aphyr.riemann.Proto$State))
(def Event (protodef com.aphyr.riemann.Proto$Event))

; Few flow control things
(defmacro threaded [thread-count & body]
  `(let [futures# (map (fn [_#] (future ~@body))
                      (range 0 ~thread-count))]
    (doseq [fut# futures#] (deref fut#))))

(defn ppmap [threads f s]
  (let [work (partition (/ (count s) threads) s)
        result (pmap (fn [part] (doall (map f part))) work)]
    (doall (apply concat result))))

; Times
(defn unix-time
  "The current unix epoch time in seconds, taken from System/currentTimeMillis."
  []
  (/ (System/currentTimeMillis) 1000))

(defn time-at 
  "Returns the Date of a unix epoch time."
  [unix-time]
  (java.util.Date. (long unix-time)))

(defn pre-dump-event
  "Transforms an event (map) into a form suitable for protocol buffer encoding."
  [e]
  (let [e (if (:metric e) (assoc e :metric_f (float (:metric e))) e)
        e (if (:ttl e)    (assoc e :ttl      (float (:ttl e)))    e)
        e (if (:time   e) (assoc e :time     (int   (:time e)))   e)]
    e))

(defn post-load-event 
  "Loads a protobuf event to an internal event. Converts the on-the-wire
  metric_f to metric, creates a time if none exists, etc."
  [e]
  (let [e (apply hash-map (apply concat e))
        e (if (:metric_f e) (assoc e :metric (:metric_f e)) e)
        e (dissoc e :metric_f)
        e (if (:time e) e (assoc e :time (unix-time)))]
    e))

; These decode-pb functions duplicate the work clojure-protobuf does; suspect
; they're slightly faster. They're needed to translate the raw protobuf classes
; that riemann.client uses back into clojure structures. Should unify these
; paths later.

(defn decode-pb-query
  "Transforms a java protobuf Query to a map."
   [^com.aphyr.riemann.Proto$Query q]
   {:string (.getString q)})

(defn decode-pb-event
  "Transforms a java protobuf Event to a map."
  [^com.aphyr.riemann.Proto$Event e]
  (let [rough
        {:host (when (.hasHost e) (.getHost e))
         :service (when (.hasService e) (.getService e))
         :state (when (.hasState e) (.getState e))
         :description (when (.hasDescription e) (.getDescription e))
         :metric (when (.hasMetricF e) (.getMetricF e))
         :tags (when (< 0 (.getTagsCount e)) (vec (.getTagsList e)))
         :time (if (.hasTime e) (.getTime e) (unix-time))
         :ttl (when (.hasTtl e) (.getTtl e))}]
    (select-keys rough (for [[k v] rough :when (not= nil v)] k))))

(defn decode-pb-msg
  "Transforms a java protobuf Msg to a map."
  [^com.aphyr.riemann.Proto$Msg m]
  {:ok (.getOk m)
   :error (.getError m)
   :events (map decode-pb-event (.getEventsList m))
   :query (decode-pb-query (.getQuery m))})

(defn decode
  "Decode a gloss buffer to a message. Decodes the protocol buffer
  representation of Msg and applies post-load-event to all events."
  [s]
  (let [buffer (gloss.io/contiguous s)
        bytes (byte-array (.remaining buffer))
        _ (.get buffer bytes 0 (alength bytes))
        msg (protobuf-load Msg bytes)]
    ; Can't use a protobuf Msg here--it would coerce events and drop our
    ; metric keys.
    {:ok (:ok msg)
          :error (:error msg)
          :states (map post-load-event (:states msg))
          :query (:query msg)
          :events (map post-load-event (:events msg))}))

(defn decode-inputstream
  "Decode an InputStream to a message. Decodes the protobuf representation of
  Msg and applies post-load-event to all events."
  [s]
  (let [msg (protobuf-load-stream Msg s)]
    ; Can't use a protobuf Msg here--it would coerce events and drop our
    ; metric keys.
    {:ok (:ok msg)
          :error (:error msg)
          :states (map post-load-event (:states msg))
          :query (:query msg)
          :events (map post-load-event (:events msg))}))

(defn ^"[B" encode
  "Builds and dumps a protobuf message from a hash. Applies pre-dump-event to
  events."
  [msg]
  (let [msg (merge msg
                   {:events (map pre-dump-event (:events msg))
                    :states (map pre-dump-event (:states msg))})
        pb (apply protobuf Msg (apply concat msg))]
    (protobuf-dump pb)))

(defn event
  "Create a new event."
  [opts]
  (let [t (long (round (or (opts :time)
                           (unix-time))))]
    (apply protobuf Event
           (apply concat (merge opts {:time t})))))

(defn approx-equal
  "Returns true if x and y are roughly equal, such that x/y is within tol of
  unity."
([x,y]
  (approx-equal x y 0.01))
([x, y, tol]
  (if (= x y) true
    (let [f (try (/ x y) (catch java.lang.ArithmeticException e (/ y x)))]
      (< (- 1 tol) f (inc tol))))))

(defn re-matches?
  "Does the given regex match string? Nil if string is nil."
  [re string]
  (when string
    (re-find re string)))

; Vector set operations
(defn member?
  "Is e present in seqable s?"
  [r s]
  (some (fn [e] (= r e)) s))

(defn subset?
  "Are all elements of required present in seqable s?"
  [required s]
  (clojure.set/subset? (set required) (set s)))

(defn overlap?
  "Do a and b (any seqables) have any elements in common?"
  [a b]
  (some (fn [e]
          (some (fn [r] (= e r)) a)) b))

(defn disjoint?
  "Do a and b (any seqables) have no elements in common?"
  [a b]
  (not-any? (fn [e] 
             (some (fn [r] (= e r)) a))
           b))
