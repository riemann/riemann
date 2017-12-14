(ns riemann.xymon-test
  (:require [riemann.logging :as logging]
            [riemann.time :refer [unix-time]]
            [riemann.xymon :refer :all]
            [clojure.test :refer :all]))

(logging/init)

(deftest ^:xymon-host-xymon host->xymon-test
  (let [pairs [["foo" "foo"]
               ["example.com" "example,com"]
               ["foo.example.com" "foo,example,com"]]]
    (doseq [[hostname xymon-hostname] pairs]
      (is (= xymon-hostname (host->xymon hostname))))))

(deftest ^:xymon-service-xymon service->xymon-test
  (let [pairs [["foo" "foo"]
               ["service name" "service_name"]
               ["service.name" "service_name"]
               ["s   erv..ice" "s___erv__ice"]]]
    (doseq [[service xymon-service] pairs]
      (is (= xymon-service (service->xymon service))))))

(deftest ^:xymon-format event->status-test
  (let [pairs [[{}
                "status . unknown \n"]
               [{:host "foo" :service "bar"}
                "status foo.bar unknown \n"]
               [{:host "foo" :service "bar" :state "ok"}
                "status foo.bar ok \n"]
               [{:host "foo" :service "bar" :state "ok" :description "blah"}
                "status foo.bar ok blah\n"]
               [{:host "foo" :service "bar" :state "ok" :ttl 300}
                "status+5 foo.bar ok \n"]
               [{:host "foo" :service "bar" :state "ok" :ttl 61}
                "status+2 foo.bar ok \n"]
               [{:host "foo" :service "bar" :state "ok" :ttl 59}
                "status+1 foo.bar ok \n"]
               [{:host "foo" :service "bar" :state "ok" :ttl 1}
                "status+1 foo.bar ok \n"]
               [{:host "foo" :service "bar" :state "ok" :ttl 0}
                "status+0 foo.bar ok \n"]
               [{:host "example.com" :service "some.metric rate" :state "ok"}
                "status example,com.some_metric_rate ok \n"]]]
    (doseq [[event line] pairs]
      (is (= line (event->status event))))))

(deftest ^:xymon-enable event->enable-test
  (let [pairs [[{}
                "enable .*"]
               [{:host "foo"}
                "enable foo.*"]
               [{:host "foo" :service "bar"}
                "enable foo.bar"]
               [{:host "foo.example.com" :service "bar"}
                "enable foo,example,com.bar"]
               [{:host "foo" :service "b  a.r"}
                "enable foo.b__a_r"]]]
    (doseq [[event line] pairs]
      (is (= line (event->enable event))))))

(deftest ^:xymon-disable event->disable-test
  (let [pairs [[{:ttl 123}
                "disable .* 3 "]
               [{:host "foo" :ttl 300}
                "disable foo.* 5 "]
               [{:host "foo" :service "bar" :ttl 1}
                "disable foo.bar 1 "]
               [{:host "foo" :service "bar" :description "desc" :ttl 61}
                "disable foo.bar 2 desc"]
               [{:host "foo.example.com" :service "bar service" :ttl 59
                 :description "desc"}
                "disable foo,example,com.bar_service 1 desc"]]]
    (doseq [[event line] pairs]
      (is (= line (event->disable event))))))

(deftest ^:xymon-combo events->combo-test
  (let [formatter #(str % "_s")
        long-message (clojure.string/join (repeat 2048 "o"))
        long-message_s (str long-message "_s")
        pairs [[["foo"] ["foo_s"]]
               [["foo" "bar" "asdf"]
                ["combo\nfoo_s\n\nbar_s\n\nasdf_s\n\n"]]
               [[long-message long-message]
                [long-message_s long-message_s]]
               [[long-message "foo" long-message]
                [(str "combo\n" long-message_s "\n\nfoo_s\n\n")
                 long-message_s]]]]
    (doseq [[events result] pairs]
      (is (= result
             (events->combo formatter events))))))

(deftest ^:xymon ^:integration xymon-test
         (let [k (xymon nil)]
           (k {:host "riemann.local"
               :service "xymon test"
               :state "green"
               :description "all clear, uh, situation normal"
               :metric -2
               :time (unix-time)}))

         (let [k (xymon nil)]
           (k {:service "xymon test"
               :state "green"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [k (xymon nil)]
           (k {:host "riemann.local"
	       :service "xymon test"
               :state "ok"
               :description "all clear, uh, situation normal"
               :metric 3.14159
               :time (unix-time)}))

         (let [k (xymon nil)]
           (k {:host "no-service.riemann.local"
               :state "ok"
               :description "Missing service, not transmitted"
               :metric 4
               :time (unix-time)})))
