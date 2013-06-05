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
