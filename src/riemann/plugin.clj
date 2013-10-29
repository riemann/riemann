(ns riemann.plugin
  "Simple plugin loader for riemann.

  Riemann already offers the ability to load jar files in the classpath
  through its initialization scripts. This namespace allows walking the
  classpath, looking for small meta files distributed with plugins which
  hint at the requires.

  This allows `load-plugins` or `load-plugin` to make new functions and
  streams available in the config file without resorting to manual
  requires.

  The meta file distributed with plugins is expected to be an EDN file
  containing at least two keys:

  - `plugin`: the plugin name
  - `require`: a vector of require statements to hand over to
               `clojure.core/require`

"
  (:require [clojure.java.classpath :as cp]
            [clojure.java.io        :refer [resource]]
            [clojure.tools.logging  :refer [info]]))

(defn read-safely
  "Read data without evaluation"
  [src]
  (binding [*read-eval* false]
    (read-string src)))

(defn load-from-meta
  "Given a metadata description map, follow requirements"
  [{:keys [plugin require]}]
  (when (seq require)
    (info "loading plugin:" plugin)
    (apply clojure.core/require require)))

(defn load-from-resource
  "Given a path to a java resource, load metadata and apply
   given requirements."
  [src]
  (-> src
      resource
      slurp
      read-safely
      load-from-meta))

(defn load-plugins
  "Walk classpath and try to load all plugins that were found."
  []
  (info "walking classpath to find plugins")
  (let [plugin-desc? (partial re-matches #"riemann_plugin/(.*)/meta.edn$")
        files        (mapcat cp/filenames-in-jar (cp/classpath-jarfiles))]
    (doseq [desc-file (filter plugin-desc? files)]
      (load-from-resource desc-file))))

(defn load-plugin
  "Given a plugin name, look for its metadata description file, execute
   given requirements."
  [plugin]
  (load-from-resource (format "riemann_plugin/%s/meta.edn" (name plugin))))

