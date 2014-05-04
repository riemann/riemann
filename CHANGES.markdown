# Version 0.2.5

All kinds of goodies! We're long overdue for a release, with five new service
integrations, a host of performance and correctness improvements, streamlined
packaging, new folds for combining events, and new streams for high-throughput
IO. Plus we've added lots of documentation, cleaned up some inconsistent
corners of the API, and improved error messages for common mistakes. All of
this should make for a faster and easier-to-use Riemann. Happy monitoring!

## Bugfixes
- Connection pools with block-start :true work correctly now
- /etc/default/riemann is now a conffile; won't be overwritten by upgrades
- Correct location for defaults file on redhat OSes
- Default value for localhost name
- streams/top correctly emits records to both top and bottom streams
- Indexing an expired event removes it from the index, rather than being a noop
- riemann.pool and riemann.time log exceptions in correct format
- Fix a race condition (?) in websocket connection close leading to dangling
  connections
- folds/mean: fix divide-by-zero where events are present but all have nil
  metrics
- folds/maximum and folds/minimum return nil when no metrics present
- Internal instrumentation events had ttls 1000x larger than they should have
- Debian package: pidofproc was mis-spelled; broke init scripts on jessie
- streams/sreduce docstring lied about what its example did
- Events from the TCP and UDP servers now receive default timestamps

## Deprecations and API changes
- update-index is now deprecated; indexes are also streams now
- Combine is deprecated in favor of smap
- Incanter is no longer included by default; cuts 14MB off the jar (!)
- coalesce flushes all known events downstream *periodically*, rather than once
  for every incoming event. Users were using coalesce for much broader
  cardinalities than originally intended, which led to performance problems.
  This change introduces additional latency into stream processing (on the
  order of dt seconds), but dramatically improves throughput for broad
  cardinalities. API is backwards-compatible; assumes a default interval of 1
  second.
- Most functions that took kwargs (foo :opt1 "a" :opt2 "b") now take maps for
  consistency and ease of composition. API is backwards compatible, but I will
  deprecate the old style in a few releases.

## New features
- folds/count-unexpired: counts unexpired events
- Improved metrics for async-queue executors
- config/reinject: reinsert events back into the core. Watch out for infinite
  loops!
- streams/batch: emit batches of events every dt seconds or n events, whichever
  comes first. Huge performance boost for integration services which support
  collections of events.
- streams/smapcat: like streams/map, but expects its function to return a
  sequence of events, each of which is sent downstream independently. Inverse
  of coalesce, project, etc.
- The (event) function can be used to create events with default times and
  faster kv lookup. You should probably use (event) instead of creating new
  events as hashmaps.
- Configurable log formats, including json events
- Hipchat integration
- KairosDB integration
- OpenTSDB integration
- Slack integration
- InfluxDB integration

## Improvements
- Indexes can be used directly as streams; no need to wrap them in update-index
- Removed blank lines in logs
- Query compiler caches 128 most recently used queries; 240x speedup in rapid
  repeated queries, massive reduction in GC pressure
- Queries for *exactly* one host and one service have an optimized query path.
- Retuned async-queue defaults for IO pools: more threads by default now
- streams/changed, streams/part-time-fast-interval, streams/sum-over-time,
  streams/mean-over-time, streams/register, streams/append,
  streams/fold-interval, streams/fill-in, streams/fill-in-last,
  streams/interpolate-constant, streams/ddt-events, streams/by-fn, and
  streams/by use atoms, not refs; ~10x speedup
- streams/top can take any comparable objects, not just numbers
- Index throws a more informative error message when events have nil times.
- Logs can be rotated by size
- Console logging can be disabled
- Better docs for async-queue!
- Assorted docstring improvements
- RPM package uses sysconfig
- Websocket server: friendlier logging for errors given non-websocket requests
- streams/forward can take collections of events
- Graphite server: accepts multiple spaces and tabs as separators
- Graphite server: much more robust parsing, error logging
- Librato integration uses persistent connection pool
- Hipchat integration included in config by default
- Hipchat can take collections of events

## Internals
- Additional tests
- time.controlled uses with-redefs
- Tests renamed to modern lein style, e.g. riemann.streams-test
- CMSClassUnloadingEnabled added to aggressive JVM opts
- Fixed a few bugs in the SSE transport tests
- clojure 1.6.0
- algo.generic 0.1.2
- math.numeric-tower 0.0.4
- core.cache 0.6.3
- java.classpath 0.2.2
- aws-java-sdk 1.7.5
- log4j 1.2.17
- aleph 0.3.2
- clj-http 0.9.1
- cheshire 5.3.1
- clj-librato 0.0.4
- slf4j 1.7.7
- capacitor 0.0.2
- riemann-clojure-client 0.2.10

