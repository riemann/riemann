(ns riemann.common
  "Utility functions. Time/date, some flow control constructs, protocol buffer
  definitions and codecs, some vector set ops, etc."

  (:import [java.util Date]
           [com.aphyr.riemann Proto$Query Proto$Event Proto$Msg])
  (:require gloss.io
            clj-time.core
            clj-time.format
            clj-time.coerce
            clojure.set
            [clj-json.core :as json]
            [clojure.java.io :as io])
  (:use [clojure.string :only [split]]
        [riemann.time :only [unix-time]]
        clojure.tools.logging
        riemann.codec
        gloss.core
        clojure.math.numeric-tower))

; Times
(defn time-at 
  "Returns the Date of a unix epoch time."
  [unix-time]
  (java.util.Date. (long (* 1000 unix-time))))

(defn unix-to-iso8601
  "Transforms unix time to iso8601 string"
  [unix]
  (clj-time.format/unparse (clj-time.format/formatters :date-time)
                           (clj-time.coerce/from-long (long (* 1000 unix)))))

(defn post-load-event
  "After events are loaded, we assign default times if none exist."
  [e]
  (if (:time e) e (assoc e :time (unix-time))))

(defn decode
  "Decode a gloss buffer to a message. Decodes the protocol buffer
  representation of Msg and applies post-load-event to all events."
  [s]
  (let [buffer (gloss.io/contiguous s)
        bytes (byte-array (.remaining buffer))
        _ (.get buffer bytes 0 (alength bytes))
        msg (decode-pb-msg (Proto$Msg/parseFrom bytes))]
    (-> msg
      (assoc :states (map post-load-event (:states msg)))
      (assoc :events (map post-load-event (:events msg))))))

(defn decode-inputstream
  "Decode an InputStream to a message. Decodes the protobuf representation of
  Msg and applies post-load-event to all events."
  [s]
  (let [msg (decode-pb-msg (Proto$Msg/parseFrom s))]
    (-> msg 
      (assoc :states (map post-load-event (:states msg)))
      (assoc :events (map post-load-event (:events msg))))))

(defn ^"[B" encode
  "Builds and dumps a protobuf message from a hash. Applies pre-dump-event to
  events."
  [msg]
  (.toByteArray (encode-pb-msg msg)))

(defn event-to-json
  "Convert an event to a JSON string."
  [event]
  (json/generate-string 
    (assoc event :time (unix-to-iso8601 (:time event)))))

(defn decode-graphite-line
  "Decode a line coming from graphite.
   Graphite uses a simple scheme where each metric is given as a CRLF delimited
   line, space split with three items:

     * The metric name
     * The metric value (optionally NaN)
     * The timestamp

   By default, decode-graphite-line will yield a simple metric with just
   a service metric and timestamp, a parser-fn can be given to it, which
   will yield a map to merge onto the result. This can be used when
   graphite metrics have known patterns that you wish to extract more
   information (host, refined service name, tags) from"
  [line parser-fn]
   (when-let [[service metric timestamp] (split line #" ")]
     (when (not= metric "nan") ;; discard nan values
       {:ok true
        :states []
        :events [(let [res {:service service
                            :metric (Float. metric)
                            :time (Long. timestamp)}]
                   (if parser-fn (merge res (parser-fn res)) res))]})))
(defn event
  "Create a new event from a map."
  [opts]
  (let [t (long (round (or (opts :time)
                           (unix-time))))]
    (map->Event (merge opts {:time t}))))

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
  "Is r present in seqable s?"
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
