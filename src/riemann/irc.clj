(ns riemann.irc
  "Forwards events to IRC"
  (:use [clojure.string :only [join upper-case]])
  (:import (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader)))

(defn irc-message
  "Formats an event into a string"
  [e]
  (str (join " " ["Riemann alert on" (str (:host e)) "-" (str (:service e)) "is" (upper-case (str (:state e))) "- Description:" (str (:description e))])))

; IRC client code from http://nakkaya.com/2010/02/10/a-simple-clojure-irc-client/

(declare conn-handler)

(defn connect [server port]
  (let [socket (Socket. server port)
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out (PrintWriter. (.getOutputStream socket))
        conn (ref {:in in :out out})]
    (doto (Thread. #(conn-handler conn)) (.start))
    conn))

(defn write [conn msg]
  (doto (:out @conn)
    (.println (str msg "\r"))
    (.flush)))

(defn conn-handler [conn]
  (while (nil? (:exit @conn))
    (let [msg (.readLine (:in @conn))]
      (println msg)
      (cond
       (re-find #"^ERROR :Closing Link:" msg)
       (dosync (alter conn merge {:exit true}))
       (re-find #"^PING" msg)
       (write conn (str "PONG "  (re-find #":.*" msg)))))))

(defn login [conn nick]
  (write conn (str "NICK " nick))
  (write conn (str "USER " nick " 0 * :" nick)))

(defn join-chan [conn channel]
  (write conn (str "JOIN " channel)))

(defn privmsg [conn channel message-string]
  (write conn (str "PRIVMSG " channel " :" message-string)))

(defn irc
  "Creates an adapter to forward events to IRC. The IRC event will
  contain the host, state, service, metric and description.

  Requires: server, port, name of IRC user to send events, and channel name.

  Can be configured like so:

  (def sendirc (irc \"chat.freenode.net\", 6667, \"riemann-bot\", \"#riemannbot\"))"
  [server port nick channel]
  (def irc-connection (connect server port))
  (login irc-connection nick)
  (join-chan irc-connection channel)
  (fn [e]
    (let [message-string (irc-message e)]
      (privmsg irc-connection channel message-string))))
