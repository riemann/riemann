Update
======

For performance reasons, and for fun, I've started work on a drop-in
replacement for ustate: reimann.

Reimann uses the same wire protocol and queries, so you can forward from ustate
to reimann and vice-versa. The ruby ustate client in master already supports
reimann. Simple benchmarks suggest 3,000 synchronous events/sec on a single
thread; most of the time spent in network code. There is low-hanging
optimization fruit here--I'm just trying to get it feature-complete so I can
test on real datasets. Underlying stream primitives like rates already handle
hundreds of thousands of events/sec on a Macbook Pro.

Reimann provides a more expressive language for filtering, changing, splitting,
and combining events across time and state space. Instead of *states*, you
submit *events* which flow through various pipes. You no longer have to
aggregate states over time on the client; it's possible to submit *every* event
directly to reimann and it will compute rates, percentiles, etc.

Ready:

- TCP server
- TCP forwarding
- Configuration file
- Stream primitives
  - Rates
  - Percentiles
  - Split by field
  - Where filter
  - Combine streams (with let bindings; macro pending)
  - Change values
  - Forward to another ustate/reimann
- State index
- Send emails
- Query parser
- Querying states
- Basic expiry

TODO before release:

- Forward to graphite
- Docs, demo, slides for talk

After that:
- HSQL index
- UDP server
- UDP client
- Ruby UDP client
- Drop aleph, replace with raw netty?
- Subscribe to streams?

The Sinatra dashboard will still be maintained, as will the ruby clients.

Overview
======

UState ("United States", "microstate", etc.) is a state aggregation daemon. It
accepts a stream of state transitions and maintains an index of service states,
which can be queried or forwarded to various handlers. A state is simply:

    state {
      host: A hostname, e.g. "api1", "foo.com",
      service: e.g. "API port 8000 reqs/sec",
      state: Any string less than 255 bytes, e.g. "ok", "warning", "critical",
      time: The time that the service entered this state, in unix time,
      description: Freeform text,
      metric_f: A floating-point number associated with this state, e.g. the number of reqs/sec,
      once: A boolean, described below.
    }

Normally, every state received by the server fires Index#on_state. When
state.state changes, Index#on_state_change is called. You can, for example,
register to send a single email whenever a state changes to :warning.

:once states are transient. They fire Index#on_state and #on_state_once, but do
*not* update the index. They can be used for events which are instantaneous;
instead of sending {state: error} and {state: ok}, send {state: error,
once:true}. 

For example, recoverable errors may not hang your application, but
should be processed by the email notifier. Sending a :once state with
the error description means you can receive an email for each error,
instead of two for entering and exiting the error state.

At http://showyou.com, we use UState to monitor the health and performance of
hundreds of services across our infrastructure, including CPU, queries/second,
latency bounds, disk usage, queues, and others.

UState also includes a simple dashboard Sinatra app.

Installing
==========

    git clone git://github.com/aphyr/ustate.git

or

    gem install ustate-client

For the client:

    gem install beefcake trollop

For the server:

    gem install treetop eventmachine sequel sqlite3 trollop beefcake

For the dashboard:

     gem install sinatra thin erubis sass

We run UState from Upstart. An example job is in docs/upstart.conf.

Demo
====

To try it out, install all the gems above, and clone the repository. Start the server with

    bin/server

UState listens on TCP socket host:port, and accepts connections from clients. Start a basic testing client with

    bin/test

The tester spews randomly generated statistics at a server on the default local host and port. To see it in action, run the dashboard:

    cd lib/ustate/dash
    ../../../bin/dash

Server
======

The server loads a file in the working directory named config.rb. Override with
--config-file.  Its contents are instance-evaled in the context of the current
server. You can use this to extend ustate with additional behavior.

Expiring States
---------------

The reaper periodically kills states matching queries which are too old. It
will ensure that any state matching a query will be present for *at least* that
many seconds. For instance:

    # States expire after 10 seconds
    reaper.default = 10

    # Except for daily stats, which last 2 days
    reaper.reap 'service =~ "%daily%"', 2 * 24 * 3600

    # We need to know RIGHT AWAY if the fridge fails to check in.
    reaper.reap 'host = "fridge"', 1

In this configuration, daily updates from host fridge will stay around for 2
days, everything not on the fridge expires after 10 seconds, and other fridge
updates are kept for only one second.

Note that the reaper does some query recomposition which can lead to
inefficient patterns. Writing an optimizer is on my list.

Email
-----

config.rb:

    # Email comes from this address (required):
    emailer.from = 'ustate@your.net'

    # Use this SMTP relay (default 127.0.0.1)
    emailer.host = '123.4.56.7'
    
    # Receive mail when a state transition matches any of ...
    emailer.tell 'you@gmail.com', 'state = "error" or state = "critical"'
    emailer.tell 'you@gmail.com', 'service =~ "mysql%"'

Aggregating states
---

UState can fold together states matching some criteria to provide a more
general overview of a complex system. Folds are executed in a separate thread
and polled from the index every aggregator.interval seconds.

