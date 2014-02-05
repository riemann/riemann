(ns riemann.query
  "The query parser. Parses strings into ASTs, and converts ASTs to functions
  which match events."
  (:use riemann.common
        [slingshot.slingshot :only [throw+ try+]])
  (:require [clojure.core.cache :as cache])
  (:import (org.antlr.runtime ANTLRStringStream
                              CommonTokenStream)
           (org.antlr.runtime.tree BaseTree)
           (riemann QueryLexer QueryParser)))

; With many thanks to Brian Carper
; http://briancarper.net/blog/554/antlr-via-clojure

(defn parse-string
  "Parse string into ANTLR tree nodes"
  [s]
  (try
    (let [lexer (QueryLexer. (ANTLRStringStream. s))
                  tokens (CommonTokenStream. lexer)
                  parser (QueryParser. tokens)]
      (.getTree (.expr parser)))
    (catch Throwable e
      (throw+ {:type ::parse-error
               :message (.getMessage (.getCause e))}))))

(defn- make-regex
  "Convert a string like \"foo%\" into /^foo.*$/"
  [string]
  (let [tokens (re-seq #"%|[^%]+" string)
        pairs (map (fn [token]
                     (case token
                        "%" ".*"
                       (java.util.regex.Pattern/quote token)))
                   tokens)]
    (re-pattern (str "^" (apply str pairs) "$"))))

(defn node-ast [^BaseTree node]
  "The AST for a given parse node"
  (let [n    (.getText node)
        kids (remove (fn [x] (= x :useless))
                     (map node-ast (.getChildren node)))]
    (case n
      "or"  (apply list 'or kids)
      "and" (apply list 'and kids)
      "not" (apply list 'not kids)
      "="   (apply list '= kids)
      ">"   (list 'when (first kids) (apply list '> kids))
      ">="  (list 'when (first kids) (apply list '>= kids))
      "<"   (list 'when (first kids) (apply list '< kids))
      "<="  (list 'when (first kids) (apply list '<= kids))
      "=~"  (list 'when (first kids) (list 're-find (make-regex (last kids))
                                           (first kids)))
      "~="  (list 'when (first kids) (list 're-find (re-pattern (last kids))
                                           (first kids)))
      "!="  (list 'not (apply list '= kids))
      "tagged"      (list 'when 'tags (list 'member? (first kids) 'tags))
      "("           :useless
      ")"           :useless
      "nil"         nil
      "null"        nil
      "true"        true
      "false"       false
      "host"        'host
      "service"     'service
      "state"       'state
      "description" 'description
      "metric_f"    'metric_f
      "metric"      'metric
      "time"        'time
      "ttl"         'ttl
      (when n (let [term (read-string n)]
                (if (or (number? term)
                        (string? term))
                  term
                  (throw+ {:type ::parse-error
                           :message (str "invalid term \"" n "\"")})))))))

(defn ast
  "The expression AST for a given string"
  [string]
  (node-ast (parse-string string)))

(def fun-cache
  "Speeds up the compilation of queries by caching map of ASTs to corresponding
  functions."
  (atom (cache/lru-cache-factory {} :threshold 64)))

(defn fun
  "Transforms an AST into a fn [event] which returns true if the query matches
  that event. Example:

  (def q (fun (ast \"metric > 2\")))
  (q {:metric 1}) => false
  (q {:metric 3}) => true"
  [ast]
  (if-let [fun (cache/lookup @fun-cache ast)]
    ; Cache hit
    (do (swap! fun-cache cache/hit ast)
        fun)
    ; Cache miss
    (let [fun (eval
                (list 'fn ['event]
                      (list 'let '[host        (:host event)
                                   service     (:service event)
                                   state       (:state event)
                                   description (:description event)
                                   metric_f    (:metric_f event)
                                   metric      (:metric event)
                                   time        (:time event)
                                   tags        (:tags event)
                                   ttl         (:ttl event)
                                   member?     riemann.common/member?]
                            ast)))]
      (swap! fun-cache cache/miss ast fun)
      fun)))
