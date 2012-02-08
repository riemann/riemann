(ns reimann.common
  (:use [protobuf.core])
  (:import [java.util Date])
  (:use gloss.core)
  (:require gloss.io)
  (:use [clojure.contrib.math]))

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

(defn decode [s]
  "Decode a gloss buffer to a Msg"
  (let [buffer (gloss.io/contiguous s)]
    (let [bytes (byte-array (.remaining buffer))]
      (.get buffer bytes 0 (alength bytes))
      (protobuf-load Msg bytes))))

; Create a new event
(defn event [opts]
  (let [t (round (or (opts :time)
                     (unix-time)))]
    (apply protobuf Event
      (apply concat (merge opts {:time t})))))

(defn approx-equal 
([x,y]
  (approx-equal x y 0.01))
([x, y, tol]
  (if (= x y) true
    (let [f (try (/ x y) (catch java.lang.ArithmeticException e (/ y x)))]
      (< (- 1 tol) f (+ 1 tol))))))

; Create a new state
(defn state [opts]
  (let [t (round (or (opts :time)
                     (unix-time)))]
    (apply protobuf State
      (apply concat (merge opts {:time t})))))
