(ns reimann.query
  "The query parser. Parses strings into ASTs, and converts ASTs to functions
  which match events."
  (:use reimann.common)
  (:import (org.antlr.runtime ANTLRStringStream
                              CommonTokenStream)
           (reimann QueryLexer QueryParser)))

; With many thanks to Brian Carper
; http://briancarper.net/blog/554/antlr-via-clojure

(defn parse-string 
  "Parse string into ANTLR tree nodes"
  [s]
    (let [lexer (QueryLexer. (ANTLRStringStream. s))
                  tokens (CommonTokenStream. lexer)
                  parser (QueryParser. tokens)]
          (.getTree (.expr parser))))

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
      "metric"      'metric
      "time"        'time
      "ttl"         'ttl
      (when n (read-string n)))))

(defn ast
  "The expression AST for a given string"
  [string]
  (let [node (parse-string string)]
    (node-ast node)))

(defn fun
  "Transforms an AST into a fn [event] which returns true if the query matches
  that event. Example:
  
  (def q (fun (ast \"metric > 2\")))
  (q {:metric 1}) => false
  (q {:metric 3}) => true"
  [ast]
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
                   ttl         (:ttl event)
                   member?     reimann.common/member?]
        ast))))
