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
