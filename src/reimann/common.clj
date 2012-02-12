(ns reimann.common
  (:use [protobuf.core])
  (:import [java.util Date])
  (:use gloss.core)
  (:require gloss.io)
  (:use [clojure.contrib.math])
  (:require clojure.set))

; Don't mangle underscores into dashes. <sigh>
(. protobuf.core.PersistentProtocolBufferMap setUseUnderscores true)

; Protobufs
(def Msg (protodef reimann.Proto$Msg))
(def Query (protodef reimann.Proto$Query))
(def State (protodef reimann.Proto$State))
(def Event (protodef reimann.Proto$Event))

(defmacro threaded [thread-count & body]
  `(let [futures# (map (fn [_#] (future ~@body))
                      (range 0 ~thread-count))]
    (doseq [fut# futures#] (deref fut#))))

(defn ppmap [threads f s]
  (let [work (partition (/ (count s) threads) s)
        result (pmap (fn [part] (doall (map f part))) work)]
    (doall (apply concat result))))

(defn unix-time []
  (/ (System/currentTimeMillis) 1000))

(defn time-at [unix-time]
  "Returns the Date of a unix epoch time."
  (java.util.Date. (long unix-time)))

(defn pre-dump-event [e]
  (assoc e :metric_f (:metric e)))

(defn post-load-event [e]
  "Loads a protobuf event to an internal event."
  (let [e (apply hash-map (apply concat e))
        e (if (:metric_f e) (assoc e :metric (:metric_f e)) e)
        e (if (:time e) e (assoc e :time (unix-time)))]
    e))

(defn decode [s]
  "Decode a gloss buffer to a Msg"
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

(defn encode [msg]
  "Builds and dumps a protobuf message from msg"
  (let [msg (merge msg
                   {:events (map pre-dump-event (:events msg))
                    :states (map pre-dump-event (:states msg))})
        pb (apply protobuf Msg (apply concat msg))]
    (protobuf-dump pb)))

; Create a new event
(defn event [opts]
  (let [t (round (or (opts :time)
                     (unix-time)))]
    (apply protobuf Event
      (apply concat (merge opts {:time t})))))

(defn state [opts]
  (let [t (round (or (opts :time)
                     (unix-time)))]
    (apply protobuf State
      (apply concat (merge opts {:time t})))))

(defn approx-equal 
([x,y]
  (approx-equal x y 0.01))
([x, y, tol]
  (if (= x y) true
    (let [f (try (/ x y) (catch java.lang.ArithmeticException e (/ y x)))]
      (< (- 1 tol) f (+ 1 tol))))))

(defn member? [r s]
  "Is e present in s?"
  (some (fn [e] (= r e)) s))

(defn subset? [required s]
  "Are all required present in s?"
  (clojure.set/subset? (set required) (set s)))

(defn overlap? [a b]
  "Do a and b have any elements in common?"
  (some (fn [e]
          (some (fn [r] (= e r)) a)) b))

(defn disjoint? [a b]
  "Do a and b have no elements in common?"
  (not-any? (fn [e] 
             (some (fn [r] (= e r)) a))
           b))