config.rb:

    # Add together the metrics for all feed mergers on any host.
    # The resulting state has service name 'feed merger', but no host.
    aggregator.sum 'service = "feed merger" and host != null', host: nil
 
    # Average latencies
    aggregator.average 'service = "api latency"'

    # You can also pass any block to aggregator.fold. The block will be called
    # with an array of states matching your query.
    aggregator.fold 'service = "custom"' do |states|
      UState::State.new(
        service: 'some crazy result',
        metric_f: states.map(&:metric_f).max
      )
    end

Graphite
---

UState can forward metrics to Graphite. Just specify a query matching states
you'd like to forward. Forwarding is performed in a separate thread, and polled
from the index every graphite.interval seconds.

config.rb:

    graphite.host = 'foo'
    graphite.port = 12345

    # Submit states every 5 seconds
    graphite.interval = 5
    
    # Send everything without a host
    graphite.graph 'host = null'

    # And also include the disk use on all nodes
    graphite.graph 'service = "disk"'
 
Custom hooks
------------

config.rb:

    # Log all states received to console.
    index.on_state do |s|
      p s
    end
    
    # Forward state transitions to another server.
    require 'ustate/client'
    client = UState::Client.new :host => '123.45.67.8'
    index.on_state_change do |old, new|
      client << new
    end
    index.on_state_once do |state|
      client << state
    end

Client
======

You can use the git repo, or the gem.

    gem install ustate-client

Then:

    require 'ustate'
    require 'ustate/client'

    # Create a client
    c = UState::Client.new(
      host: "my.host",    # Default localhost
      port: 1234          # Default 55956
    )
    
    # Insert a state
    c << {
      state: "ok",
      service: "My service"
    }

    # Query for states
    c.query.states # => [UState::State(state: 'ok', service: 'My service')]
    c.query('state != "ok"').states # => []

Client state management
-----------------------

UState provides some classes to make managing state updates easier.

UState::MetricThread starts a thread to poll a metric periodically, which can
be used to flush an accumulated value to ustate at regular intervals.

UState::AutoState bundles a state and a client together. Any changes to the
AutoState automatically send the new state to the client.

The Dashboard
=============

The dashboard runs a file in the local directory: config.rb. That file can
override any configuration options on the Dash class (hence all Sinatra
configuration) as well as the Ustate client, etc.

    set :port, 6000 # HTTP server on port 6000
    config[:client][:host] = 'my.ustate.server'

It also loads views from the local directory. Sinatra makes it awkward to
compose multiple view directories, so you'll probably want to create your own
view/ and config.rb. I've provided an example stylesheet, layout, and dashboard
in lib/ustate/dash/views--as well as an extensive set of functions for laying
out states corresponding to any query: see lib/ustate/dash/helper/renderer.rb.
The way I figure, you're almost certainly going to want to write your own, so
I'm going to give you the tools you need, and get out of your way.

An example config.rb, additional controllers, views, and public directory are
all in doc/dash. Should give you ideas for extending the dashboard for your own needs.

Protocol
========

A connection to UState is a stream of messages. Each message is a 4 byte
network-endian integer *length*, followed by a Procol Buffer Message of
*length* bytes. See proto/message.proto for the protobuf particulars.

The server will accept a repeated list of States, and respond with a
confirmation message with either an acknowledgement or an error. Check the `ok`
boolean in the message; if false, message.error will be a descriptive string.

States are uniquely identified by host and service. Both allow null. State.time
is the time in unix epoch seconds and is required. 

You can also query states using a very basic expression language. The grammar
is specified as a Parsable Expression Grammar in query_string.treetop. Examples
include:

    state = "ok"
    
    (service =~ "disk%") or (state == "critical" and host =~ "%.trioptimum.com")

    metric_f > 2.0 and not host = "tau ceti 5"
    
    # All states
    true

    # No states
    false

Just submit a Message with your query in Message.query.string. Search queries
will return a message with repeated States matching that expression. An null
expression will return no states.

Performance
===========

On a macbook pro 8,3, I see >1300 queries/sec or >1200 inserts/sec. The client
is fully threadsafe, and performs well concurrently. I will continue to tune
UState for latency and throughput, and welcome patches.

For large installations, I plan to implement a selective forwarder. Local
ustate servers can accept high volumes of states from a small set of nodes, and
forward updates at a larger granularity to supervisors, and so forth, in a
tree. The query language should be able to support proxying requests to the
most recent source of a state, so very large sets of services can be maintained
at high granularity.

Future directions
=====

Several people have mentioned wanting to query historical states; to replay the
events in ustate over time. There are some difficulties here; notably that
compressing hundreds of millions of states can make it a little tricky to query
states over the entire dataset. If we restrict ourselves to specific time
ranges, storing sequential states as protocol buffers compressed with snappy
could work, especially if only *state* changes are written. Storing only state
deltas might work as well.

UState currently offers only one-second time resolution. Sub-second times will
be provided by a second field (e.g. State.nanoseconds). I haven't decided on
the granularity yet.

It'd be interesting to subscribe to states matching a query and receive states
pushed to you as soon as they change.

Should be easy to add a UDP acceptor for states as well. Have to figure out
eventmachine with multiple backends.

When the protocol and architecture are finalized, I plan to reimplement the
server in a faster language.
