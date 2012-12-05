(ns riemann.repl
  "The riemann REPL server is a bit of a special case. Since it controls almost
  every aspect of Riemann--and can shut those aspects down--it needs to live
  above them. While you usually *start* a repl server from the config file, it
  is not bound to the usual config lifecycle and won't be shut down or
  interrupted during config reload."
  (:require [clojure.tools.nrepl.server :as nrepl]))

(def server nil)

(defn stop-server!
  "Stops the REPL server."
  []
  (when-let [s server]
    (nrepl/stop-server s))
  (def server nil))

(defn start-server!
  "Starts a new repl server. Options:
  
  :host (default \"127.0.0.1\")
  :port (default 5557)"
  [opts]
  (stop-server!)
  (let [opts (apply hash-map opts)]
    (def server (nrepl/start-server
                  :port (get opts :port 5557)
                  :bind (get opts :host "127.0.0.1")))))

(defn start-server
  "Starts a new REPL server, when one isn't already running."
  [opts]
  (when-not server (start-server! opts)))
