(ns reimann.query
  (:use reimann.common)
  (:import (org.antlr.runtime ANTLRStringStream
                              CommonTokenStream)
           (reimann QueryLexer QueryParser)))

; With many thanks to Brian Carper
; http://briancarper.net/blog/554/antlr-via-clojure

(defn parse-string [s]
  "Parse nodes for a string containing an expression"
    (let [lexer (QueryLexer. (ANTLRStringStream. s))
                  tokens (CommonTokenStream. lexer)
                  parser (QueryParser. tokens)]
          (.getTree (.expr parser))))

(defn- make-regex [string]
  "Convert a string like \"foo%\" into /^foo.*$/"
  (let [tokens (re-seq #"%|[^%]+" string)
        pairs (map (fn [token]
                     (case token
                        "%" ".*"
                       (java.util.regex.Pattern/quote token)))
                   tokens)]
    (re-pattern (str "^" (apply str pairs) "$"))))

(defn node-ast [node]
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
      "metric"    'metric
      "time"        'time
      (when n (read-string n)))))

(defn ast [string]
  "The expression AST for a given string"
  (let [node (parse-string string)]
    (node-ast node)))

(defn fun [ast]
  "Transforms an AST into a fn [event] which returns true if the query matches
  that event."
  (eval
    (list 'fn ['event]
      (list 'let '[host        (:host event)
                   service     (:service event)
                   state       (:state event)
                   description (:description event)
                   metric_f    (:metric_f event)
                   metric      (:metric event)
                   time        (:time event)
                   tags        (:tags event)
                   member?     reimann.common/member?]
        ast))))
