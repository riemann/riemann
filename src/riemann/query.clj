(ns riemann.query
  "The query parser. Parses strings into ASTs, and converts ASTs to functions
  which match events."
  (:require [clojure.core.cache :as cache]
            [clojure.java.io :as io]
            [clj-antlr.core :as antlr]
            [riemann.common :refer :all]
            [slingshot.slingshot :refer [throw+ try+]]))

(def antlr-parser (-> "query.g4" io/resource slurp antlr/parser))

(declare antlr->ast)

(defn ast-predicate
  "Rewrites predicates with and/or/not to normal Clojure forms."
  [terms]
  (cond ; Basic predicate
        (= 1 (count terms))
        (antlr->ast (first terms))

        ; Negation
        (= "not" (first terms))
        (do (assert (= 2 (count terms)))
            (list 'not (antlr->ast (second terms))))

        ; And/or
        (= "or" (second terms))
        (do (assert (= 3 (count terms)))
            (let [[t1 _ t2] terms]
              (list 'or (antlr->ast t1) (antlr->ast t2))))

        (= "and" (second terms))
        (do (assert (= 3 (count terms)))
            (let [[t1 _ t2] terms]
              (list 'and (antlr->ast t1) (antlr->ast t2))))

        ; The grammar should never generate these trees, but for completeness...
        true
        (throw+ {:type ::parse-error
                 :message (str "Unexpected predicate structure: "
                               (pr-str terms))})))

(defn ast-prefix
  "Rewrites binary expressions from infix to prefix. Takes a symbol `(e.g. '=)`
  and a 3-element seq like `(1 \"=\" 2)` and emits `(= 1 2)`, ignoring the
  middle term and transforming t1 and t2."
  [sym [t1 _ t2]]
  (list sym (antlr->ast t1) (antlr->ast t2)))

(defn ast-regex
  "Takes a type of match (:like or :regex), a list of three string terms: an
  AST resolving to a string, the literal =~ (ignored), and a pattern. Returns
  `(:regex pattern string-ast)`.

  For :regex matches, emits a regular expression pattern.
  For :like matches, emits a string pattern."
  [type [string _ pattern]]
  (list type
        (condp = type
          :regex (re-pattern (antlr->ast pattern))
          :like  (antlr->ast pattern))
        (antlr->ast string)))

(defn antlr->ast
  "Converts a parse tree to an intermediate AST which is a little easier to
  analyze and work with. This is the AST we use for optimization and which is
  passed to various query compilers. Turns literals into their equivalent JVM
  types, and eliminates some unnecessary parser structure."
  [[node-type & terms]]
  ; (prn :antlr->ast node-type terms)
  (case node-type
    ; Unwrapping transformations: dropping unnecessary parse tree wrapper nodes
    :primary        (recur (first (remove string? terms)))
    :simple         (recur (first terms))
    :value          (recur (first terms))
    :number         (recur (first terms))

    ; Predicate transforms emit and/or/not prefixes: (not (and (= a b)))
    :predicate      (ast-predicate terms)

    ; Rewrite relations like '(:equal a "=" b) as prefix exprs '(= a b)
    :equal          (ast-prefix '=     terms)
    :not_equal      (ast-prefix 'not=  terms)
    :lesser         (ast-prefix '<     terms)
    :greater        (ast-prefix '>     terms)
    :lesser_equal   (ast-prefix '<=    terms)
    :greater_equal  (ast-prefix '>=    terms)

    ; String first, then pattern.
    :regex_match    (ast-regex :regex terms)
    :like           (ast-regex :like  terms)

    ; Drop redundant terms from prefix expressions
    :tagged         (list :tagged (antlr->ast (second terms)))

    ; Value transformations: coercing strings to JVM types.
    :long       (Long/parseLong (first terms))
    :float      (Double/parseDouble (first terms))
    :bign       (bigint (subs (first terms) 0 (dec (.length (first terms)))))
    :string     (read-string (first terms))
    :field      (keyword (first terms))
    :true       true
    :false      false
    :nil        nil

    ; And by default, recurse into sub-expressions.
    (->> terms
         (map (fn [term]
                (if (sequential? term)
                  (antlr->ast term)
                  term)))
         doall
         (cons node-type))))

(defn ast
  "Takes a string to a general AST."
  [string]
  (-> string antlr-parser antlr->ast))

;; This code transforms the general AST into Clojure code.

(declare clj-ast)

(defn clj-ast-guarded-prefix
  "Like prefix, but inserts a predicate check around both terms."
  [f check [a b]]
  (list 'let ['a (clj-ast a)
              'b (clj-ast b)]
        (list 'and
              (list check 'a)
              (list check 'b)
              (list f 'a 'b))))

(defn clj-ast-field
  "Takes a keyword field name and emits an expression to extract that field
  from an 'event map, e.g. `(:fieldname event)`."
  [field]
  (list field 'event))

(defn clj-ast-tagged
  "Takes a tag and emits an expression to match that tag in an event."
  [tag]
  (list 'when-let '[tags (:tags event)]
        (list 'riemann.common/member? tag 'tags)))

(defn make-regex
  "Convert a string like \"foo%\" into /^foo.*$/"
  [string]
  (let [tokens (re-seq #"%|[^%]+" string)
        pairs (map (fn [token]
                     (case token
                        "%" ".*"
                       (java.util.regex.Pattern/quote token)))
                   tokens)]
    (re-pattern (str "^" (apply str pairs) "$"))))

(defn clj-ast-regex-match
  "Takes a pattern transformer, and a list of [pattern string-ast], and emits
  code to match the string-ast's results with a regex match, compiling pattern
  with pattern-transformer."
  [pattern-transformer [pattern field]]
  (list 'let ['s (clj-ast field)]
        (list 'and
              (list 'string? 's)
              (list 're-find (pattern-transformer pattern) 's))))

(defn clj-ast
  "Rewrites an AST to eval-able Clojure forms."
  [ast]
  (cond
    ; Rewrite fields to field extracting expressions
    (keyword? ast)
    (clj-ast-field ast)

    ; Anything other than a list passes through unchanged
    (not (sequential? ast))
    ast

    ; Lists, on the other hand
    true
    (let [[node-type & terms] ast]
      (case node-type
        =               (apply list '= (map clj-ast terms))
        <               (clj-ast-guarded-prefix '< 'number? terms)
        >               (clj-ast-guarded-prefix '> 'number? terms)
        <=              (clj-ast-guarded-prefix '<= 'number? terms)
        >=              (clj-ast-guarded-prefix '>= 'number? terms)
        :like           (clj-ast-regex-match make-regex terms)
        :regex          (clj-ast-regex-match identity   terms)
        :tagged         (clj-ast-tagged (first terms))
       (cons node-type (mapv clj-ast terms))))))

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
    (let [fun (eval (list 'fn ['event] (clj-ast ast)))]
      (swap! fun-cache cache/miss ast fun)
      fun)))
