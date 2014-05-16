(ns riemann.common
  "Utility functions. Time/date, some flow control constructs, protocol buffer
  definitions and codecs, some vector set ops, etc."
  (:import [java.util Date]
           (java.io InputStream)
           [com.aphyr.riemann Proto$Query Proto$Event Proto$Msg]
           [java.net InetAddress])
  (:require gloss.io
            clj-time.core
            clj-time.format
            clj-time.coerce
            clojure.set
            [cheshire.core :as json]
            [clojure.java.io :as io])
  (:use [clojure.string :only [split join]]
        [riemann.time :only [unix-time]]
        [clojure.java.shell :only [sh]]
        clojure.tools.logging
        riemann.codec
        gloss.core
        clojure.math.numeric-tower))

(defprotocol Match
  (match [pred object]
    "Does predicate describe object?"))

; Deprecation
(defmacro deprecated
  "Wraps body in an implicit (do), and logs a deprecation notice when invoked."
  [comment & body]
  `(do
     (info ~(str "Deprecated: "
                 (format "<%s:%s> " *file* (:line (meta &form)))
                 comment))
     ~@body))

(def hostname-refresh-interval
  "How often to allow shelling out to hostname (1), in seconds."
  60)

(defn get-hostname
  "Fetches the hostname by shelling out to hostname (1), whenever the given age
  is stale enough. If the given age is recent, as defined by
  hostname-refresh-interval, returns age and val instead."
  [[age val]]
  (if (and val (<= (* 1000 hostname-refresh-interval)
                   (- (System/currentTimeMillis) age)))
   [age val]
   [(System/currentTimeMillis)
    (let [{:keys [exit out]} (sh "hostname")]
      (if (= exit 0)
        (.trim out)))]))

; Platform
(let [cache (atom [nil nil])]
  (defn localhost
    "Returns the local host name."
    []
    (if (re-find #"^Windows" (System/getProperty "os.name"))
      (or (System/getenv "COMPUTERNAME") "localhost")
      (or (System/getenv "HOSTNAME")
          (second (swap! cache get-hostname))
          "localhost"))))

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

(defn iso8601->unix
  "Transforms ISO8601 strings to unix timestamps."
  [iso8601]
  (-> (->> iso8601
          (clj-time.format/parse (:date-time-parser clj-time.format/formatters))
          (clj-time.coerce/to-long))
      (/ 1000)
      long))

; Events
(defn post-load-event
  "After events are loaded, we assign default times if none exist."
  [e]
  (if (:time e) e (assoc e :time (unix-time))))

(defn decode-msg
  "Decode a protobuf to a message. Decodes the protocol buffer
  representation of Msg and applies post-load-event to all events."
  [msg]
  (let [msg (decode-pb-msg msg)]
    (-> msg
      (assoc :states (map post-load-event (:states msg)))
      (assoc :events (map post-load-event (:events msg))))))

(defn decode-inputstream
  "Decode an InputStream to a message. Decodes the protobuf representation of
  Msg and applies post-load-event to all events."
  [^InputStream s]
  (let [msg (decode-pb-msg (Proto$Msg/parseFrom s))]
    (-> msg
      (assoc :states (map post-load-event (:states msg)))
      (assoc :events (map post-load-event (:events msg))))))

(defn ^"[B" encode
  "Builds and dumps a protobuf message as bytes from a hash."
  [msg]
  (.toByteArray (encode-pb-msg msg)))

(defn expire
  "An expired version of an event."
  [event]
  (into (select-keys event [:host :service])
        [[:time (unix-time)]
         [:state "expired"]]))

(defn event-to-json
  "Convert an event to a JSON string."
  [event]
  (json/generate-string
    (assoc event :time (unix-to-iso8601 (:time event)))))

(defn ensure-event-time
  "Ensures an event has a timestamp."
  [e]
  (assoc e :time (if-let [t (:time e)]
                   (iso8601->unix t)
                   (unix-time))))

(defn event
  "Create a new event from a map."
  [opts]
  (let [t (long (round (or (opts :time)
                           (unix-time))))]
    (map->Event (merge opts {:time t}))))

(defn exception->event
  "Creates an event from an Exception."
  [^Throwable e]
  (map->Event {:time (unix-time)
               :service "riemann exception"
               :state "error"
               :tags ["exception" (.getName (class e))]
               :description (str e "\n\n"
                                 (join "\n" (.getStackTrace e)))}))

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

(defn map-matches?
  "Does the given map pattern match obj?"
  [pat obj]
    (every? (fn [[k v]] (match v (get obj k))) pat))

; Matching
(extend-protocol Match
  ; Regexes are matched against strings.
  java.util.regex.Pattern
  (match [re string]
         (try (re-find re string)
           (catch NullPointerException _ false)
           (catch ClassCastException _ false)))

  ; Functions are called with the given object.
  java.util.concurrent.Callable
  (match [f obj]
         (f obj))

  ; Map types 
  clojure.lang.PersistentArrayMap
  (match [pat obj] (map-matches? pat obj))

  clojure.lang.PersistentHashMap
  (match [pat obj] (map-matches? pat obj))

  clojure.lang.PersistentTreeMap
  (match [pat obj] (map-matches? pat obj))

  ; Falls back to object equality
  java.lang.Object
  (match [pred object]
         (= pred object))

  ; Nils match nils only.
  nil
  (match [_ object]
    (nil? object)))

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

(defn middle
  "Takes the element at the middle of a seq."
  [s]
  (if (empty? s)
    nil
    (nth s (/ (count s) 2))))

; composing human-readable messages
(defn human-uniq
  "Returns a human-readable string describing things, e.g.

  importer
  api1, api2, api4
  23 services"
  [things, type]
  (let [things (distinct things)]
    (case (count things)
      0 nil
      1 (first things)
      2 (str (first things) " and " (nth things 1))
      3 (join ", " things)
      4 (join ", " things)
      (str (count things) " " type))))

(defn subject
  "Constructs a subject line for a set of events."
  [events]
  (join " " (keep identity
        [(human-uniq (map :host events) "hosts")
         (human-uniq (map :service events) "services")
         (human-uniq (map :state events) "states")])))

(defn custom-attributes
  "Returns a Map of the custom attributes of an Event."
  [event]
  (let [attribute-keys (filter (complement event-keys) (keys event))]
    (select-keys event attribute-keys)))

(defn body
  "Constructs a message body for a set of events."
  [events]
  (join "\n\n\n"
        (map
          (fn [event]
            (str
              "At " (time-at (:time event)) "\n"
              (:host event) " "
              (:service event) " "
              (:state event) " ("
              (if (ratio? (:metric event))
                (double (:metric event))
                (:metric event)) ")\n"
              "Tags: [" (join ", " (:tags event)) "]"
              "\n"
              "Custom Attributes: " (custom-attributes event)
              "\n\n"
              (:description event)))
          events)))

(defn count-string-bytes [s]
  (count (.getBytes ^String s "UTF8")))

(defn count-character-bytes [^Character c]
  (count-string-bytes (.toString c)))

(defn truncate [^String s n]
  (if (<= n 0)
    ""
    (if (> (count s) n)
      (.substring s 0 n)
      s)))

(defn truncate-bytes [s n]
  (let [summed (reduce
                (fn [memo v]
                  (if (> (:sum memo) n)
                    memo
                    {:sum (+ (:sum memo) (count-character-bytes v))
                     :i (inc (:i memo))}))
                {:sum 0 :i 0}
                s)
        cutoff (if (> (:sum summed) n)
            (dec (:i summed))
            (:i summed))]
    (truncate s cutoff)))
