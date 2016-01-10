(ns riemann.irc
  "Forwards events to IRC."
  (:refer-clojure :exclude [replace])
  (:import
   (java.net Socket
             DatagramSocket
             DatagramPacket
             InetAddress)
   (java.io Writer OutputStreamWriter))
   (:use [clojure.string :only [join upper-case]]
        clojure.tools.logging
        riemann.pool
        riemann.common
        [riemann.transport :only [resolve-host]]))

(defprotocol IrcClient
  (open [client]
        "Creates an IRC client")
  (login [client nick channel]
         "Logs into IRC")
  (send-line [client channel line]
        "Sends a formatted line to IRC")
  (close [client]
         "Cleans up (closes sockets etc.)"))

(defrecord IrcConnection [^String host ^int port]
  IrcClient
  (open [this]
    (let [sock (Socket. host port)]
      (assoc this
             :socket sock
             :out (OutputStreamWriter. (.getOutputStream sock)))))
  (login [this nick channel]
    (let [out (:out this)]
      (.write ^OutputStreamWriter out ^String (str "NICK " nick "\n"))
      (.write ^OutputStreamWriter out ^String (str "USER " nick " 0 * :" nick "\n"))
      (.write ^OutputStreamWriter out ^String (str "JOIN " channel "\n"))
      (.flush ^OutputStreamWriter out)))
  (send-line [this channel line]
    (let [out (:out this)]
      (.write ^OutputStreamWriter out ^String (str "PRIVMSG " channel " :" line "\n"))
      (.flush ^OutputStreamWriter out)))
  (close [this]
    (.close ^OutputStreamWriter (:out this))
    (.close ^Socket (:socket this))))

(defn irc-message
  "Formats an event into a string"
  [e]
  (str (join " " ["Riemann alert on" (str (:host e)) "-" (str (:service e)) "is" (upper-case (str (:state e))) "- Description:" (str (:description e))])))

(defn irc
  "Returns a function which accepts an event and sends it to IRC.
  Silently drops events when IRC is down. Attempts to reconnect
  automatically every five seconds. Use:

  (irc {:host \"chat.freenode.net\" :port 6667})

  Options:

  :nick                 The nick to use to send messages. Defaults to 'riemann-bot'.

  :channel              The channel to join.

  :pool-size            The number of connections to keep open. Default 1.

  :reconnect-interval   How many seconds to wait between attempts to connect.
                        Default 5.

  :claim-timeout        How many seconds to wait for an IRC connection from
                        the pool. Default 0.1.

  :block-start          Wait for the pool's initial connections to open
                        before returning."

  [opts]
  (let [opts (merge {:host "chat.freenode.net"
                     :port 6667
                     :nick "riemann-bot"
                     :channel "#riemannbot"
                     :claim-timeout 0.1
                     :pool-size 1} opts)
        pool (fixed-pool
               (fn []
                 (info "Connecting to " (select-keys opts [:host :port]))
                 (let [host (resolve-host (:host opts))
                       port (:port opts)
                       nick (:nick opts)
                       channel (:channel opts)
                       client (open (IrcConnection. host port))
                       conn (login client nick channel)]
                   (info "Connected to" host)
                   client))
               (fn [client]
                 (info "Closing connection to "
                       (select-keys opts [:host :port]))
                 (close client))
               (-> opts
                   (select-keys [:block-start])
                   (assoc :size (:pool-size opts))
                   (assoc :regenerate-interval (:reconnect-interval opts))))]
    (fn [event]
        (with-pool [client pool (:claim-timeout opts)]
          (send-line client (:channel opts) (irc-message event))))))
