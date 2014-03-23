(ns riemann.hsqldb-index
  (:require [riemann.query :as query]
            [riemann.index :as index])
  (:use [riemann.time :only [unix-time]]
        riemann.service))

(defn translate-ast
  "Translate AST into korma-compatible format"
  [query-ast]
  (if (list? query-ast)
    (let [[op & rest-ast] query-ast]
      (condp = op
        'when    (translate-ast (last rest-ast))
        'and     (cons op (map translate-ast rest-ast))
        'or      (cons op (map translate-ast rest-ast))
        ; Ignore tags for now
        'member? (list)
        're-find (list 'not= (list 'sqlfn
                       "REGEXP_MATCHES"
                       (keyword (last rest-ast))
                       (str (first rest-ast)))
                       nil)
        query-ast))
    query-ast))