(ns riemann.hsqldb-index-test
  (:use riemann.query
        riemann.hsqldb-index
        clojure.test))

(deftest translate-ast-simple
  (let [query-ast (ast "true")]
    (is (= true (translate-ast query-ast)))))

(deftest translate-ast-for-equals
  (let [query-ast (ast "host = nil")]
    (is (= {:statement "host IS NULL", :params nil} (translate-ast query-ast)))))

(deftest translate-ast-for-less-than
  (let [query-ast      (ast "metric < 3")
        translated-ast (translate-ast query-ast)]
    (is (= {:statement "((metric_sint64 IS NOT NULL AND metric_sint64 < ?) OR (metric_f IS NOT NULL AND metric_f < ?))", :params '(3 3)} translated-ast))))

(deftest translate-ast-for-and-and-less-then
  (let [query-ast (ast "host = nil and metric < 3")
        translated-ast (translate-ast query-ast)]
    (is (= {:statement "(host IS NULL) AND (((metric_sint64 IS NOT NULL AND metric_sint64 < ?) OR (metric_f IS NOT NULL AND metric_f < ?)))", :params '(3 3)} translated-ast))))

(deftest translate-ast-for-tagged
  (let [query-ast (ast "tagged \"cat\"")
        translated-ast (translate-ast query-ast)]
    (is (= {:statement "POSITION_ARRAY(? IN tags) != 0", :params '("cat")} translated-ast))))

(deftest translate-ast-for-not-equal
  (let [query-ast (ast "host != \"cat\"")
        translated-ast (translate-ast query-ast)]
    (is (=  {:statement "NOT (host = ?)", :params '("cat")} translated-ast))))

(deftest translate-ast-for-regexp
  (let [query-ast (ast "host ~= \"cat.*\"")
        translated-ast (translate-ast query-ast)]
    (is (= {:statement "REGEXP_SUBSTRING(host,?) IS NOT NULL", :params '("cat.*")} translated-ast))))
