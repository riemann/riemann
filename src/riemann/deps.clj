(ns riemann.deps
  "Riemann's dependency resolution system expresses stateful relationships
  between events. Dependencies are expressed as Rules; a Rule is a statement
  about the relationship between a particular event and the current state of
  the index.

  Maps are rules which specify that their keys and values should be present in
  some event in the index. {} will match any non-empty index. {:service \"a\"
  :state \"ok\"} will match an index which has {:service \"a\" :state \"ok\"
  :metric 123}, and so on.

  (all & rules) matches only if all rules match.

  (any & rules) matches if any of the rules match.

  (localhost & rules) states that all child rules must have the same host as
  the event of interest.

  (depends a & bs) means that if a matches the current event (and only the
  current event, not the full index), b must match the current event and index.
  "
  (:require [riemann.streams :as streams]))

(defprotocol Rule
  (match [this context event]))

(extend-protocol Rule
  clojure.lang.IPersistentMap
  (match [this index _]
         (some (fn [e] (= this (select-keys e (keys this))))
               index)
               ))

(defrecord All [rules]
  Rule
  (match [this index event]
;         (prn "Matching all" rules)
;         (prn "index are" index)
;         (prn "event is" event)
         (every? #(match % index event) rules)))

(defn all [& rules]
  (All. rules))

(defrecord Any [rules]
  Rule
  (match [this index event]
         (some #(match % index event) rules)))

(defn any [& rules]
  (Any. rules))

(defrecord Localhost [rule]
  Rule
  (match [this index event]
         (match rule
                (filter (fn [e] (= (:host event) (:host e))) index)
                event)))

(defn localhost [& rules]
  (Localhost. (apply all rules)))

(defrecord Depends [a b]
  Rule
  (match [this index event]
         (if (match a [event] event)
           (match b index event)
           true)))

(defn depends [a & bs]
  (Depends. a (All. bs)))

(defn deps-tag [index rule & children]
  "Returns a stream which accepts events, checks whether they satisfy the given
  rule, and associates those which have their dependencies satisfied with
  {:deps-satisfied true}, and false for those which are satisfied."
  (fn [event]
    (streams/call-rescue
      (assoc event :deps-satisfied? (match rule index event))
      children)))
