# Version 0.2.14

Contains mostly new features and enhancements. Mathieu Corbin replaced `refs` in both `fixed-time-window` and `moving-time-window` with atoms, greatly improving their performance. They also added an `sflatten` stream and refactored the Elasticsearch output. boernd extended the capabilities of the Pushover plugin and added an MS Teams plugin. Brian Conn enhanced and reduced the payload of the Netuitive plugin.

There were also some fixes to documentation, the website and bumps of various project dependencies including `clj-http` to 3.5.0.

## Features and enhancements

- Extend pushover functionality [\#808](https://github.com/riemann/riemann/pull/808) ([boernd](https://github.com/boernd))
- Refactoring Elasticsearch output [\#804](https://github.com/riemann/riemann/pull/804) ([mcorbin](https://github.com/mcorbin))
- replace refs in fixed-time-window-fn by an atom [\#797](https://github.com/riemann/riemann/pull/797) ([mcorbin](https://github.com/mcorbin))
- Replace refs in moving-time-window by an atom [\#811](https://github.com/riemann/riemann/pull/811) ([mcorbin](https://github.com/mcorbin))
- Updated a number of project dependencies [\#800](https://github.com/riemann/riemann/pull/800) ([jamtur01](https://github.com/jamtur01))
- bump clj-http to 3.5.0 - http-integrations over a sniproxy \o/ [\#814](https://github.com/riemann/riemann/pull/814) ([andrerocker](https://github.com/andrerocker))
- Netuitive Payload Size Reduction [\#831](https://github.com/riemann/riemann/pull/831) ([TheConnMan](https://github.com/TheConnMan))
- Add ms teams output [\#830](https://github.com/riemann/riemann/pull/830) ([boernd](https://github.com/boernd))
- Add sflatten stream [\#825](https://github.com/riemann/riemann/pull/825) ([mcorbin](https://github.com/mcorbin))

## Bug fixes

- Remove all \*warn-on-reflection\* warning in the influxdb stream [\#829](https://github.com/riemann/riemann/pull/829) ([mcorbin](https://github.com/mcorbin))

# Version 0.2.13

This release contains new Kafka input and output plugins. An Netuitive
plugin and a new output plugin for Telgraph notifications. The InfluxDB
plugin has been refactored, basic auth support added to the
Elasticsearch plugin and a variety of other enhancements and fixes.

## Features and enhancements

- Added Kafka [input](https://github.com/riemann/riemann/pull/781) and [output](https://github.com/riemann/riemann/pull/760) plugins.
- Added Netuitive plugin
  [\#753](https://github.com/riemann/riemann/pull/753)
- Telegram notification support.
  [\#714](https://github.com/riemann/riemann/pull/714)
- Support for Basic Auth credentials for Elasticsearch.
  [\#754](https://github.com/riemann/riemann/pull/754)
- Added support of time in microsecond resolution in the Riemann
  protocol (See `time_micros` in the [Riemann client](https://github.com/riemann/riemann-java-client/blob/master/riemann-java-client/src/main/proto/riemann/proto.proto). If you maintain a Riemann client should update them to support microseconds.
- Added an `:options` parameter to the Pagerduty plugin.
  [\#773](https://github.com/riemann/riemann/pull/773)
- Added Riemann tag to instrumented transports and services.
  [\#756](https://github.com/riemann/riemann/pull/756)
- Updated to latest codox version.
- Pretty'ed test output.
  [\#790](https://github.com/riemann/riemann/pull/790)
- Removed capacitor dependency
  [\#774](https://github.com/riemann/riemann/pull/774)
- Bumped nrepl to 0.2.12
  [\#769](https://github.com/riemann/riemann/pull/769)

## Bug Fixes

- Refactor of the InfluxDB plugin
  [\#741](https://github.com/riemann/riemann/pull/741)
- Prometheus label / body only support some characters.
  [\#747](https://github.com/riemann/riemann/pull/747)
- Cast Slack event tags into a vector
  [\#749](https://github.com/riemann/riemann/pull/749)
- Restore Netty queue size metric
  [\#757](https://github.com/riemann/riemann/pull/757)

# Version 0.2.12

This version includes Prometheus, Druid and Elasticsearch plugins. It
adds HTML body support for Mailgun, KairosDB HTTP integration,

This version also contains a number of bug fixes and deprecates the
`within`, `without`, and `combine` streams.

We've also renamed com.aphyr to io.riemann.

## Features and enhancements

- Added Prometheus Plugin
  [\#692](https://github.com/riemann/riemann/pull/692)
- Added Druid plugin
  [\#691](https://github.com/riemann/riemann/pull/691)
- Add support for KairosDB HTTP integration and metric TTLs
  [\#627](https://github.com/riemann/riemann/pull/627)
- Add riemann.elasticsearch
  [\#722](https://github.com/riemann/riemann/pull/722)
- Add HTML body support for mailgun
  [\#719](https://github.com/riemann/riemann/pull/719)
- Add the ability to read SNS credentials from the default credential
  chain [\#701](https://github.com/riemann/riemann/pull/701)
- Updating netty to 4.1.0
  [\#694](https://github.com/riemann/riemann/pull/694)
- Allow explicit config of Slack HTTP connection params
  [\#681](https://github.com/riemann/riemann/pull/681)
- Add batch forwarding for datadog
  [\#679](https://github.com/riemann/riemann/pull/679)

## Bug fixes

- Suppress exception logging if the exception is handled by `exception-stream` [\#726](https://github.com/riemann/riemann/issues/726)
- Improve error messages for librato with missing metrics [\#374](https://github.com/riemann/riemann/issues/374)
- Fix sse listening address [\#737](https://github.com/riemann/riemann/pull/737)
- Fix RuntimeException in udp graphite-server [\#736](https://github.com/riemann/riemann/pull/736) 
- Fix websocket listening address [\#735](https://github.com/riemann/riemann/pull/735) 
- Remove tags and fields if value is nil or empty [\#734](https://github.com/riemann/riemann/pull/734)
- Don't log exceptions if in exception-stream [\#729](https://github.com/riemann/riemann/pull/729) 
- Revert previous 'fix' closing unwritable channels [\#724](https://github.com/riemann/riemann/pull/724)
- Fix nested escaping of strings [\#717](https://github.com/riemann/riemann/pull/717) 
- Link to '/' rather than index.html [\#711](https://github.com/riemann/riemann/pull/711) 
- Fix fraction divisor in generating events example [\#708](https://github.com/riemann/riemann/pull/708)
- Add ChannelOption/SO\_BACKLOG to TCP server [\#706](https://github.com/riemann/riemann/pull/706) 
- Clarify GC behavior of \(by\) streams [\#704](https://github.com/riemann/riemann/pull/704) 
- Correction in maintenance-mode function [\#702](https://github.com/riemann/riemann/pull/702) 
- Add the ability to read SNS credentials from the default credential chain [\#701](https://github.com/riemann/riemann/pull/701)
- Fixes \#374 - Librato error without metric [\#695](https://github.com/riemann/riemann/pull/695) 
- Updating netty to 4.1.0 [\#694](https://github.com/riemann/riemann/pull/694) 

## Deprecations and API changes

- Removed deprecated functions: `within`, `without` and `combine`. These
  were deprecated in 2014.
- Renamed com.aphyr to io.riemann in Riemann core
  [\#685](https://github.com/riemann/riemann/pull/685)

# Version 0.2.11

This update includes a variety of bug fixes and improvements. Also
included is a VictorOps integration, improvements to the Graphite, Xymon,
InfluxDB, Hipchat and Nagios integrations.

Internally the project has been updated for Clojure 1.8.

## Bugfixes

- time: prevent negative delays in every!. fixes #368
- Coerce graphite metric to double, when not an int
- Fix InfluxDB 0.9 tags

## Deprecations and API changes

- `update` is now a reserved keyword in Clojure. Please use `insert`
  instead. If you have a configuration which uses `update` then Riemann
  will generate a deprecation warning and automatically use `insert`.
- riemann.config: use :refer instead of def for logstash & graphite
- Fix logging, use logback instead log4j, (import log levels from
  `ch.qos.logback.classic` instead of `org.apache.log4j`)
- `by-fn` now expects the new-fork argument to be a 1-arity function
- pagerduty requires the service-key to be passed as a key named `service-key`
  rather than directly as a string.
  eg: `(let [pd (pagerduty :service-key "my-service-key")]`

## Improvement

- Added tags to the OpsGenie integration.
- Xymon: fixes, scalability, multiple xymon host, error handling
- hipchat: provide a default from field and do not leak server params
- nagios: provide a default state for events
- Added support for a PagerDuty formatter for events
- Allow overriding graphite metric conversion method
- Allow graphite to take a function as host name
- Xymon: ability to support more message types. Enable/Disable messages
  implementation
- Add insecure flag for influxdb in case cert is self-signed for https
- Add -v and version command to display Lein or POM version
- logging: improve console logging
- folds: add modes and mode
- Cloudwatch can now use instance profiles for authentication
- Slack can now output simple status lines for text clients
- The Twilio client was adapted to updated API behavior
- Docstring typo fixes

## New features

- VictorOps integration
- Add config directory to classpath; we won't need to use `include` any more
- fill-in-last*: apply arbitrary function to last event

## Internals

- Move to Clojure 1.8
- riemann-clojure-client "0.4.2"
- nREPL dep to 0.2.11
- netty to 4.0.36.Final

# Version 0.2.10

0.2.10 brings long-awaited fixes to the Influx integration, support for sending
events to Pushover, and improvements to slack and hipchat formatting. There are
also a few minor usability improvements, and assorted library updates.

## Bugfixes

- RPM package correctly requires JDK 1.7+
- \*config-file\* is correctly bound when including directories

## Deprecations and API changes

- Hipchat plugin now requires a v2 auth token

## New features

- New metric for index size
- Pushover integration
- riemann.test/lookup: For folks who just want the most recent event for a host
  & service

## Improvements

- Tunable UDP server so-rcvbuf
- Mailer gives more helpful feedback when you provide non-string addresses
- InfluxDB 0.9 support
- Hipchat supports private servers and uses the v2 API
- More detailed Slack messages
- Slack custom formatters can emit markup
- Email supports both varargs and sequential address lists
- Better docstrings for throttle
- TSDB tags are converted to custom fields
- epoll server can now be disabled with -Dnetty.epoll.enabled=false

## Internals

- Removed old query parser altogether
- riemann-clojure-client 0.4.1
- tools.nrepl 0.2.7 -> 0.2.10
- cheshire 5.4.0 -> 5.5.0
- capacitor 0.4.2 -> 0.4.3
- amazonica 0.3.13 -> 0.3.28
- slingshot 0.12.1 -> 0.12.2
- clj-http 1.0.1 -> 1.1.2
- aws-java-sdk 1.9.13 -> 1.10.5.1
- clj-time 0.9.0 -> 0.10.0
- slf4j-log4j12 1.7.10 -> 1.7.12


# Version 0.2.9

0.2.9 brings a new query engine, packaging improvements, and assorted bugfixes.
We have two new services we can talk to: Boundary, and Keen IO. The InfluxDB
adapter is now dramatically faster, and we have better test coverage for some
integration clients. There's also a host of library updates, which enables new
features and better library interop for advanced users.

## Bugfixes

- RPM init scripts return proper errors when startup fails
- streams/where now only evaluates its predicate expression once
- Fix debian and RPM package file ownership; should fix the default logging
  errors
- Only enable epoll on linux/amd64 (fixes i386 and ARM crashes)

## Deprecations and API changes

- bin scripts now place EXTRA_CLASSPATH last, not first, to ensure its classes
  take precedence.

## New features

- streams/fixed-offset-time-window
- Keen IO integration
- Boundary integration
- Queries support custom fields

## Improvements

- Exception events now carry the original exception in the :exception field.
- Bring back tcp/udp server "threads active" metrics
- Codox links to Github source
- Deprecation warnings are only emitted once
- InfluxDB now accepts sequences of events, so it works with batch, rollup, etc
- InfluxDB passes event times on to Influx
- Various xymon improvements
- Tarball now supports EXTRA_CLASSPATH and EXTRA_JAVA_OPTS
- Query parser now offers better feedback on syntax errors
- Reduced log spew from misbehaving graphite clients

## Internals

- Removed need for Boundary maven repo
- Maven repo cached between builds (improves testing speed in CI)
- clj-time 0.6.0 -> 0.9.0
- high-scale-lib 1.0.4 -> 1.0.6
- clj-http 0.9.1 -> 1.0.0
- capacitor 0.2.2 -> 0.4.2
- cheshire 5.3.1 -> 5.4.0
- aws-java-sdk 1.7.5 -> 1.9.16
- riemann-clojure-client 0.3.0 -> 0.3.1
- tools.logging 0.2.6 -> 0.3.1
- apache-log4j-extras 1.0 -> 1.2.17
- postal 1.11.1 -> 1.11.3
- jsonevent-layout 1.5 -> 1.7
- slingshot 0.10.3 -> 0.12.1
- slf4j-log4j12 1.7.7 -> 1.7.10
- core.cache 0.6.3 -> 0.6.4
- amazonica 0.2.26->0.3.13
- tools.nrepl 0.2.3 -> 0.2.7
- aws-java-sdk 1.7.5 -> 1.9.13
- less-awful-ssl 0.1.1 -> 1.0.0


# Version 0.2.8

Minor followup release: fixes a bug in 0.2.7 which broke TCP servers on
non-linux platforms.

## Bugfixes

- TCP transport now uses epoll only on Linux platforms, Java NIO otherwise.


# Version 0.2.7

Performance improvements and important bugfixes: 0.2.7 is long overdue. New
integrations with Blueflood, Logentries, Opsgenie, Cloudwatch, Mailgun, Xymon,
Datadog, and Twilio.

## Bugfixes

- Stackdriver: fix a shadowing warning
- Debian package now recommends Java
- Debian package launches Riemann on boot by default
- riemann test command now actually exists, works from startup scripts
- Indexes no longer disappear on config reload

## Deprecations and API changes

- Riemann-clojure-client and riemann-java-client 0.3.x, included in riemann
  0.2.7, return asynchronous results by default. `streams/forward` is still
  synchronous, but if you're invoking clients manually, make sure to `deref`
  results from `send-event` etc.

## New features

- Dynamic loading of dependencies via the new plugin system
- Logentries integration
- Blueflood integration
- Xymon integration
- Mailgun integration
- Opsgenie integration
- Cloudwatch integration
- Datadog integration
- Twilio integration
- Slack adapter allows incoming webhooks
- OpenTSDB server

## Improvements

- Logstash now sends events without metrics
- You can set log4j options via a properties file
- Aggressive JVM opts now includes -server
- Various docstring improvements
- Optimizations to zero- and single-argument forms of `sdo`
- Faster startup and shutdown for TCP/UDP servers
- `streams/with` can work with vectors of events, perf improvements
- More type hints in performance-critical paths

## Internals

- clj-librato 0.0.5
- Aleph and Lamina are now completely removed. Improves startup times and jar
  sizes.
- Websocket and SSE transports now based on httpkit. No instrumentation for
  httpkit latencies sadly.
- Upgrade from Netty 3 to Netty 4.0.21. Should see reduced CPU, slightly higher
  throughput. Uses the epoll transport.
- New mock macro in riemann.test-utils for testing integration streams.
- riemann-clojure-client 0.3.1

# Version 0.2.6

Improvements to ease of use, expanded integration with other monitoring tools,
and important bugfixes. Most importantly, we've added two new features: the
`pipe` stream, which makes it easy to split and recombine events through a
cascading series of streams, and riemann.test infrastructure for writing
repeatable tests for your config's streams. There's also new support for two
new services: stackdriver and Shinken, and a change to the officially supported
JDK versions.

## Bugfixes

- Fixed a bug introduced in 0.2.5 (due to a change in Clojure's destructuring
  bind defaults for maps) which caused exceptions when expiring events without
  a TTL.
- JSON-decoded events from the HTTP PUT endpoint now have correct timestamps;
  they were 1000x too large.
- Package md5sums now have two spaces.
- Equivalent indexes are no longer wiped between reloads.
- `(where (tagged "foo"))` now fully qualifies its expanded form; works when
  `tagged-any` or `tagged-all` isn't in the invoking namespace.

## Deprecations and API changes

- `streams/within` and `streams/without` are deprecated; bounds logic was
  ambiguous. Use `(where (< 1 metric 2))` etc.
- config/include now only loads .clj and .config files when given a directory.
  Now you can mix resources and other files into those directories.
- JDK6 is no longer supported, though it'll probably keep working for a while.
- JDK8 is now supported.

## New features

- streams/pipe: Easily create n->m->l-wide manifolds of streams.
- Testing configs! See the howto or 6ea82e07 for details.
- Stackdriver integration.
- Shinken integration.
- streams/changed now takes a `:pairs?` option, which emits `[old-event
  new-event]` pairs when the predicate value changes.

## Improvements

- You can now match `nil` in `where`, `split`, and other Match expressions.
- You can now match maps in `where`, `split`, etc: given a map of keys to Match
  predicates, asserts that for all keys in the predicate map, the corresponding
  value in the target map matches the predicate's value.
- Slack integration can take a custom formatter function.
- Influxdb configurable service names.
- Docstring improvements for `streams/ewma`, `rate`, `with`, and `default`.
- Websocket conn logs are now `debug`, not `info`.

## Internals

- Clojure.complete is no longer required.
- Interval-metrics 1.0.0
- streams-test/run-stream is now a part of riemann.test, and advances time.

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
