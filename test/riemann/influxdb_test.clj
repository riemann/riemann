(ns riemann.influxdb-test
  (:require
   [clojure.test :refer :all]
   [riemann.influxdb :as influxdb]
   [riemann.logging :as logging]
   [riemann.test-utils :refer [with-mock]]
   [riemann.time :refer [unix-time]])
  (:import
   (java.util.concurrent TimeUnit)
   (org.influxdb InfluxDBFactory InfluxDB$ConsistencyLevel)
   (org.influxdb.dto BatchPoints Point)))

(logging/init)

(defn ^java.lang.reflect.Field get-field
  "Return Field object"
  [^Class class ^String field-name]
  (let [f (.getDeclaredField class field-name)]
    (.setAccessible f true)
    f))

(def measurement (get-field Point "measurement"))
(def time-field (get-field Point "time"))
(def tags (get-field Point "tags"))
(def fields (get-field Point "fields"))
(def precision (get-field Point "precision"))

;; a database named "riemann_test" should exists for integration tests

(deftest ^:influxdb influxdb-test-mock
  (with-mock [calls influxdb/write-batch-point]
    (testing "deprecated influxdb stream"
      (let [k (influxdb/influxdb
               {:host (or (System/getenv "INFLUXDB_HOST") "localhost")
                :db "riemann_test"})
            t (unix-time)]
        (k {:host "riemann.local"
            :service "influxdb test"
            :state "ok"
            :description "all clear"
            :metric -2
            :time t})
        (is (= 1 (count @calls)))
        (let  [batch-point (second (first @calls))
               points (.getPoints batch-point)
               point (first points)]
          (is (= (.getDatabase batch-point) "riemann_test"))
          (is (= (.getRetentionPolicy batch-point) nil))
          (is (= (.getConsistency batch-point) InfluxDB$ConsistencyLevel/ONE))
          (is (= TimeUnit/SECONDS (.get precision point)))
          (is (= (into {} (.getTags batch-point)) {}))
          (is (= 1 (count points)))
          (is (= {"value" -2.0
                  "state" "ok"
                  "description" "all clear"} (into {} (.get fields point))))
          (is (= "influxdb-test") (.get measurement point))
          (is (= {"host" "riemann.local"} (into {} (.get tags point))))
          (is (=  (long t) (.get time-field point))))
        (k [{:host "riemann.local"
             :service "influxdb test"
             :state "ok"
             :description "all clear"
             :metric -2
             :time t}
            {:host "riemann.local2"
             :service "influxdb test2"
             :state "ok"
             :description "not clear"
             :metric -3
             :foobar "foobaz"
             :time (+ t 1)}
            {:host "riemann.local2"
             :service "influxdb test2"
             :state "ok"
             :description "not clear"
             :metric -3
             :foobar "foobaz"
             "rofl" "mao" ;; test with string key
             "hello" "goodbye"
             :tag-fields #{:foobar "hello"}
             :precision :microseconds
             :time 1000}])
        (is (= 2 (count @calls)))
        (let  [batch-point (second (second @calls))
               points (.getPoints batch-point)
               point1 (first points)
               point2 (second points)
               point3 (last points)]
          (is (= (.getDatabase batch-point) "riemann_test"))
          (is (= (.getRetentionPolicy batch-point) nil))
          (is (= (.getConsistency batch-point) InfluxDB$ConsistencyLevel/ONE))
          (is (= (into {} (.getTags batch-point)) {}))
          (is (= TimeUnit/SECONDS (.get precision point1)))
          (is (= 3 (count points)))
          (is (= {"value" -2.0
                  "state" "ok"
                  "description" "all clear"} (into {} (.get fields point1))))
          (is (= "influxdb-test") (.get measurement point1))
          (is (= {"host" "riemann.local"} (into {} (.get tags point1))))
          (is (=  (long t) (.get time-field point1)))
          ;; second point
          (is (= {"value" -3.0
                  "state" "ok"
                  "description" "not clear"
                  "foobar" "foobaz"} (into {} (.get fields point2))))
          (is (= "influxdb-test2") (.get measurement point2))
          (is (= TimeUnit/SECONDS (.get precision point2)))
          (is (= {"host" "riemann.local2"} (into {} (.get tags point2))))
          (is (=  (+ 1 (long t)) (.get time-field point2)))
          ;; third point
          (is (= {"value" -3.0
                  "state" "ok"
                  "rofl" "mao"
                  "description" "not clear"} (into {} (.get fields point3))))
          (is (= "influxdb-test2") (.get measurement point3))
          (is (= {"host" "riemann.local2"
                  "hello" "goodbye"
                  "foobar" "foobaz"} (into {} (.get tags point3))))
          (is (= TimeUnit/MICROSECONDS (.get precision point3)))
          (is (= 1000000000 (.get time-field point3))))))
    (reset! calls [])
    (testing "deprecated influx stream with default opts"
      (let [k (influxdb/influxdb
               {:host "aphyr.com"
                :version :deprecated
                :db "foodb"
                :port 9999
                :username "user"
                :password "password"
                :precision :milliseconds
                :tag-fields #{:host :baz}
                :tags {"foo" "bar"}
                :retention "autogen"
                :timeout 10000
                :consistency "ALL"})]
        (k {:host "riemann.local"
            :service "influxdb test"
            :state "ok"
            :description "all clear"
            :precision :microseconds
            :foobar "hello"
            :baz "bar"
            :tag-fields #{:foobar}
            :metric -2
            :time 1000})
        (is (= 1 (count @calls)))
        (let  [batch-point (second (first @calls))
               points (.getPoints batch-point)
               point (first points)]
          (is (= 1 (count points)))
          (is (= (.getDatabase batch-point) "foodb"))
          (is (= (.getRetentionPolicy batch-point) "autogen"))
          (is (= (.getConsistency batch-point) InfluxDB$ConsistencyLevel/ALL))
          (is (= TimeUnit/MICROSECONDS (.get precision point)))
          (is (= (into {} (.getTags batch-point)) {"foo" "bar"}))
          (is (= {"value" -2.0
                  "state" "ok"
                  "description" "all clear"} (into {} (.get fields point))))
          (is (= "influxdb-test") (.get measurement point))
          (is (= {"host" "riemann.local"
                  "foobar" "hello"
                  "foo" "bar"
                  "baz" "bar"}
                 (into {} (.get tags point))))
          (is (=  1000000000 (.get time-field point))))))
    (reset! calls [])
    (testing "new influxdb stream"
      (let [k (influxdb/influxdb
               {:host (or (System/getenv "INFLUXDB_HOST") "localhost")
                :version :new-stream})]
        (k {:time 1428366765
            :influxdb-tags {:foo "bar"
                            :bar "baz"}
            :db "riemann_test"
            :measurement "measurement"
            :influxdb-fields {:alice "bob"}})
        (is (= 1 (count @calls)))
        (let  [batch-point (second (first @calls))
               points (.getPoints batch-point)
               point (first points)]
          (is (= (.getDatabase batch-point) "riemann_test"))
          (is (= (.getRetentionPolicy batch-point) nil))
          (is (= (.getConsistency batch-point) InfluxDB$ConsistencyLevel/ONE))
          (is (= (into {} (.getTags batch-point)) {}))
          (is (= 1 (count points)))
          (is (= {"alice" "bob"} (into {} (.get fields point))))
          (is (= "measurement") (.get measurement point))
          (is (= {"foo" "bar"
                  "bar" "baz"} (into {} (.get tags point))))
          (is (= 1428366765 (.get time-field point))))
        (reset! calls [])
        ;; send events with differents db/retention/consistency
        (k [{:time 1428366765
             :influxdb-tags {:foo "bar"
                              :bar "baz"}
             :db "riemann_test"
             :measurement "measurement"
             :influxdb-fields {:alice "bob"}}
            {:time 1428366766
             :influxdb-tags {:foo "bar"
                             :bar "baz"}
             :db "riemann_test_2"
             :measurement "measurement2"
             :retention "autogen"
             :precision :microseconds
             :influxdb-fields {:alice "bob"}}
            {:time 1428366767
             :influxdb-tags {:foo "foo"
                             :bar "bar"}
             :db "riemann_test"
             :measurement "measurement"
             :consistency "ALL"
             :influxdb-fields {:alice "bob"
                               :hello "goodbye"}}
            {:time 1428366768
             :influxdb-tags {:foo "foo"
                             "one" "two"
                             :bar "bar"}
             :db "riemann_test"
             :measurement "measurement2"
             :precision :microseconds
             :consistency "ALL"
             :influxdb-fields {:alice 1
                               "haha" "huhu"
                               :hello 2}}])
        ;; 3 calls because 3 batch points (see influxb/partition-events)
        (is (= 3 (count @calls)))
        ;; first call
        (let  [batch-point (second (first @calls))
               points (.getPoints batch-point)
               point (first points)]
          (is (= 1 (count points)))
          (is (= (.getDatabase batch-point) "riemann_test"))
          (is (= (.getRetentionPolicy batch-point) nil))
          (is (= (.getConsistency batch-point) InfluxDB$ConsistencyLevel/ONE))
          (is (= (into {} (.getTags batch-point)) {}))
          (is (= 1 (count points)))
          (is (= TimeUnit/SECONDS (.get precision point)))
          (is (= {"alice" "bob"} (into {} (.get fields point))))
          (is (= "measurement") (.get measurement point))
          (is (= {"foo" "bar"
                  "bar" "baz"} (into {} (.get tags point))))
          (is (= 1428366765 (.get time-field point))))
        ;; second call
        (let  [batch-point (second (second @calls))
               points (.getPoints batch-point)
               point (first points)]
          (is (= 1 (count points)))
          (is (= (.getDatabase batch-point) "riemann_test_2"))
          (is (= (.getRetentionPolicy batch-point) "autogen"))
          (is (= TimeUnit/MICROSECONDS (.get precision point)))
          (is (= (.getConsistency batch-point) InfluxDB$ConsistencyLevel/ONE))
          (is (= (into {} (.getTags batch-point)) {}))
          (is (= 1 (count points)))
          (is (= {"alice" "bob"} (into {} (.get fields point))))
          (is (= "measurement2") (.get measurement point))
          (is (= {"foo" "bar"
                  "bar" "baz"} (into {} (.get tags point))))
          (is (= 1428366766000000 (.get time-field point))))
        ;; third call
        (let  [batch-point (second (last @calls))
               points (.getPoints batch-point)
               point1 (first points)
               point2 (second points)]
          (is (= 2 (count points)))
          (is (= (.getDatabase batch-point) "riemann_test"))
          (is (= (.getRetentionPolicy batch-point) nil))
          (is (= (.getConsistency batch-point) InfluxDB$ConsistencyLevel/ALL))
          (is (= (into {} (.getTags batch-point)) {}))
          ;; point 1
          (is (= TimeUnit/SECONDS (.get precision point1)))
          (is (= {"alice" "bob" "hello" "goodbye"} (into {} (.get fields point1))))
          (is (= "measurement") (.get measurement point1))
          (is (= {"foo" "foo"
                  "bar" "bar"} (into {} (.get tags point1))))
          (is (= 1428366767 (.get time-field point1)))
          ;; point 2
          (is (= TimeUnit/MICROSECONDS (.get precision point2)))
          (is (= {"alice" 1.0 "hello" 2.0 "haha" "huhu"} (into {} (.get fields point2))))
          (is (= "measurement2") (.get measurement point2))
          (is (= {"foo" "foo"
                  "one" "two"
                  "bar" "bar"} (into {} (.get tags point2))))
          (is (= 1428366768000000 (.get time-field point2))))))
    (reset! calls [])
    (testing "new influxdb stream with default opts"
      (let [k (influxdb/influxdb
               {:host "aphyr.com"
                :version :new-stream
                :db "foodb"
                :port 9999
                :username "user"
                :password "password"
                :precision :milliseconds
                :tags {"hello" "goodbye" :test1 "test1"}
                :retention "autogen"
                :timeout 10000
                :consistency "ALL"})]
        (k {:time 1428366765
            :influxdb-tags {:foo "bar"
                            :bar "baz"}
            :measurement "example"
            :influxdb-fields {:alice "bob"}})
        (is (= 1 (count @calls)))
        (let  [batch-point (second (first @calls))
               points (.getPoints batch-point)
               point (first points)]
          (is (= (.getDatabase batch-point) "foodb"))
          (is (= (.getRetentionPolicy batch-point) "autogen"))
          (is (= (.getConsistency batch-point) InfluxDB$ConsistencyLevel/ALL))
          (is (= (into {} (.getTags batch-point)) {"hello" "goodbye"
                                                   "test1" "test1"}))
          (is (= 1 (count points)))
          (is (= {"alice" "bob"} (into {} (.get fields point))))
          (is (= "example") (.get measurement point))
          (is (= {"foo" "bar"
                  "test1" "test1"
                  "hello" "goodbye"
                  "bar" "baz"} (into {} (.get tags point))))
          (is (= 1428366765000 (.get time-field point))))
        (reset! calls [])
        (k {:time 1428366765
            :influxdb-tags {:foo "bar"
                            :bar "baz"}
            :db "newdb"
            :precision :microseconds
            :retention "foobar"
            :consistency "ONE"
            :measurement "example"
            :influxdb-fields {:alice "bob"}})
        (is (= 1 (count @calls)))
        (let  [batch-point (second (first @calls))
               points (.getPoints batch-point)
               point (first points)]
          (is (= 1 (count points)))
          (is (= (.getDatabase batch-point) "newdb"))
          (is (= (.getRetentionPolicy batch-point) "foobar"))
          (is (= (.getConsistency batch-point) InfluxDB$ConsistencyLevel/ONE))
          (is (= (into {} (.getTags batch-point)) {"hello" "goodbye"
                                                   "test1" "test1"}))
          (is (= {"alice" "bob"} (into {} (.get fields point))))
          (is (= "example") (.get measurement point))
          (is (= {"foo" "bar"
                  "test1" "test1"
                  "hello" "goodbye"
                  "bar" "baz"} (into {} (.get tags point))))
          (is (= 1428366765000000 (.get time-field point))))))))

