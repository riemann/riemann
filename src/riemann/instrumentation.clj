(ns riemann.instrumentation
  "Tracks Riemann performance data"
  (:use [riemann.time :only [unix-time]]
        [riemann.common :only [event localhost]]
        [interval-metrics.core :only [Metric
                                      update!
                                      snapshot!
                                      rate
                                      quantile
                                      uniform-reservoir]]))

(defprotocol Instrumented
  "These things can can be asked to report events about their performance and
  health."
  (events [this]
          "Returns a sequence of events describing the current state of the
          object."))

(defn nanos->millis
  "Convert nanoseconds to milliseconds."
  [nanos]
  (when nanos
    (* 1e-6 nanos)))

(defrecord RateLatency [event quantiles rate latencies]
  Metric
  (update! [this time]
           (update! latencies time)
           (update! rate 1))

  Instrumented
  (events [this]
          (let [rate      (snapshot! rate)
                latencies (snapshot! latencies)
                t         (unix-time)]
            (cons
              ; Rate
              (merge event {:service  (str (:service event) " rate")
                            :metric   rate
                            :time     t})
              ; Latencies
              (map (fn [q]
                     (merge event {:service (str (:service event) " latency " q)
                                   :metric  (-> latencies
                                              (quantile q)
                                              nanos->millis)
                                   :time    t}))
                   quantiles)))))

(defn rate+latency
  "Returns a Metric which can be updated with latency measurements. When asked
  for events, returns a rate of total throughput, plus the given quantiles of
  latency metrics. Takes an optional base event which is used as the template.
  Input latencies are in nanoseconds (for storage as longs), emits latencies as
  doubles in milliseconds."
  ([event] (rate+latency event [0.0 0.5 0.95 0.99 0.999]))
  ([ev quantiles]
   (let [ev (merge ev {:service (str "riemann " (:service ev))})]
     (RateLatency. ev quantiles (rate) (uniform-reservoir)))))

(defn instrumented?
  "Does a thingy provide instrumentation?"
  [thingy]
  (satisfies? Instrumented thingy))

(defmacro measure-latency
  "Wraps body in a macro which reports its running time in nanoseconds to a
  Metric."
  [metric & body]
  `(let [t0#    (System/nanoTime)
         value# (do ~@body)
         t1#    (System/nanoTime)]
     (update! ~metric (- t1# t0#))
     value#))
