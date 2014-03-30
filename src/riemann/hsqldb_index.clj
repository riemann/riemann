(ns riemann.hsqldb-index
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource))
  (:require [riemann.query :as query]
            [riemann.index :as index]
            [riemann.codec :as codec]
            [riemann.common :as common]
            [clojure.java.jdbc :as jdbc]
            [cheshire.core :as json])
  (:use [riemann.time :only [unix-time]]
        riemann.service
        [riemann.index :only [Index default-ttl]]
        [clojure.string :only [join]]))


(def last-hsqldb-id
  "The most recent hsqldb id"
  (atom 0))

(defn hsqldb-id
  "Returns a new unique hsqldb id."
  []
  (swap! last-hsqldb-id inc))

(def hsqldb-spec
  {:classname         "org.hsqldb.jdbc.JDBCDriver"
   :subprotocol       "hsqldb"
   :subname           (str "mem:riemann-" (hsqldb-id))
   :maximum-pool-size 20})

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


(defn create-index-for-column-ddl
  [table column & {:keys [entities] :or {entities identity}}]
  (format "CREATE INDEX %s ON %s (%s)"
          (jdbc/as-sql-name entities (str (name table) "_" (name column)))
          (jdbc/as-sql-name entities table)
          (jdbc/as-sql-name entities column)))

(def table-index-columns
  [:state :service :host :description :tags :ttl :metric_sint64 :metric_f :time])

(def schema-statements
  (concat
    ["DROP TABLE IF EXISTS events"
     (jdbc/create-table-ddl
       :events
       [:key            "VARBINARY(2048) PRIMARY KEY"]
       [:time           "BIGINT"]
       [:state          "VARCHAR(1024)"]
       [:service        "VARCHAR(1024)"]
       [:host           "VARCHAR(1024)"]
       [:description    "VARCHAR(1024)"]
       [:tags           "VARCHAR(1024) ARRAY DEFAULT ARRAY[]"]
       [:ttl            "FLOAT"]
       [:attributes     "VARCHAR(4096)"]
       [:metric_sint64  "BIGINT"]
       [:metric_f       "FLOAT"])]
    (map #(create-index-for-column-ddl :events %) table-index-columns)))

(defn create-schema!
  [db-spec]
  (doseq [sql schema-statements]
    (jdbc/execute! db-spec [sql])))


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
  (apply sql-where
         (cons
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

(defn row-to-event
  [row]
  (let [metric      (if (:metric_sint64 row)
                      (:metric_sint64 row)
                      (:metric_f row))
        attributes  (json/parse-string (:attributes row) keyword)
        tags        (seq (.getArray (:tags row)))
        event       (assoc (select-keys row codec/event-keys)
                      :metric metric
                      :tags   tags)]
    (common/event (merge event attributes))))

(defn find-all-events
  [db-spec]
  (jdbc/query db-spec "SELECT * FROM events" :row-fn row-to-event))


(defn find-events
  [db-spec query-ast]
  (let [query (translate-ast query-ast)
        sql (format "SELECT * FROM events WHERE %s" (:statement query))]
    (jdbc/query db-spec (concat [sql] (:params query)) :row-fn row-to-event)))

(defn primary-key-for-event
  "Provide the primary key for an event"
  [event]
  (.getBytes (str (:host event) \ufffe (:service event))))

(defn insert-event
  [db-spec event]
  (jdbc/with-db-transaction [t-con db-spec]
    (let [primary-key       (primary-key-for-event event)
          metric            (:metric event)
          metric-column     (if (and (integer? metric) (<= Long/MIN_VALUE metric Long/MAX_VALUE))
                              :metric_sint64
                              :metric_f)
          custom-attributes (apply dissoc event codec/event-keys)
          event-for-insert  (assoc (select-keys event (disj codec/event-keys :metric))
                              :key          primary-key
                              :tags         (.createArrayOf (jdbc/db-find-connection t-con) "VARCHAR" (into-array (:tags event)))
                              metric-column metric
                              :attributes   (json/generate-string custom-attributes)
                              :ttl          (or (:ttl event) default-ttl)
                              :time         (long (or (:time event) (unix-time)))
                              :state        (if (:state event) (name (:state event))))]
      (jdbc/delete! t-con :events ["key = ?" primary-key])
      (jdbc/insert! t-con :events event-for-insert))))

(defn delete-event
  [db-spec event]
  (let [primary-key (primary-key-for-event event)]
    (jdbc/delete! db-spec :events ["key = ?" primary-key])))

(defn find-expired-events
  [db-spec]
  (jdbc/query db-spec
              ["SELECT * FROM events WHERE ? - time > ttl" (long (unix-time))]
              :row-fn row-to-event))

(defn hsqldb-index
  "Create a new HSQLDB backed index"
  []
  (let [db-spec (connection-pool hsqldb-spec)]
    (create-schema! db-spec)
    (reify
      Index
      (clear [this]
        (jdbc/delete! db-spec :events))

      (delete [this event]
        (delete-event db-spec event))

      (delete-exactly [this event]
        ;(.remove hm [(:host event) (:service event)] event))
        (.delete this event))

      (expire [this]
        (let [events (find-expired-events db-spec)]
          (doseq [event events]
            (.delete this event))
        events))

      (search [this query-ast]
        "Super fast and indexed"
        (find-events db-spec query-ast))

      (update [this event]
        (if (= "expired" (:state event))
          (.delete this event)
          (insert-event db-spec event)))

      (lookup [this host service]
        (first (.search this (list 'and
                                   (list '= 'host host)
                                   (list '= 'service service)))))

      clojure.lang.Seqable
      (seq [this]
        (find-all-events db-spec))

      ServiceEquiv
      (equiv? [this other] (= (class this) (class other)))

      Service
      (conflict? [this other] false)
      (reload! [this new-core])
      (start! [this])
      (stop! [this]
        (jdbc/execute! db-spec ["SHUTDOWN"])))))
