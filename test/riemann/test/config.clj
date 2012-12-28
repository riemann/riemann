(ns riemann.test.config
  (:use riemann.config
        clojure.test
        [riemann.index :only [Index]])
  (:require [riemann.core :as core]
            [riemann.pubsub :as pubsub]
            [riemann.logging :as logging]
            [riemann.streams :as streams]))

(defn reset-core! [f]
  (logging/suppress "riemann.core"
                    (clear!)
                    (core/stop! @core)
                    (reset! core (core/core))
                    (f)
                    (clear!)
                    (reset! core (core/core))
                    (core/stop! @core)))

(use-fixtures :each reset-core!)

(deftest blank-test
         (is (empty? (:streams @core)))
         (is (empty? (:streams @next-core)))
         (is (empty? (:services @core)))
         (is (empty? (:services @core))))

(deftest apply-test
         (is (not= @core @next-core))
         (let [old-next-core @next-core]
           (apply!)
           (is (= old-next-core @core))
           (is (not= @core @next-core))))

(deftest tcp-server-test
         (tcp-server :host "a")
         (is (= "a" (:host (first (:services @next-core)))))
         (is (empty? (:services @core))))

(deftest udp-server-test
         (udp-server :host "b")
         (is (= "b" (:host (first (:services @next-core)))))
         (is (empty? (:services @core))))

(deftest ws-server-test
         (ws-server :port 1234)
         (is (= 1234 (:port (first (:services @next-core)))))
         (is (empty? (:services @core))))

(deftest graphite-server-test
         (graphite-server :port 1)
         (is (= 1 (:port (first (:services @next-core)))))
         (is (empty? (:services @core))))

(deftest streams-test
         (streams :a)
         (streams :b)
         (is (= [:a :b] (:streams @next-core)))
         (is (empty? (:streams @core))))

(deftest index-test
         (let [i (index)]
           (is (satisfies? Index i))
           (is (= i (:index @next-core)))
           (is (nil? (:index @core)))))

(deftest update-index-test
         (let [i (index)
               up (update-index i)]
           (apply!)
           (up {:service 1 :state "ok"})
           (is (= (seq i) [{:service 1 :state "ok"}]))))

(deftest subscribe-in-stream-test
         (let [received (promise)]
           (streams
             (streams/where (service "test-in")
                            (publish :test))
             (subscribe :test (partial deliver received)))
           (apply!)
           
           ; Send through streams
           ((first (:streams @core)) {:service "test-in"})
           (is (= {:service "test-in"} @received))))

(deftest subscribe-outside-stream-test
         (let [received (promise)]
           (subscribe :test (partial deliver received))
           (apply!)

           ; Send outside streams
           (pubsub/publish (:pubsub @core) :test "hi")
           (is (= "hi" @received))))
