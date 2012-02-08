(ns reimann.test.index
  (:use [reimann.index])
  (:use [reimann.query])
  (:use [reimann.common])
  (:use [clojure.test]))

(deftest nbhm-update
         (let [i (nbhm-index)]
           (update i {:host 1})
           (update i {:host 2})
           (update i {:host 1 :service 3 :state :ok})
           (update i {:host 1 :service 3 :description "new"})

           (is (= (set (.values i))
                  #{{:host 1}
                    {:host 2}
                    {:host 1 :service 3 :description "new"}}))))

(deftest nhbm-delete
         (let [i (nbhm-index)]
           (update i {:host 1})
           (update i {:host 2})
           (delete i {:host 1 :service 1})
           (delete i {:host 2 :state :ok})
           (is (= (set (.values i))
                  #{{:host 1}}))))

(deftest nhbm-search
         (let [i (nbhm-index)]
           (update i {:host 1})
           (update i {:host 2 :service "meow"})
           (update i {:host 3 :service "mrrrow"})
           (is (= (set (search i (ast "host >= 2 and not service =~ \"%r%\"")))
                  #{{:host 2 :service "meow"}}))))

(deftest nhbm-expire
         (let [i (nbhm-index)]
           (update i {:host 1 :ttl 0 :time (unix-time)})
           (update i {:host 2 :ttl 10 :time (unix-time)})
           (update i {:host 3 :ttl 20 :time (- (unix-time) 21)})

           (let [expired (expire i)]
             (is (= (set (map (fn [e] (:host e)) 
                              expired))
                    #{1 3})))

           (is (= (map (fn [e] (:host e))
                       (.values i))
                  [2]))))
