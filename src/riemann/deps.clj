(ns riemann.deps)

; Prompted by Klout's request to have Riemann help alert them as to the *root*
; cause of outages, I'm starting to work on a dependency system for Riemann.
;
; The API is a little bit loose in my head--so I'm gonna talk about it here and
; let things precipitate into code.
;
; It is *not* a pure stream. This is all about detecting the state of
; dependencies. We'll have a reference to the index, and query it to identify
; relevant services when an outage occurs.
;
; We can use the index ttls to identify when services may not have checked in
; recently. It may be worth waiting a short while to let states settle before
; deciding on the analysis, but for starters we'll proceed with notifications
; immediately.
;
; The dependency tracker is going to operate on services, obviously. It'll
; express a directed acyclic graph of dependencies between then. The graph
; needs to be dynamic in some respects--new hosts might appear. Since we're
; talking about the index here I'm thinking index queries might be a good
; choice to express the services one depends on.
;
; Maybe the stream system can meet us halfway.
;
; API ideas
;
; - Get a dependency tracker, connected to an index
; - Define the graph
; - Ask "What services does this service depend on?"
; - A stream which passes on only events which have no broken
;   dependencies.
; 
; JUST WHAT ARE DEPENDENCIES?
;
; "app" "riak" "redis" "memcache"
;
; "memcache"
;   [:local "cpu"]
;
; [:any "app" "ok"]
;   [:any "riak" "up"]]
;
; [:any "riak" "up"]
;   [:local "memory" "ok"]
;   [:local "disk" "ok"]]
;
; This gets hard because we don't just need to suppress failure events, but
; also their resolution. Imagine the DB fails, and with it, the app. Then the
; DB recovers but the app is still down. We need to emit an app failure event
; at that point. One way to do this is to have the deps system wrap itself
; around the index so it can intercept all state transitions. Another is for it
; to maintain an internal queue of transitions awaiting dependent resolution;
; that requires that the streams which generate those transitions remember to
; pass them to deps.
;
; Or we could poll. :-P
;
; Eventually, this might be a part of an integrated intelligence module, which
; can tell you things like "These six systems are broken, the root cause is X,
; and by the way, this unrelated service went down a minute later, here's a
; timeline."

(defn normalize-class
  "Normalizes a vector describing a class of events. Inputs:
  \"riak\"
  [\"redis\" \"up\"]
  [\"db1\" \"mysql\" \"ok\"]"
  [rule]
  (if (string? rule)
    [:any rule "ok"]
    (case (count rule)
      1 (concat [:any] rule ["ok"])
      2 (conj rule "ok")
      3 rule)))

(defn dep
  "Establishes a dependency between class a and every class bs, in graph.
  Returns the graph with dependencies established."
  [graph a & bs]
  (assoc graph :deps
    (conj (:deps graph) 
          [(normalize-class a) :all (map normalize-class bs)])))

(defn member?
  "Does the given event fall in to class?"
  [class event]
  (let [[host service state] class]
    (and
      (= service (:service event))
      (or (= :any state) (= state (:state event)))
      (or (= :any host) (= host (:host event))))))

(defn class-query
  "Returns a query AST for the given class, in the context of event."
  [class event]
  (let [[host service state] class
        host (if (= :local host)
               (:host event)
               host)]
    (list 'and
          (if (= :any host)
            true
            (list '= host 'host))
          (list '= service 'service)
          (list '= state 'state))))
