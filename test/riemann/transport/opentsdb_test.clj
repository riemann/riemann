(ns riemann.transport.opentsdb-test
  (:use clojure.test
        [riemann.common :only [event]]
        riemann.transport.opentsdb
        [slingshot.slingshot :only [try+]])
  (:require [riemann.logging :as logging]))

(deftest decode-opentsdb-line-success-test
  (is (= (event {:service "name", :description "name", :metric 456.0, :time 123})
         (decode-opentsdb-line "put name 123 456")))
  (is (= (event {:host "host", :service "name host=host", :description "name", :metric 456.0, :tags (list "host=host"), :time 123})
         (decode-opentsdb-line "put name 123 456 host=host")))
  (is (= (event {:service "name tag=value", :description "name", :metric 456.0, :tags (list "tag=value"), :time 123})
         (decode-opentsdb-line "put name 123 456 tag=value")))
  (is (= (event {:service "name tag=value tag2=value2", :description "name", :metric 456.0, :tags (list "tag=value" "tag2=value2"), :time 123})
         (decode-opentsdb-line "put name 123 456 tag=value tag2=value2")))
)

(deftest decode-opentsdb-line-failure-test
  (let [err #(try+ (decode-opentsdb-line %)
                   (catch Object e e))]
    (is (= (err "") "blank line"))
    (is (= (err "version") "version request"))
    (is (= (err "put") "no metric name"))
    (is (= (err "put name") "no timestamp"))
    (is (= (err "put name 123") "no metric"))
    (is (= (err "put name 123 NaN") "NaN metric"))
    (is (= (err "put name 123 metric") "invalid metric"))
    (is (= (err "put name timestamp 123") "invalid timestamp"))
  ))
