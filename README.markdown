Reimann
=======

Reimann is a network event stream processor. It is designed for monitoring,
analytics, and alerts for events from multiple services. Reimann listens on
port 5555 for protocol buffer messages containing events and processes them
through various streams.

You can use Reimann to graph the average rate of requests in your application,
email responsible parties every time an exception is thrown, and plot the 50th,
95th, and 99th percentile latencies for your HTTP service. It is a tool to make
writing comprehensive, site-specific analytics easy.

[Main Reimann Site](http://aphyr.github.com/reimann/)<br />

Plan
====

I built Reimann with the goal of getting it out the door as quickly as
possible. There are many slow or kludgy parts, but they should all be readily
replaceable as I find the time. Top on my list:

- Add a raw Netty UDP listener for accepting events. We lose a lot of time to
aleph.tcp.
- Use Korma/HSQL to implement a faster index for query-heavy installations.
- Think more carefully about time-partitioning functions.
- Reservoir sampling
- Event pubsub