(deftest ^:influxdb ^:integration influxdb-test
  (testing "deprecated influxdb stream"
    (let [k (influxdb/influxdb
             {:host (or (System/getenv "INFLUXDB_HOST") "localhost")
              :db "riemann_test"})]
      (k {:host "riemann.local"
          :service "influxdb test"
          :state "ok"
          :description "all clear, uh, situation normal"
          :metric -2
          :time (unix-time)})
      (k {:service "influxdb test"
          :state "ok"
          :description "all clear, uh, situation normal"
          :metric 3.14159
          :time (unix-time)})
      (k {:host "no-service.riemann.local"
          :state "ok"
          :description "Missing service, not transmitted"
          :metric 4
          :time (unix-time)})
      (k {:host 20
          :service "influx "
          :state "ok"
          :description "all clear, uh, situation normal"
          :foo 10
          :metric -2
          :time (unix-time)})))

  (testing "new influxdb stream"
    (let [k (influxdb/influxdb
             {:host (or (System/getenv "INFLUXDB_HOST") "localhost")
              :version :new-stream})]
      (k {:time 1428366765
          :influxdb-tags {:foo "bar"
                          :bar "baz"}
          :precision :milliseconds
          :db "riemann_test"
          :measurement "measurement"
          :influxdb-fields {:alice "bob"}})
      (k {:time 21893979/1000000
          :influxdb-tags {:foo "bar"
                          :bar "baz"}
          :db "riemann_test"
          :measurement "measurement1"
          :influxdb-fields {:alice 1
                            :bob "hu"}})
      (k {:time 1428366765
          :influxdb-tags {:foo "bar"
                          :bar "baz"}
          :precision :milliseconds
          :db "riemann_test"
          :measurement "measurement"
          :influxdb-fields {:ratio 21893979/1000000}})
      (k {:time 1428366765
          :influxdb-tags {:foo "bar"
                          :bar "baz"}
          :precision :seconds
          :db "riemann_test"
          :consistency "ALL"
          :retention "autogen"
          :measurement "measurement"
          :influxdb-fields {:alice "bob"}})
      (k {:time 1428366765
          :influxdb-tags {:foo "bar"
                          :bar "baz"
                          :baz 10}
          :precision :milliseconds
          :db "riemann_test"
          :measurement "measurement"
          :influxdb-fields {:alice "bob"
                            :bob 20}}))))

