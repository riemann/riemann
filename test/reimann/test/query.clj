(ns reimann.test.query
  (:use [reimann.query])
  (:use [clojure.test]))

(deftest ast-test
         (are [s expr] (= (ast s) expr)
              ; Fields
              "state = true"        '(= state true)
              "host = true"         '(= host true)
              "service = true"      '(= service true)
              "description = true"  '(= description true)
              "metric_f = true"     '(= metric_f true)
              "time = true"         '(= time true)

              ; Literals
              "true"  true
              "false" false
              "nil"   nil
              "null"  nil

              ; Integers
              "state = 0"  '(= state 0)
              "state = 1"  '(= state 1)
              "state = -1" '(= state -1)
              
              ; Floats
              "state = 1."      '(= state 1.)
              "state = 0.0"     '(= state 0.0)
              "state = 1.5"     '(= state 1.5)
              "state = -1.5"    '(= state -1.5)
              "state = 1e5"     '(= state 1e5)
              "state = 1E5"     '(= state 1e5)
              "state = -1.2e-5" '(= state -1.2e-5)

              ; Strings
              "state = \"\""                '(= state "")
              "state = \"foo\""             '(= state "foo")
              "state = \"\\b\\t\\n\\f\\r\"" '(= state "\b\t\n\f\r")
              "state = \" \\\" \\\\ \""     '(= state " \" \\ ")
              "state = \"辻斬\""            '(= state "辻斬")

              ; Simple predicates
              "state = 2"                   '(= state 2)
              "state > 2"                   '(when state (> state 2))
              "state < 2"                   '(when state (< state 2))
              "state >= 2"                  '(when state (>= state 2))
              "state <= 2"                  '(when state (<= state 2))
              "state != 2"                  '(not (= state 2))
              ; Regexen aren't comparable
              ; "state =~ \"%foo%\""          '(re-find #".*foo.*" state)

              ; Boolean operators
              "not host = 1"                '(not (= host 1))
              "host = 1 and state = 2"      '(and (= host 1) (= state 2))
              "host = 1 or state = 2"       '(or (= host 1) (= state 2))
              
              ; Grouping
              "(host = 1)"                  '(= host 1)
              "((host = 1))"                '(= host 1)

              ; Precedence
              "not host = 1 and host = 2"
              '(and (not (= host 1)) (= host 2))

              "not host = 1 or host = 2 and host = 3"
              '(or (not (= host 1))
                   (and (= host 2) (= host 3)))

              "not ((host = 1 or host = 2) and host = 3)"
              '(not (and (or (= host 1)
                             (= host 2))
                         (= host 3)))
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
         (f "host =~ \"%s\""
            [{:host "s"} {:host "foos"}]
            [{:host "a"} {}]))

(deftest inequality
         (f "metric_f > 1e10"
            [{:metric_f 1e11}]
            [{:metric_f 1e10} {}])
         (f "metric_f >= -1"
            [{:metric_f 0} {:metric_f -1}]
            [{:metric_f -2} {}])
         (f "metric_f < 1.2e2"
            [{:metric_f 1.5e1}]
            [{:metric_f 1.2e2} {}])
         (f "metric_f <= 1"
            [{:metric_f 1} {:metric_f -20}]
            [{:metric_f 2} {}]))

(deftest null
         (f "time = null and description != nil"
            [{:time nil :description true} {:description "hey"}]
             [{:time 2 :description true} {:description nil} {}]))

(deftest bool
         (f "not ((host = 1 or host = 2) and service = 3)"
            [{:host 1} {:service 3} {}]
            [{:host 2 :service 3}]))

(deftest fast
         (let [fun (fun (ast 
                      "host =~ \"api %\" and state = \"ok\" and metric_f > 0"))
               events (cycle [{:host "api 1" :state "ok" :metric_f 1.2}
                              {:host "other" :state "ok" :metric_f 1.2}
                              {:host "api 2" :state "no" :metric_f 1.2}
                              {:host "api 3" :state "ok" :metric_f 0.5}
                              {}])]
           (time (doseq [e (take 1000 events)]
                   (fun e)))))
