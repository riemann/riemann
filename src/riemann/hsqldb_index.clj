(ns riemann.hsqldb-index
  (:require [riemann.query :as query]
            [riemann.index :as index])
  (:use [riemann.time :only [unix-time]]
        riemann.service))

(defn translate-ast
  "Translate AST into korma-compatible format"
  [query-ast]
  (if (list? query-ast)
    (condp = (first query-ast)
      'when    (translate-ast (last query-ast))
      'and     (cons 'and (map translate-ast (rest query-ast)))
      'or      (cons 'or (map translate-ast (rest query-ast)))
      'member? (list '= 'tag (second query-ast))
      're-find query-ast
      query-ast)
    query-ast))