(deftest event-fields-test
  (is (= (influxdb/event-fields
          #{}
          {:host "host-01"
           :service "test service"
           :time 1428366765
           :metric 42.08})
         {"value" 42.08}))
  (is (= (influxdb/event-fields
          #{}
          {:host "host-01"
           :service "test service"
           :time 1428366765
           :metric 42.08
           :hello "hello"})
         {"value" 42.08 "hello" "hello"}))
  (is (= (influxdb/event-fields
          #{:hello}
          {:host "host-01"
           :service "test service"
           :time 1428366765
           :metric 42.08
           :hello "hello"})
         {"value" 42.08}))
  (is (= (influxdb/event-fields
          #{:hello}
          {:host "host-01"
           :service "test service"
           :time 1428366765
           :metric 42.08
           :hello "hello"
           :foo "bar"})
         {"value" 42.08
          "foo" "bar"})))

(deftest event-tags-test
  (is (= (influxdb/event-tags
          #{}
          {:host "host-01"
           :service "test service"
           :time 1428366765
           :metric 42.08})
         {}))
  (is (= (influxdb/event-tags
          #{:host}
          {:host "host-01"
           :service "test service"
           :time 1428366765
           :metric 42.08})
         {"host" "host-01"}))
  (is (= (influxdb/event-tags
          #{:host :hello}
          {:host "host-01"
           :service "test service"
           :time 1428366765
           :hello "hello"
           :metric 42.08})
         {"host" "host-01"
          "hello" "hello"}))
  (is (= (influxdb/event-tags
          #{:host :hello}
          {:host "host-01"
           :service "test service"
           :time 1428366765
           :foo "bar"
           :hello "hello"
           :metric 42.08})
         {"host" "host-01"
          "hello" "hello"})))

