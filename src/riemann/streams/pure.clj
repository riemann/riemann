(ns riemann.streams.pure
  "Riemann streams have performed exceptionally well, but their design scope
  was intentionally limited. Users consistently request:

  - Consistent behavior with respect to time intervals
  - Persisting stream state across reloads and process restarts
  - Replicating streams across multiple nodes

  These suggest:

  - Streams should be atomically coerceable to data structures for storage and
    exchange between nodes.

  - Streams need globally unique identifiers so we can handle multiple streams
    feeding into the same child stream.

  - Streams should be deterministic functions of their input events, instead of
    depending on wall-clock time.

  - Streams should be as pure as possible--side effect should be explicitly
    identified and controlled for distribution and replayability.


  And we wish to preserve:

  - Performance: tens of millions of events per second is nice.

  - Implicit parallelism: it's easy to get determinism in a single thread, but
    we can and should do better.

  - The existing stream syntax, including macros like (by).


  Useful existing assumptions:

  - Events are roughly time-ordered, and we have no problem rejecting events
  that appear too far outside our latency window.


  New assumptions:

  - Events are uniquely identified by [host, service, time, seq] identifiers.

  - Events are deterministically partitioned into time windows. Windows are
  monotonically advancing; all streams see windows in strictly sequential
  order.

  ## Ideas on Time

  Peter Alvaro has convinced me of the obvious (lol) idea that a distributed
  stream processor must have some way to *seal* the events--to tell when a
  given stream has seen all inputs for a particular interval, so that it can in
  turn send state downstream.

  Presently, sealing is performed as a global side effect from the
  riemann.time scheduler, which causes weird latency anomalies--for instance,
  chaining 3 windows together introduces 3x window latency, and if multiple
  streams are recombined we'll happily interleave events from different times.
  Also, this fucks with determinism.

  I think the right move is to thread sealing information through the same
  paths that connect streams to one another, but this introduces a new problem.
  Right now, Riemann streams only have links to their children, not their
  parents. In order for a stream to know it's received all its inputs, it must
  know how many producers are sending it messages.

  We could do this by sending an 'initiation pulse' through the topology, where
  each parent informs its child that it exists. Happily, producer relationships
  are local, not global: a stream only has to know about its immediate
  producers.

  A difficulty: (by) creates producers dynamically. I THINK this is the only
  place where Riemann topologies shift at runtime. Can we figure out a solution
  that works for (by)?

  A specific case: (by) has one child stream c0, which has events for the
  current window w0. (by) receives an event e which creates a new child stream
  c0. All child streams unify at a final stream f.


        /--- c0 ---\
      by            f
        \--- c1 ---/


  Case 1: e falls in w0. Create child c1 and inform it that the current window
  is w0. c1 sends an initiation pulse for w0 to f, causing f to increment its
  producer count for w0. If by can only seal w0 once c1's initiation pulse has
  completely propagated, f will not seal w0 until c1 has had a chance to
  forward its w0 events to f as well.

  Case 2: e falls in w1. Seal w0 and inform c0 as usual. Move to window w1,
  create c1 and proceed as in Case 1.

  Implication: initiation and sealing pulses require synchronous
  acknowledgement--we have to wait for the initiation to propagate all the way
  downstream to make sure that every downstream node is aware of its new
  producer. It's fine to emit events concurrently (cuz the stream may be able
  to do parallel processing) so long as sealing is strictly ordered.

  Another implication: Sealing pulses must be ordered w.r.t the event stream.
  We have to acquire something like a ReaderWriterLock to ensure no threads are
  operating on our children while we seal them. That means acquiring a readlock
  on every. single. event. Fuck.

  Alternate strategy: queues between everything. We are going for CSP
  invariants, after all, and that's what Akka does! That means locks AND
  allocations and pointer chasing for every event. Fuck no.

  Alternate strategy: most configs involve tens of branches. Branches can
  execute in parallel: we could enforce a single-thread-per-branch rule. When
  branches recombine, though, we gotta introduce a queue or a lock. Seems less
  bad. OTOH, if only one thread can ever execute a given stream, we can stop
  using atoms and move all stream state to volatile mutables, which could
  dramatically reduce memory barriers. And the initiation pulses tell us when
  we need locks: if you only have one producer, you don't need to lock.

  If we use queues and enforce strict thread affinity for streams, we could get
  away with unsynchronized mutables, not just volatile ones.

  UGH")