# Version 0.2.4

Minor bugfix release: fixes a few packaging issues and an obnoxious but
nonfatal bug in the websocket server, introduced in 0.2.3.

## Bugfixes

- Packages generate correct md5sums by default
- Tarball package logs to cwd, not /var/log/riemann.log.
- Aleph 0.3.1: fixes a bug which caused websockets to crash on the first conn

## Internals

- Explicit gen-class for riemann.bin; aids in embedding Riemann in Java
- Early work towards testable configs

# Version 0.2.3

Riemann continues to expand in scope and adaptability, especially adding
support for integration with other monitoring systems. Metrics for riemann
internals are now recorded and exposed as events through the standard streams.
Packaging improvements and some minor performance/bugfixes round out the mix!

## New features

- Comprehensive instrumentation for UDP, TCP, and Websocket servers, plus core;
  latencies, queue depths, and throughput
- Plugin system for loading external namespaces from the classpath.
- Standard deviation fold
- HipChat client
- Logstash client
- Support for passive Nagios checks
- HTTP server-sent-event transport

## Improvements

- Librato adapter can take a sequence of events, e.g. from rollups. Much more
  efficient
- Improved error handling for Websockets HTTP server for 404 paths
- Custom attributes included in emails and SNS notifications
- config/include can now recursively load all files if given a directory
- Ratios are now formatted as doubles in email

## Bugfixes

- Debian package now correctly respects `$EXTRA_CLASSPATH`
- No longer attempts to register SIGHUP handler on windows
- Index expiry no longer breaks on events with nil ttls
- Riemann.pool no longer sets :block-start true always
- Various docstring fixups
- Tarball now uses /usr/bin/env bash, not /bin/bash
- Fix an issue with start-stop-daemon on older Debian versions
- Load defaults from /etc/default on Debian

## Internals

- Additional type hints; improves performance, especially for Graphite.
- mean-over-time is now deprecated
- Tests for graphite server
- Expanded test suite
- clojure 1.5.0 -> 1.5.1
- clj-librato 0.0.3
- riemann-clojure-client 0.2.9
- postal 1.11.1
- clj-time 0.6.0
- clj-http 0.7.7
- cheshire 5.2.0
- clj-wallhack 1.0.1
- clojure-complete 0.2.3
- netty 3.8.0-Final
- slf4j-log4j12 1.7.5
- slingshot 0.10.3
- clj-campfire 2.2.0
- less-awful-ssl 0.1.1
- tools.nrepl 0.2.3
- tools.logging 0.2.6
- org.clojure/java.classpath 0.2.1
- aleph 0.3.0
- incanter-core 1.5.4
- math.numeric-tower 0.0.2
- algo.generic 0.1.1

# Version 0.2.2

## New features

- HTTP server: PUT /events can take a series of JSON-encoded events
- Graphite server: can also speak Carbon over UDP
- riemann.sns: hooks in to Amazon's Simple Notification Service
- riemann.campfire: sends events to Campfire
- streams/apdex: the fraction of requests which are acceptable
- streams/clock-skew: detects clock skew between hosts
- streams/scale: multiplies metrics by a fixed factor
- streams/stable: passes on events with the same value for at least dt seconds
- Internal instrumentation: measures throughput and latency of streams
- Limited TLS support for tcp-server

## Improvements

- Graphite pool: improved logging and saner concurrency defaults
- streams/periodically-until-expired (and dependent streams) now correctly
  handle their own expiry
- More type hints (reduces several performance bottlenecks in the index,
  pubsub, and graphite)!
- PID log message logs just the PID, not pid@host
- bin/riemann: easier to add custom jars and JVM options via /etc/defaults/

## Bugfixes

- streams/where: tagged-any and tagged-all work correctly
- streams/where\*: evaluates child streams only once
- streams/throttle: throttles expired events correctly
- count-string-bytes: fixed an error with multibyte chars
- Debian package: saner java deps, md5s for snapshot versions
- Reaper: reloads correctly when you change :keep-keys

## Internals

- Removed need for AOT compilation
- No more gen-class for riemann.config
- Removed dependency on incanter-charts
- Services can declare that they conflict with other services.
- streams/part-time-simple: like part-time-fast, but with a saner API
- index/lookup: looks up an event by host and service
- riemann.email: Cleaned up deprecated formatting functions


# Version 0.2.1

