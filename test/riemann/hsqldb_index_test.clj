(ns riemann.hsqldb-index-test
  (:use riemann.query
        riemann.hsqldb-index
        clojure.test))

(deftest translate-ast-simple
  (let [query-ast (ast "true")]
    (is (= true (translate-ast query-ast)))))

(deftest translate-ast-for-equals
  (let [query-ast (ast "host = nil")]
    (is (= {:statement "\"host\" IS NULL", :args nil} (translate-ast query-ast)))))

(deftest translate-ast-for-less-than
  (let [query-ast      (ast "metric_f < 3")
        translated-ast (translate-ast query-ast)]
    (is (= {:statement "\"metric_f\" < ?", :args '(3)} translated-ast))))

(deftest translate-ast-for-and-and-less-then
  (let [query-ast (ast "host = nil and metric_f < 3")
        translated-ast (translate-ast query-ast)]
    (is (= {:statement "(\"host\" IS NULL) AND (\"metric_f\" < ?)", :args '(3)} translated-ast))))

(deftest translate-ast-for-tagged
  (let [query-ast (ast "tagged \"cat\"")
        translated-ast (translate-ast query-ast)]
    (is (= {:statement "POSITION_ARRAY(? IN \"tags\") != 0", :args '("cat")} translated-ast))))

(deftest translate-ast-for-not-equal
  (let [query-ast (ast "host != \"cat\"")
        translated-ast (translate-ast query-ast)]
    (is (=  {:statement "NOT (\"host\" = ?)", :args '("cat")} translated-ast))))

(deftest translate-ast-for-regexp
  (let [query-ast (ast "host ~= \"cat.*\"")
        translated-ast (translate-ast query-ast)]
    (is (= {:statement "REGEXP_SUBSTRING(\"host\",?) IS NOT NULL", :args '("cat.*")} translated-ast))))

