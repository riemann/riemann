(ns riemann.query-test
  (:require [riemann.query :refer :all]
            [riemann.time :refer [linear-time]]
            [clojure.test :refer :all]))

(deftest ast-test
  (are [s expr] (= (ast s) expr)
       ; Fields
       "state = true"        '(= :state true)
       "host = true"         '(= :host true)
       "service = true"      '(= :service true)
       "description = true"  '(= :description true)
       "metric_f = true"     '(= :metric_f true)
       "metric = true"       '(= :metric true)
       "time = true"         '(= :time true)
       "ttl = 64"            '(= :ttl 64)

       ; Literals
       "true"  true
       "false" false
       "nil"   nil
       "null"  nil

       ; Integers
       "state = 0"  '(= :state 0)
       "state = 1"  '(= :state 1)
       "state = -1" '(= :state -1)

       ; Floats
       "state = 0.0"     '(= :state 0.0)
       "state = 1.5"     '(= :state 1.5)
       "state = -1.5"    '(= :state -1.5)
       "state = 1e5"     '(= :state 1e5)
       "state = 1E5"     '(= :state 1e5)
       "state = -1.2e-5" '(= :state -1.2e-5)

       ; Strings
       "state = \"\""                '(= :state "")
       "state = \"foo\""             '(= :state "foo")
       "state = \"\\b\\t\\n\\f\\r\"" '(= :state "\b\t\n\f\r")
       "state = \" \\\" \\\\ \""     '(= :state " \" \\ ")
       "state = \"辻斬\""            '(= :state "辻斬")

       ; Simple predicates
       "state = 2"                   '(= :state 2)
       "state > 2"                   '(> :state 2)
       "state < 2"                   '(< :state 2)
       "state >= 2"                  '(>= :state 2)
       "state <= 2"                  '(<= :state 2)
       "state != 2"                  '(not= :state 2)

       "state =~ \"%foo%\""          '(:like "%foo%" :state)

       ; yay regexes aren't comparable
;       "state ~= \"[a-z]+\""        '(:regex #"[a-z]+" :state)
;       "state ~= \"\w+\""           '(:regex #"\w+" :state)

       ; Tags
       "tagged \"cat\""              '(:tagged "cat")

       ; Boolean operators
       "not host = 1"                '(not (= :host 1))
       "host = 1 and state = 2"      '(and (= :host 1)
                                           (= :state 2))
       "host = 1 or state = 2"       '(or (= :host 1)
                                          (= :state 2))

       ; Grouping
       "(host = 1)"                  '(= :host 1)
       "((host = 1))"                '(= :host 1)

       ; Precedence
       "not host = 1 and host = 2"   '(and (not (= :host 1))
                                           (= :host 2))

       "not host = 1 or host = 2 and host = 3"
       '(or (not (= :host 1))
            (and (= :host 2) (= :host 3)))

       "not ((host = 1 or host = 2) and host = 3)"
       '(not (and (or (= :host 1)
                      (= :host 2))
                  (= :host 3)))
       ))


(deftest clj-ast-test
  (are [s expr] (= (clj-ast (ast s)) expr)
       ; Fields
       "state = true"        '(= (:state event) true)
       "host = true"         '(= (:host event) true)
       "service = true"      '(= (:service event) true)
       "description = true"  '(= (:description event) true)
       "metric_f = true"     '(= (:metric_f event) true)
       "metric = true"       '(= (:metric event) true)
       "time = true"         '(= (:time event) true)
       "ttl = 64"            '(= (:ttl event) 64)

       ; Literals
       "true"  true
       "false" false
       "nil"   nil
       "null"  nil

       ; Integers
       "state = 0"  '(= (:state event) 0)
       "state = 1"  '(= (:state event) 1)
       "state = -1" '(= (:state event) -1)

       ; Floats
       "state = 0.0"     '(= (:state event) 0.0)
       "state = 1.5"     '(= (:state event) 1.5)
       "state = -1.5"    '(= (:state event) -1.5)
       "state = 1e5"     '(= (:state event) 1e5)
       "state = 1E5"     '(= (:state event) 1e5)
       "state = -1.2e-5" '(= (:state event) -1.2e-5)

       ; Strings
       "state = \"\""                '(= (:state event) "")
       "state = \"foo\""             '(= (:state event) "foo")
       "state = \"\\b\\t\\n\\f\\r\"" '(= (:state event) "\b\t\n\f\r")
       "state = \" \\\" \\\\ \""     '(= (:state event) " \" \\ ")
       "state = \"辻斬\""            '(= (:state event) "辻斬")

       ; Simple predicates
       "state = 2"                   '(= (:state event) 2)
       "state > 2"                   '(let [a (:state event)
                                            b 2]
                                        (and (number? a)
                                             (number? b)
                                             (> a b)))
       "state < 2"                   '(let [a (:state event)
                                            b 2]
                                        (and (number? a)
                                             (number? b)
                                             (< a b)))
       "state >= 2"                  '(let [a (:state event)
                                            b 2]
                                        (and (number? a)
                                             (number? b)
                                             (>= a b)))
       "state <= 2"                  '(let [a (:state event)
                                            b 2]
                                        (and (number? a)
                                             (number? b)
                                             (<= a b)))
       "state != 2"                  '(not= (:state event) 2)
       ; Regexen aren't comparable
       ; "state =~ \"%foo%\""          '(re-find #".*foo.*" state)

       ; Tags
       "tagged \"cat\""              '(when-let [tags (:tags event)]
                                        (riemann.common/member? "cat" tags))

       ; Boolean operators
       "not host = 1"                '(not (= (:host event) 1))
       "host = 1 and state = 2"      '(and (= (:host event) 1)
                                           (= (:state event) 2))
       "host = 1 or state = 2"       '(or (= (:host event) 1)
                                          (= (:state event) 2))

       ; Grouping
       "(host = 1)"                  '(= (:host event) 1)
       "((host = 1))"                '(= (:host event) 1)

       ; Precedence
       "not host = 1 and host = 2"   '(and (not (= (:host event) 1))
                                           (= (:host event) 2))

       "not host = 1 or host = 2 and host = 3"
       '(or (not (= (:host event) 1))
            (and (= (:host event) 2) (= (:host event) 3)))

       "not ((host = 1 or host = 2) and host = 3)"
       '(not (and (or (= (:host event) 1)
                      (= (:host event) 2))
                  (= (:host event) 3)))
       ))