This is a small maintenance release to address a few issues with 0.2.0: most
notably, an accidental transitive dependency on a snapshot release of Yammer
Metrics which is no longer available.

## New features

- streams/runs: detects runs of successive events.

## Improvements

- Various documentation fixes.
- Riemann.pool uses a LinkedBlockingQueue--reports improved performance.

## Bugfixes

- Fix dependencies on Aleph and Lamina which relied on a snapshot variant of
  Yammer Metrics.
- Debian package init script reports "already running" when appropriate.
- Debian package depends on Java.
- (streams core) is no longer lazy (fixes a possible threading issue).

## Internals

- Some utility functions for emails got moved to riemann.common, to facilitate
  their re-use.

# Version 0.2.0

Guess it's time we started a formal changelog. Version 0.2.0 is a fairly major
improvement in Riemann's performance and capabilities. Many things have been
solidified, expanded, or tuned, and there are a few completely new ideas as
well.

There are a few minor API changes, mostly to internal structure--but a few
streams are involved as well. Most functions will continue to work normally,
but log a deprecation notice when used.

## New features

- Arbitrary key-value (string) pairs on events
- Hot config reloading
- Integrated nrepl server
- streams/sdo: bind together multiple streams as one
- streams/split: like (cond), dispatch an event to the first matching stream
- streams/splitp: like split, but on the basis of a specific predicate
- config/delete-from-index: explicitly remove (similar) events from the index
- streams/top: streaming top-k
- streams/tag: add tags to events
- RPM packaging
- Init scripts, proper log dirs, and users for debian and RPM packages. Yeah,
  this means you can /etc/init.d/riemann reload, and Stuff Just Works (TM).
- folds/difference, product, and quotient.
- Folds come in sloppy and strict variants which should "Do What I Mean" in
  most contexts.
- Executor Services for asynchronous queued processing of events.
- streams/exception-stream: captures exceptions and converts them to events.

## Improvements

- http://riemann.io site
- Lots more documentation and examples
- Config file syntax errors are detected early
- Cleaned up server logging
- Helpful messages (line numbers! filenames!) for configuration errors
- Silence closed channel exceptions
- Cores can preserve services like pubsub, the index, etc through reloads
- Massive speedups in TCP and UDP server throughput
- streams/rate works in real-time: no need for fill-in any more
- Graphite client is faster, more complete
- Config files can include other files by relative path
- streams/coalesce passes on expired events
- riemann.email/mailer can take custom :subject and :body functions
- riemann.config includes some common time/scheduling functions
- streams/where returns whether it matched an event, which means (where) is
  now re-usable as a predicate in lots of different contexts.
- streams/tagged-any and tagged-all return whether they matched
- streams/counter is resettable to a particular metric, and supports expiry
- Bring back "hyperspace core online"
- Update to netty 3.6.1
- Reduced the number of threadpools used by the servers
- Massive speedup in Netty performance by re-organizing execution handlers
- core/reaper takes a :keep-keys option to specify which fields on an event
  are preserved
- streams/smap ignores nil values for better use with folds
- Update to aleph 0.3.0-beta15
- Config files ship with emacs modelines, too

## Bugfixes

- Fixed a bug in part-time-fast causing undercounting under high contention
- Catch exceptions while processing expired events
- Fix a bug escaping metric names for librato
- riemann.email/mailer can talk to SMTP relays again
- graphite-path-percentiles will convert decimals of three or more places to
  percentile strings
- streams/rollup is much more efficient; doesn't leak tasks
- streams/rollup aggregates and forwards expired events instead of stopping
- Fixed a threadpool leak from Netty
- streams/coalesce: fixed a bug involving lazy persistence of transients
- streams/ddt: fixed a few edge cases

## Internals

- Cleaned up the test suite's logging
- Pluggable transports for netty servers
- Cores are immutable
- Service protocol: provides lifecycle management for internal components
- Tests for riemann.config
- riemann.periodic is gone; replaced by riemann.time
- Tried to clean up some duplicated functions between core, config, and streams
- riemann.common/deprecated
- Cleaned up riemann.streams, removing unused commented-out code
- Lots of anonymous functions have names now, to help with profiling
- Composing netty pipeline factories is much simpler
- Clojure 1.5

## Known bugs

- Passing :host to websocket-server does nothing: it binds to * regardless.
- Folds/mean throws when it receives empty lists
- graphite-server has no tests
- Riemann will happily overload browsers via websockets
- streams/rate doesn't stop its internal poller correctly when self-expiring
- When Netty runs out of filehandles, it'll hang new connections
