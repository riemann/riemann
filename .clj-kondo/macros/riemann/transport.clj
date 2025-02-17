(ns riemann.transport)

(defmacro channel-initializer [& names-and-exprs]
  (let [handlers (partition 2 names-and-exprs)
        shared (filter (comp :shared meta first) handlers)
        pipeline-name (vary-meta (gensym "pipeline")
                                 assoc :tag `ChannelPipeline)
        forms (map (fn [[h-name h-expr]]
                     `(.addLast ~pipeline-name
                                ~(when-let [e (:executor (meta h-name))]
                                   e)
                                ~(str h-name)
                                ~(if (:shared (meta h-name))
                                   h-name
                                   h-expr)))
                   handlers)]
    `(let [~@(apply concat shared)]
       (proxy [ChannelInitializer] []
         (initChannel [~'ch]
           (let [~pipeline-name (.pipeline ^Channel ~'ch)]
             ~@forms
             ~pipeline-name))))))

