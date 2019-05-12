(ns riemann.zabbix-test
  (:require [cheshire.core :as json] 
            [clojure.test :refer :all]
            [riemann.zabbix :refer :all]) 
  (:import (java.io ByteArrayOutputStream)
           (java.nio ByteBuffer ByteOrder)
           (java.net Socket)))

(def test-event {:host "riemann.local"
                 :service "zabbix.test"
                 :state "ok"
                 :description "Successful test"
                 :metric 2
                 :time (int (/ (System/currentTimeMillis) 1000))
                 :tags ["riemann" "zabbix"]})
(def other-event {:host "riemann.local"
                  :service "zabbix.other"
                  :state "ok"
                  :description "Successful test"
                  :metric 5
                  :time (int (/ (System/currentTimeMillis) 1000))
                  :tags ["other"]})

(deftest ^:zabbix datapoint-test
  (is (= (:host (make-datapoint test-event)) "riemann.local"))
  (is (= (:key (make-datapoint test-event)) "zabbix.test"))
  (is (= (:value (make-datapoint test-event)) (str (:metric test-event))))
  (is (= (:clock (make-datapoint test-event)) (:time test-event))))

(deftest ^:zabbix request-test
  (is (= (:request (make-request [test-event other-event])) "sender data"))
  (is (= (count (:data (make-request [test-event]))) 1))  
  (is (= (count (:data (make-request [test-event other-event]))) 2)))

(deftest ^:zabbix frame-test
  (let [r (make-request [test-event])
        f (make-frame r)]
    (is (= (->> (take 4 f)
                (map char)
                (apply str))
           "ZBXD"))
    (is (= (nth f 5)) 1)
    (let [length (-> (ByteBuffer/wrap (byte-array (drop 5 (take 13 f))))
                     (.order ByteOrder/LITTLE_ENDIAN)
                     (.getLong))
          data (drop 13 f)
          req (json/parse-string (->> data
                                      (map char)
                                      (apply str)) true)]
      (is (= length (count data)))
      (is (= req r)))))
