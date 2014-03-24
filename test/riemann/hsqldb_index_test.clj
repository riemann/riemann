(ns riemann.hsqldb-index-test
  (:use riemann.query
        riemann.hsqldb-index
        clojure.test))

(deftest translate-ast-simple
  (let [query-ast (ast "true")]
    (is (= true (translate-ast query-ast)))))

(deftest translate-ast-for-equals
  (let [query-ast (ast "host = nil")]
    (is (= '(= :host nil) (translate-ast query-ast)))))

(deftest translate-ast-for-less-than
  (let [query-ast      (ast "metric_f < 3")
        translated-ast (translate-ast query-ast)]
    (is (= '(< :metric_f 3) translated-ast))))

(deftest translate-ast-for-and-and-less-then
  (let [query-ast (ast "host = nil and metric_f < 3")
        translated-ast (translate-ast query-ast)]
    (is (= '(and (= :host nil) (< :metric_f 3)) translated-ast))))

(deftest translate-ast-for-tagged
  (let [query-ast (ast "tagged \"cat\"")
        translated-ast (translate-ast query-ast)]
    (is (= '() translated-ast))))

(deftest translate-ast-for-not-equal
  (let [query-ast (ast "host != \"cat\"")
        translated-ast (translate-ast query-ast)]
    (is (= '(not (= :host "cat")) translated-ast))))

(deftest translate-ast-for-regexp
  (let [query-ast (ast "host ~= \"cat.*\"")
        translated-ast (translate-ast query-ast)]
    (is (= '(not= (sqlfn "REGEXP_MATCHES" :host "cat.*") nil) translated-ast))))