(deftest get-time-unit-test
  (is (= TimeUnit/SECONDS (influxdb/get-time-unit :seconds)))
  (is (= TimeUnit/MILLISECONDS (influxdb/get-time-unit :milliseconds)))
  (is (= TimeUnit/MICROSECONDS (influxdb/get-time-unit :microseconds)))
  (is (= TimeUnit/SECONDS (influxdb/get-time-unit :default))
      "Default value is SECONDS"))

(deftest convert-time-test
  (is (= 1 (influxdb/convert-time 1 :seconds))
      "seconds -> seconds")
  (is (= 1000 (influxdb/convert-time 1 :milliseconds))
      "seconds -> milliseconds")
  (is (= 1000000 (influxdb/convert-time 1 :microseconds))
      "seconds -> microseconds")
  (is (= 1 (influxdb/convert-time 1 :default))
      "seconds -> seconds (default)"))

(deftest point-conversion-deprecated
  (is (nil? (influxdb/event->point-9 {:service "foo test" :time 1} {:tag-fields #{}}))
      "Event with no metric is converted to nil")
  (testing "Minimal event is converted to point fields"
    (let [point (influxdb/event->point-9 {:host "host-01"
                                          :service "test service"
                                          :time 1428366765
                                          :metric 42.08}
                                         {:tag-fields #{:host}
                                          :precision :seconds})]
      (is (= "test service" (.get measurement point)))
      (is (= TimeUnit/SECONDS (.get precision point)))
      (is (= 1428366765 (.get time-field point)))
      (is (= {"host" "host-01"} (into {} (.get tags point))))
      (is (= {"value" 42.08} (into {} (.get fields point))))))

  (testing "Event is converted with time in milliseconds"
    (let [point (influxdb/event->point-9 {:host "host-01"
                                          :service "test service"
                                          :time 1428366765
                                          :precision :milliseconds
                                          :metric 42.08}
                                         {:tag-fields #{:host}
                                          :precision :seconds})]
      (is (= "test service" (.get measurement point)))
      (is (= 1428366765000 (.get time-field point)))
      (is (= TimeUnit/MILLISECONDS (.get precision point)))
      (is (= {"host" "host-01"} (into {} (.get tags point))))
      (is (= {"value" 42.08} (into {} (.get fields point))))))

  (testing "Event is converted with time in microseconds"
    (let [point (influxdb/event->point-9 {:host "host-01"
                                          :service "test service"
                                          :time 1428366765
                                          :metric 42.08}
                                         {:tag-fields #{:host}
                                          :precision :microseconds})]
      (is (= "test service" (.get measurement point)))
      (is (= 1428366765000000 (.get time-field point)))
      (is (= TimeUnit/MICROSECONDS (.get precision point)))
      (is (= {"host" "host-01"} (into {} (.get tags point))))
      (is (= {"value" 42.08} (into {} (.get fields point))))))

  (testing "Event with ratio field"
    (let [point (influxdb/event->point-9 {:host "host-01"
                                          :service "test service"
                                          :time 1428366765
                                          :metric 21893979/1000000}
                                         {:tag-fields #{:host}
                                          :precision :microseconds})]
      (is (= "test service" (.get measurement point)))
      (is (= 1428366765000000 (.get time-field point)))
      (is (= TimeUnit/MICROSECONDS (.get precision point)))
      (is (= {"host" "host-01"} (into {} (.get tags point))))
      (is (= {"value" (double 21893979/1000000)} (into {} (.get fields point))))))

  (testing "Event is converted with time in microseconds"
    (let [point (influxdb/event->point-9 {:host "host-01"
                                          :service "test service"
                                          :time 1428366765
                                          :precision :microseconds
                                          :metric 42.08}
                                         {:tag-fields #{:host}
                                          :precision :seconds})]
      (is (= "test service" (.get measurement point)))
      (is (= 1428366765000000 (.get time-field point)))
      (is (= TimeUnit/MICROSECONDS (.get precision point)))
      (is (= {"host" "host-01"} (into {} (.get tags point))))
      (is (= {"value" 42.08} (into {} (.get fields point))))))

  (testing "Full event is converted to point fields"
    (let [point (influxdb/event->point-9 {:host "www-dev-app-01.sfo1.example.com"
                                          :service "service_api_req_latency"
                                          :time 1428354941
                                          :metric 0.8025
                                          :state "ok"
                                          :description "A text description!"
                                          :ttl 60
                                          :tags ["one" "two" "red"]
                                          :sys "www"
                                          :env "dev"
                                          :role "app"
                                          :loc "sfo1"
                                          :foo "frobble"}
                                         {:tag-fields #{:host :sys :env :role :loc}
                                          :precision :milliseconds})]
      (is (= "service_api_req_latency" (.get measurement point)))
      (is (= 1428354941000 (.get time-field point)))
      (is (= TimeUnit/MILLISECONDS (.get precision point)))
      (is (= {"host" "www-dev-app-01.sfo1.example.com"
              "sys" "www"
              "env" "dev"
              "role" "app"
              "loc" "sfo1"}
             (into {} (.get tags point))))
      (is (= {"value" 0.8025
              "description" "A text description!"
              "state" "ok"
              "foo" "frobble"}
             (into {} (.get fields point))))))

  (testing ":sys and :loc tags and removed because nil or empty str. Same for :bar and :hello fields"
    (let [point (influxdb/event->point-9 {:host "www-dev-app-01.sfo1.example.com"
                                          :service "service_api_req_latency"
                                          :time 1428354941
                                          :metric 0.8025
                                          :state "ok"
                                          :description "A text description!"
                                          :ttl 60
                                          :tags ["one" "two" "red"]
                                          :sys nil
                                          :env "dev"
                                          :role "app"
                                          :loc ""
                                          :foo "frobble"
                                          :bar nil
                                          :hello ""}
                                         {:tag-fields #{:host :sys :env :role :loc}})]
      (is (= "service_api_req_latency" (.get measurement point)))
      (is (= 1428354941 (.get time-field point)))
      (is (= TimeUnit/SECONDS (.get precision point)))
      (is (= {"host" "www-dev-app-01.sfo1.example.com"
              "role" "app"
              "env" "dev"}
             (into {} (.get tags point))))
      (is (= {"value" 0.8025
              "description" "A text description!"
              "state" "ok"
              "foo" "frobble"}
             (into {} (.get fields point))))))

  (testing "event :tag-fields"
    (let [point (influxdb/event->point-9 {:host "host-01"
                                          :service "test service"
                                          :time 1428366765
                                          :precision :milliseconds
                                          :metric 42.08
                                          :env "dev"
                                          :tag-fields #{:env}}
                                         {:tag-fields #{:host}})]
      (is (= "test service" (.get measurement point)))
      (is (= 1428366765000 (.get time-field point)))
      (is (= {"host" "host-01" "env" "dev"} (into {} (.get tags point))))
      (is (= {"value" 42.08} (into {} (.get fields point)))))))

(deftest point-conversion
  (is (nil? (influxdb/event->point {:service "foo test" :time 1} {:precision :seconds}))
      "Event with no measurement is converted to nil")

  (testing "Event :metric ratio"
    (let [point (influxdb/event->point {:time 1428366765
                                        :influxdb-tags {:foo "bar"
                                                        :bar "baz"}
                                        :precision :milliseconds
                                        :measurement "measurement"
                                        :influxdb-fields {:alice 21893979/1000000}}
                                       {:precision :seconds})]
      (is (= "measurement" (.get measurement point)))
      (is (= 1428366765000 (.get time-field point)))
      (is (= TimeUnit/MILLISECONDS (.get precision point)))
      (is (= {"foo" "bar" "bar" "baz"} (into {} (.get tags point))))
      (is (= {"alice" (double 21893979/1000000)} (into {} (.get fields point))))))

  (testing "Minimal event is converted to point fields"
    (let [point (influxdb/event->point {:time 1428366765
                                        :influxdb-tags {:foo "bar"
                                                        :bar "baz"}
                                        :measurement "measurement"
                                        :influxdb-fields {:alice "bob"}}
                                       {:precision :seconds})]
      (is (= "measurement" (.get measurement point)))
      (is (= TimeUnit/SECONDS (.get precision point)))
      (is (= 1428366765 (.get time-field point)))
      (is (= {"alice" "bob"} (into {} (.get fields point))))))

  (testing "Event is converted with time in milliseconds"
    (let [point (influxdb/event->point {:time 1428366765
                                        :influxdb-tags {:foo "bar"
                                                        :bar "baz"}
                                        :precision :milliseconds
                                        :measurement "measurement"
                                        :influxdb-fields {:alice "bob"}}
                                       {:precision :seconds})]
      (is (= "measurement" (.get measurement point)))
      (is (= 1428366765000 (.get time-field point)))
      (is (= TimeUnit/MILLISECONDS (.get precision point)))
      (is (= {"foo" "bar" "bar" "baz"} (into {} (.get tags point))))
      (is (= {"alice" "bob"} (into {} (.get fields point))))))

  (testing "Event is converted with time in microseconds"
    (let [point (influxdb/event->point {:time 1428366765
                                        :influxdb-tags {:foo "bar"
                                                        :bar "baz"}
                                        :precision :microseconds
                                        :measurement "measurement"
                                        :influxdb-fields {:alice "bob"}}
                                       {:precision :seconds})]
      (is (= "measurement" (.get measurement point)))
      (is (= 1428366765000000 (.get time-field point)))
      (is (= TimeUnit/MICROSECONDS (.get precision point)))
      (is (= {"foo" "bar" "bar" "baz"} (into {} (.get tags point))))
      (is (= {"alice" "bob"} (into {} (.get fields point))))))

  (testing "Event is converted with time in microseconds"
    (let [point (influxdb/event->point {:time 1428366765
                                        :influxdb-tags {:foo "bar"
                                                        :bar "baz"}
                                        :measurement "measurement"
                                        :influxdb-fields {:alice "bob"}}
                                       {:precision :microseconds})]
      (is (= "measurement" (.get measurement point)))
      (is (= 1428366765000000 (.get time-field point)))
      (is (= TimeUnit/MICROSECONDS (.get precision point)))
      (is (= {"foo" "bar" "bar" "baz"} (into {} (.get tags point))))
      (is (= {"alice" "bob"} (into {} (.get fields point))))))

  (testing ":sys and :loc tags are removed because nil or empty str. Same for :bar and :hello fields"
    (let [point (influxdb/event->point {:time 1428366765
                                        :influxdb-tags {:foo "bar"
                                                        :bar "baz"
                                                        :sys ""
                                                        :loc nil}
                                        :precision :milliseconds
                                        :measurement "measurement"
                                        :influxdb-fields {:alice "bob"
                                                          :bar nil
                                                          :hello ""}}
                                       {:precision :seconds})]
      (is (= "measurement" (.get measurement point)))
      (is (= 1428366765000 (.get time-field point)))
      (is (= TimeUnit/MILLISECONDS (.get precision point)))
      (is (= {"foo" "bar" "bar" "baz"} (into {} (.get tags point))))
      (is (= {"alice" "bob"} (into {} (.get fields point)))))))

(deftest get-batchpoint-test
  (testing "No tags, no retention"
    (let [batch-point (influxdb/get-batchpoint {:tags {}
                                                :db "riemann_test"
                                                :retention nil
                                                :consistency "ALL"})]
      (is (= (.getDatabase batch-point) "riemann_test"))
      (is (= (.getRetentionPolicy batch-point) nil))
      (is (= (.getConsistency batch-point) InfluxDB$ConsistencyLevel/ALL))
      (is (= (into {} (.getTags batch-point)) {}))))
  (testing "With tags, no retention"
    (let [batch-point (influxdb/get-batchpoint {:tags {:foo "bar"
                                                       :bar "baz"}
                                                :db "riemann_test"
                                                :retention nil
                                                :consistency "ONE"})]
      (is (= (.getDatabase batch-point) "riemann_test"))
      (is (= (.getRetentionPolicy batch-point) nil))
      (is (= (.getConsistency batch-point) InfluxDB$ConsistencyLevel/ONE))
      (is (= (into {} (.getTags batch-point)) {"foo" "bar" "bar" "baz"}))))
  (testing "With tags, with retention"
    (let [batch-point (influxdb/get-batchpoint {:tags {:foo "bar"
                                                       :bar "baz"}
                                                :db "riemann_test"
                                                :retention "hello"
                                                :consistency "ONE"})]
      (is (= (.getDatabase batch-point) "riemann_test"))
      (is (= (.getRetentionPolicy batch-point) "hello"))
      (is (= (.getConsistency batch-point) InfluxDB$ConsistencyLevel/ONE))
      (is (= (into {} (.getTags batch-point)) {"foo" "bar" "bar" "baz"}))))
  (testing "BigInt metric converted to double"
    (let [point (influxdb/event->point {:time 1428366765
                                        :influxdb-tags {:foo "bar"
                                                        :bar "baz"}
                                        :precision :microseconds
                                        :measurement "measurement"
                                        :influxdb-fields {:alice 1N}}
                                       {:precision :seconds})]
      (is (= "measurement" (.get measurement point)))
      (is (= 1428366765000000 (.get time-field point)))
      (is (= TimeUnit/MICROSECONDS (.get precision point)))
      (is (= {"foo" "bar" "bar" "baz"} (into {} (.get tags point))))
      (is (= {"alice" 1.0} (into {} (.get fields point))))))
(testing "Ratio metric converted to double"
    (let [point (influxdb/event->point {:time 1428366765
                                        :influxdb-tags {:foo "bar"
                                                        :bar "baz"}
                                        :precision :microseconds
                                        :measurement "measurement"
                                        :influxdb-fields {:alice 21893979/1000000}}
                                       {:precision :seconds})]
      (is (= "measurement" (.get measurement point)))
      (is (= 1428366765000000 (.get time-field point)))
      (is (= TimeUnit/MICROSECONDS (.get precision point)))
      (is (= {"foo" "bar" "bar" "baz"} (into {} (.get tags point))))
      (is (= {"alice" (double 21893979/1000000)} (into {} (.get fields point)))))))

(deftest get-batchpoints-test
  (testing "partition by db"
    (let [partition [[{:time 1428366765
                       :influxdb-tags {:foo "bar"}
                       :measurement "measurement"
                       :db "db1"
                       :influxdb-fields {:alice "bob"}}
                      {:time 1428366768
                       :influxdb-tags {:foo "bar"}
                       :db "db1"
                       :measurement "measurement"
                       :influxdb-fields {:alice "bob"}}]
                     [{:time 1428366766
                       :influxdb-tags {:foo "bar"}
                       :measurement "measurement"
                       :db "db2"
                       :influxdb-fields {:alice "bob"}}
                      {:time 1428366767
                       :influxdb-tags {:foo "bar"}
                       :db "db2"
                       :measurement "measurement"
                       :influxdb-fields {:alice "bob"}}]]
          [b1 b2 :as batch-points] (influxdb/get-batchpoints {:consistency "ALL"} partition)]
      (is (= (count batch-points) 2))
      (is (= (.getDatabase b1) "db1"))
      (is (= (.getRetentionPolicy b1) nil))
      (is (= (.getConsistency b1) InfluxDB$ConsistencyLevel/ALL))
      (is (= (into {} (.getTags b1)) {}))
      (is (= (.getDatabase b2) "db2"))
      (is (= (.getRetentionPolicy b2) nil))
      (is (= (.getConsistency b2) InfluxDB$ConsistencyLevel/ALL))
      (is (= (into {} (.getTags b2)) {}))))
  (testing "partition by db, consistency, retention"
    (let [partition [[{:time 1428366765
                       :influxdb-tags {:foo "bar"}
                       :measurement "measurement"
                       :db "db1"
                       :consistency "ALL"
                       :influxdb-fields {:alice "bob"}}
                      {:time 1428366768
                       :influxdb-tags {:foo "bar"}
                       :db "db1"
                       :consistency "ALL"
                       :measurement "measurement"
                       :influxdb-fields {:alice "bob"}}]
                     [{:time 1428366765
                       :influxdb-tags {:foo "bar"}
                       :measurement "measurement"
                       :db "db1"
                       :influxdb-fields {:alice "bob"}}]
                     [{:time 1428366766
                       :influxdb-tags {:foo "bar"}
                       :measurement "measurement"
                       :db "db2"
                       :influxdb-fields {:alice "bob"}}
                      {:time 1428366767
                       :influxdb-tags {:foo "bar"}
                       :db "db2"
                       :measurement "measurement"
                       :influxdb-fields {:alice "bob"}}]
                     [{:time 1428366766
                       :influxdb-tags {:foo "bar"}
                       :measurement "measurement"
                       :db "db2"
                       :retention "hello"
                       :influxdb-fields {:alice "bob"}}]]
          [b1 b2 b3 b4 :as batch-points] (influxdb/get-batchpoints {:consistency "ONE"} partition)]
      (is (= (count batch-points) 4))
      (is (= (.getDatabase b1) "db1"))
      (is (= (.getRetentionPolicy b1) nil))
      (is (= (.getConsistency b1) InfluxDB$ConsistencyLevel/ALL))
      (is (= (into {} (.getTags b1)) {}))
      (is (= (.getDatabase b2) "db1"))
      (is (= (.getRetentionPolicy b2) nil))
      (is (= (.getConsistency b2) InfluxDB$ConsistencyLevel/ONE))
      (is (= (into {} (.getTags b2)) {}))
      (is (= (.getDatabase b3) "db2"))
      (is (= (.getRetentionPolicy b3) nil))
      (is (= (.getConsistency b3) InfluxDB$ConsistencyLevel/ONE))
      (is (= (into {} (.getTags b3)) {}))
      (is (= (.getDatabase b4) "db2"))
      (is (= (.getRetentionPolicy b4) "hello"))
      (is (= (.getConsistency b4) InfluxDB$ConsistencyLevel/ONE))
      (is (= (into {} (.getTags b4)) {})))))

(deftest partition-events-test
  (let [[p1 p2 p3 p4 :as result] (influxdb/partition-events [{:db "db1"
                                                              :consistency "ALL"}
                                                             {:db "db1"}
                                                             {:db "db2"}
                                                             {:db "db2"}
                                                             {:db "db1"
                                                              :consistency "ALL"
                                                              :foo "bar"}
                                                             {:measurement "measurement"
                                                              :db "db2"
                                                              :retention "hello"}])]
    (is (= (count result) 4))
    (is (= (count p1) 2))
    (is (= (first p1) {:db "db1" :consistency "ALL"}))
    (is (= (second p1) {:db "db1" :consistency "ALL" :foo "bar"}))
    (is (= (count p2) 1))
    (is (= (first p2) {:db "db1"}))
    (is (= (first p3) {:db "db2"}))
    (is (= (second p3) {:db "db2"}))
    (is (= (count p3) 2))
    (is (= (first p4) {:db "db2" :measurement "measurement" :retention "hello"}))
    (is (= (count p4) 1)))

  (let [[p1 p2 p3 p4 p5 :as result] (influxdb/partition-events [{}
                                                                {:consistency "ALL"}
                                                                {:consistency "ONE"}
                                                                {:db "db1"}
                                                                {:retention "foo"}
                                                                {:foo "bar"}
                                                                {:retention "foo"
                                                                 :bar "baz"}])]
    (is (= (count result) 5))
    (is (= (count p1) 2))
    (is (= (first p1) {}))
    (is (= (second p1) {:foo "bar"}))
    (is (= (count p2) 1))
    (is (= (first p2) {:consistency "ALL"}))
    (is (= (count p3) 1))
    (is (= (first p3) {:consistency "ONE"}))
    (is (= (count p4) 1))
    (is (= (first p4) {:db "db1"}))
    (is (= (count p5) 2))
    (is (= (first p5) {:retention "foo"}))
    (is (= (second p5) {:retention "foo"
                        :bar "baz"}))))

(deftest converts-double-test
  (is (= (influxdb/converts-double 21893979/1000000) (double 21893979/1000000)))
  (is (= (influxdb/converts-double 3) 3))
  (is (= (influxdb/converts-double 3.1) 3.1))
  (is (= (influxdb/converts-double "foo") "foo"))
  (is (= (influxdb/converts-double 3N) 3.0)))
