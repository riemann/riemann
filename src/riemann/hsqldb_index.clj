(ns riemann.hsqldb-index
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource))
  (:require [riemann.query :as query]
            [riemann.index :as index]
            [clojure.java.jdbc :as jdbc])
  (:use [riemann.time :only [unix-time]]
        riemann.service
        [riemann.index :only [Index]]
        [clojure.string :only [join]]))


(defn quote-column
  [column]
  (str column))

(defn sql-where
  [statement & params]
  {:statement statement
   :params    params})

(defn sql-where-metric-op
  [op value]
  (sql-where (format "((%2$s IS NOT NULL AND %2$s %1$s ?) OR (%3$s IS NOT NULL AND %3$s %1$s ?))"
                     op (quote-column "metric_sint64") (quote-column "metric_f"))
             value value))

(defn sql-where-op
  [op column value]
  (if (= 'metric column)
    (sql-where-metric-op op value)
    (sql-where (format "%s %s ?" (quote-column column) op) value)))


(defn sql-where-eq
  [column value]
  (if (= 'metric column)
    (if (nil? value)
      (sql-where (format "(%s IS NULL AND %s IS NULL)"
                         (quote-column "metric_sint64") (quote-column "metric_f")))
      (sql-where-metric-op "=" value))
    (if (nil? value)
      (sql-where (format "%s IS NULL" (quote-column column)))
      (sql-where-op "=" column value))))

(defn sql-where-join
  [op children]
  {:statement (join (str " " op " ")
                    (map #(str "(" (:statement %) ")") children))
   :params    (apply concat (map #(:params %) children))})

(defn sql-where-not
  [child]
  (apply sql-where (cons
                     (format "NOT (%s)" (:statement child))
                     (:params child))))


(defn translate-ast
  "Translate AST into SQL"
  [query-ast]
  (if (list? query-ast)
    (let [[op & rest-ast] query-ast]
      (condp = op
        'when    (translate-ast (last rest-ast))
        'and     (sql-where-join "AND" (map translate-ast rest-ast))
        'or      (sql-where-join "OR" (map translate-ast rest-ast))
        'not     (sql-where-not (translate-ast (last rest-ast)))
        'member? (sql-where (format "POSITION_ARRAY(? IN %s) != 0"
                                    (quote-column (second rest-ast)))
                            (str (first rest-ast)))
        're-find (sql-where (format "REGEXP_SUBSTRING(%s,?) IS NOT NULL"
                                    (quote-column (last rest-ast)))
                            (str (first rest-ast)))
        '=       (apply sql-where-eq rest-ast)
        '<       (apply sql-where-op op rest-ast)
        '<=      (apply sql-where-op op rest-ast)
        '>       (apply sql-where-op op rest-ast)
        '>=      (apply sql-where-op op rest-ast)
        query-ast))
    query-ast))

(def hsqldb-spec
       {:classname   "org.hsqldb.jdbc.JDBCDriver"
        :subprotocol "hsqldb"
        :subname     "memory:riemann"})

(defn connection-pool
  "Create a connection pool for the given database spec."
  [{:keys [subprotocol subname classname user password
           excess-timeout idle-timeout minimum-pool-size maximum-pool-size]
    :or {excess-timeout (* 30 60)
         idle-timeout (* 3 60 60)
         minimum-pool-size 3
         maximum-pool-size 15}
    :as spec}]
  {:datasource (doto (ComboPooledDataSource.)
                 (.setDriverClass classname)
                 (.setJdbcUrl (str "jdbc:" subprotocol ":" subname))
                 (.setUser user)
                 (.setPassword password)
                 (.setMaxIdleTimeExcessConnections excess-timeout)
                 (.setMaxIdleTime idle-timeout)
                 (.setMinPoolSize minimum-pool-size)
                 (.setMaxPoolSize maximum-pool-size))})

(defn delay-pool
  "Return a delay for creating a connection pool for the given spec."
  [spec]
  (delay (connection-pool spec)))

(defn get-connection
  "Get a connection from the potentially delayed connection object."
  [db]
  (if-not db
    (throw (Exception. "No valid DB connection selected."))
    (if (delay? db)
      @db
      db)))


(defn create-index-for-column-ddl
  [table column & {:keys [entities] :or {entities identity}}]
  (format "CREATE INDEX %s ON %s (%s)"
          (jdbc/as-sql-name entities (str (name table) "_" (name column)))
          (jdbc/as-sql-name entities table)
          (jdbc/as-sql-name entities column)))

(def default-connection
  (delay-pool hsqldb-spec))

(defn execute-query
  [db-spec query]
  (let [ast (query/ast query)
        query (translate-ast ast)
        sql (format "SELECT * FROM events WHERE %s" (:statement query))]
    (jdbc/query db-spec (concat [sql] (:params query)))))

(def table-index-columns
  [:state :service :host :description :tags :ttl :metric_sint64 :metric_f :time])

(def schema-statements
  (concat
    ["DROP TABLE IF EXISTS events"
     (jdbc/create-table-ddl
       :events
       [:key "VARBINARY(2048) PRIMARY KEY"]
       [:time "BIGINT"]
       [:state "VARCHAR(1024)"]
       [:service "VARCHAR(1024)"]
       [:host "VARCHAR(1024)"]
       [:description "VARCHAR(1024)"]
       [:tags "VARCHAR(1024) ARRAY DEFAULT ARRAY[]"]
       [:ttl "FLOAT"]
       [:attributes "VARCHAR(2048)"]
       [:metric_sint64 "BIGINT"]
       [:metric_f "FLOAT"])]
    (map #(create-index-for-column-ddl :events %) table-index-columns)))

(defn create-schema!
  [db-spec]
  (doseq [sql schema-statements]
    (jdbc/execute! db-spec [sql])))

(defn primary-key-for-event
  "Provide the primary key for an event"
  [event]
  (.getBytes (str (:host event) \ufffe (:service event))))

(defn insert-event
  [db-spec event]
  (let [event-with-key (assoc event :key (primary-key-for-event event))]
    (jdbc/insert! db-spec :events event-with-key)))


; (kd/with-db conn
;             (kc/insert events (kc/values {:service "testing2" :tags (kc/raw "ARRAY['t1', 't2']")})))

; (kd/with-db conn
;             (kc/exec-raw ["SELECT * from \"events\" where POSITION_ARRAY('co''ld' IN \"tags\") != 0"] :results))

; (kd/with-db conn
;             (kc/exec-raw ["INSERT INTO \"events\" (\"service\", \"tags\") VALUES ?, ?;" ["testing3" (.createArrayOf (j/connection) "VARCHAR" (into-array ["one" "tw'o"]))]]))

; (kd/with-db conn
;             (kc/insert events (kc/values {:service "sweet" :tags (.createArrayOf (j/connection) "VARCHAR" (into-array ["one" "'sweet'"]))})))

; (seq (.getArray (:tags (first (kd/with-db conn
;             (kc/exec-raw ["SELECT * from \"events\" where POSITION_ARRAY(? IN \"tags\") != 0" ["'sweet'"]] :results))))))


(defn hsqldb-index
  "Create a new HSQLDB backed index"
  []
  (let [conn (delay-pool hsqldb-spec)]
    (reify
      Index
      (clear [this]
        ;(kd/with-db conn (kc/delete events))
        )

      (delete [this event]
        ; (kd/with-db conn
        ;             (kc/delete events
        ;                        (kc/where {:key (primary-key-for-event event)})))
        )

      (delete-exactly [this event]
        ;(.remove hm [(:host event) (:service event)] event))
        )

      (expire [this]
        ;(filter
        ;  (fn [{:keys [ttl time] :or {ttl index/default-ttl} :as state}]
        ;    (let [age (- (unix-time) time)]
        ;      (when (> age ttl)
        ;        (delete this state)
        ;        true)))
        ;  ; (.values hm)))
        ;  )
        )

      (search [this query-ast]
        "O(n), sadly."
        ; (let [matching (query/fun query-ast)]
        ;   (filter matching (.values hm))))
        )

      (update [this event]
        ;(if (= "expired" (:state event))
        ;  (delete this event)
        ;  ;(.put hm [(:host event) (:service event)] event))
        )

      (lookup [this host service]
        ;(.get hm [host service])
        )

      clojure.lang.Seqable
      (seq [this]
        ; (seq (.values hm))
        )

      ServiceEquiv
      (equiv? [this other] (= (class this) (class other)))

      Service
      (conflict? [this other] false)
      (reload! [this new-core])
      (start! [this])
      (stop! [this]))))
