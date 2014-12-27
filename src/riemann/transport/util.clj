(ns riemann.transport.util
  "Helpers for Netty stuff."
  (:import [io.netty.channel ChannelHandler
                             ChannelInboundHandler
                             ChannelOutboundHandler]))

(defn tap [n]
  (reify
    ChannelHandler
    ChannelInboundHandler
    ChannelOutboundHandler

    (handlerAdded [_ x]
      (prn n :handler-added x))

    (handlerRemoved [_ x]
      (prn n :handler-removed x))

    (exceptionCaught [_ ctx cause]
      (prn n :exception-caught ctx)
      (.printStackTrace cause)
      (.fireExceptionCaught ctx cause))

    (channelRegistered [_ ctx]
      (prn n :channel-registered ctx)
      (.fireChannelRegistered ctx))

    (channelUnregistered [_ ctx]
      (prn n :channel-unregistered ctx)
      (.fireChannelUnregistered ctx))

    (channelActive [_ ctx]
      (prn n :channel-active ctx)
      (.fireChannelActive ctx))

    (channelInactive [_ ctx]
      (prn n :channel-inactive ctx)
      (.fireChannelInactive ctx))

    (channelRead [_ ctx msg]
      (prn n :channel-read ctx msg)
      (.fireChannelRead ctx msg))

    (channelReadComplete [_ ctx]
      (prn n :channel-read-complete ctx)
      (.fireChannelReadComplete ctx))

    (userEventTriggered [_ ctx event]
      (prn n :user-event-triggered ctx event)
      (.fireUserEventTriggered ctx event))

    (channelWritabilityChanged [_ ctx]
      (prn n :channel-writability-changed ctx)
      (.fireChannelWritabilityChanged ctx))

    (bind [_ ctx local-address promise]
      (prn n :bind ctx local-address promise)
      (.bind ctx local-address promise))

    (connect [_ ctx remote-address local-address promise]
      (prn n :connect ctx remote-address local-address promise)
      (.connect ctx remote-address local-address promise))

    (disconnect [_ ctx promise]
      (prn n :disconnect ctx promise)
      (.disconnect ctx promise))

    (close [_ ctx promise]
      (prn n :close ctx promise)
      (.close ctx promise))

    (read [_ ctx]
      (prn n :read ctx)
      (.read ctx))

    (write [_ ctx msg promise]
      (prn n :write ctx msg promise)
      (.write ctx msg promise))

    (flush [_ ctx]
      (prn n :flush ctx)
      (.flush ctx))))