(defn f [s good evil]
  "Given a query string s, ensure that it matches all good states and no evil
  ones."
  (let [fun (fun (ast s))]
    (doseq [state good]
      (is (fun state)))
    (doseq [state evil]
      (is (not (fun state))))))

(deftest truthy
         (f "true"
            [{:state "foo"} {}]
            [])

         (f "false"
            []
            [{:state "foo"} {}])

         (f "null"
            []
            [{:state "foo"} {}]))

(deftest equal
         (f "state = \"foo\"" 
           [{:state "foo"}]
           [{:state "bar"} {}])
         )

(deftest not-equal
         (f "state != 1"
            [{:state 0.5} {}]
            [{:state 1}]))

(deftest wildcard
         (f "host =~ \"%s.\""
            [{:host "s."} {:host "foos."}]
            [{:host "a."} {:host "s.murf"} {}]))

(deftest regexp
         (f "host ~= \"foo?[1-9]+\""
            [{:host "foo19"} {:host "foo1"} {:host "fo42"}]
            [{:host "abc"} {:host "foo"} {:host "fooo42"} {}]))

(deftest inequality
         (f "metric > 1e10"
            [{:metric 1e11}]
            [{:metric 1e10} {}])
         (f "metric >= -1"
            [{:metric 0} {:metric -1}]
            [{:metric -2} {}])
         (f "metric < 1.2e2"
            [{:metric 1.5e1}]
            [{:metric 1.2e2} {}])
         (f "metric <= 1"
            [{:metric 1} {:metric -20}]
            [{:metric 2} {}]))

(deftest tagged
         (f "tagged \"cat\""
            [{:tags #{"cat" "dog"}} {:tags #{"cat"}}]
            [{:tags #{"dog"}} {}]))

(deftest null
         (f "time = null and description != nil"
            [{:time nil :description true} {:description "hey"}]
             [{:time 2 :description true} {:description nil} {}]))

(deftest bool
         (f "not ((host = 1 or host = 2) and service = 3)"
            [{:host 1} {:service 3} {}]
            [{:host 2 :service 3}]))

(deftest custom-fields
  (f "paws = 4 and tagged \"catz\""
     [{:paws 4, :time 2, :tags ["fuzzy" "catz"]}]
     [; Bad tags
      {:paws 4, :time 2, :tags ["fuzzy"]}
      {:paws 4, :time 2, :tags []}
      {:paws 4, :time 2}

      ; Bad paws
      {:paws 3,   :time 2, :tags ["fuzzy" "catz"]}
      {:paws nil, :time 2, :tags ["fuzzy" "catz"]}
      {           :time 2, :tags ["fuzzy" "catz"]}]))

(deftest fast
         (let [fun (fun (ast
                      "host =~ \"api %\" and state = \"ok\" and metric > 0"))
               events (cycle [{:host "api 1" :state "ok" :metric 1.2}
                              {:host "other" :state "ok" :metric 1.2}
                              {:host "api 2" :state "no" :metric 1.2}
                              {:host "api 3" :state "ok" :metric 0.5}
                              {}])
               t1 (linear-time)]
           (doseq [e (take 1000 events)]
             (fun e))
           (is (< (- (linear-time) t1) 0.05))))

(deftest memory-test
  (let [ast (ast "metric = 4")]
      (dotimes [i 1e3]
        (fun ast))))
