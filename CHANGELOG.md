# Changelog

## [0.3.11](https://github.com/riemann/riemann/tree/0.3.11) (2023-12-27)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.10...0.3.11)

**Merged pull requests:**

- Also run the test suite on PR [\#1040](https://github.com/riemann/riemann/pull/1040) ([smortex](https://github.com/smortex))
- Update to Clojure 1.11.1 [\#1039](https://github.com/riemann/riemann/pull/1039) ([brandonvin](https://github.com/brandonvin))

## [0.3.10](https://github.com/riemann/riemann/tree/0.3.10) (2023-10-30)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.9...0.3.10)

**Merged pull requests:**

- Forced long to Thread/sleep duration to avoid reflection error [\#1036](https://github.com/riemann/riemann/pull/1036) ([jamtur01](https://github.com/jamtur01))

## [0.3.9](https://github.com/riemann/riemann/tree/0.3.9) (2023-10-29)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.8...0.3.9)

**Closed issues:**

- `influxdb2` keeps accumulating background threads [\#1027](https://github.com/riemann/riemann/issues/1027)
- amazonica throws RuntimeException on jdk 17/18 [\#1021](https://github.com/riemann/riemann/issues/1021)
- "Interrupted consumption" from riemann.kafka and it never recovers [\#1016](https://github.com/riemann/riemann/issues/1016)
- Has 0.3.8 been retagged? [\#1013](https://github.com/riemann/riemann/issues/1013)
- \[influxdb.clj\] SSLSocketfactory not supported on JDK 9+ [\#1007](https://github.com/riemann/riemann/issues/1007)
- Logback needs update to 1.2.10 [\#1006](https://github.com/riemann/riemann/issues/1006)
- Docker image builds switched architecture [\#1002](https://github.com/riemann/riemann/issues/1002)

**Merged pull requests:**

- Update Docker base image to Temurin 21 JRE [\#1035](https://github.com/riemann/riemann/pull/1035) ([pgilad](https://github.com/pgilad))
- Increase performance of Elasticsearch bulk forwarder. [\#1033](https://github.com/riemann/riemann/pull/1033) ([cresh](https://github.com/cresh))
- Improvements in clickhouse plugin [\#1031](https://github.com/riemann/riemann/pull/1031) ([chhetripradeep](https://github.com/chhetripradeep))
- avoid using nil as a function [\#1030](https://github.com/riemann/riemann/pull/1030) ([pyr](https://github.com/pyr))
- Use getWriteApiBlocking to post data to InfluxDB [\#1028](https://github.com/riemann/riemann/pull/1028) ([node13h](https://github.com/node13h))
- Added gh actions testing workflow [\#1024](https://github.com/riemann/riemann/pull/1024) ([jamtur01](https://github.com/jamtur01))
- Filter excluded fields in prometheus with batch processing. [\#1023](https://github.com/riemann/riemann/pull/1023) ([cresh](https://github.com/cresh))
- Upgrade amazonica to play nice with JDK17 [\#1022](https://github.com/riemann/riemann/pull/1022) ([nukemberg](https://github.com/nukemberg))
- Updated to non-deprecared CircleCI images [\#1019](https://github.com/riemann/riemann/pull/1019) ([jamtur01](https://github.com/jamtur01))
- Upgraded pom to 1.2.1 and kinsky to 0.1.26 [\#1018](https://github.com/riemann/riemann/pull/1018) ([jamtur01](https://github.com/jamtur01))
- Improve riemann.kafka resilience to empty messages \(\#1016\) [\#1017](https://github.com/riemann/riemann/pull/1017) ([szrumi](https://github.com/szrumi))
- Fix cut-n-paste error in tagged-any docstring [\#1015](https://github.com/riemann/riemann/pull/1015) ([jarpy](https://github.com/jarpy))
- Improve performance of tagged streams [\#1011](https://github.com/riemann/riemann/pull/1011) ([nukemberg](https://github.com/nukemberg))
- Use jsoup version 1.14.3 [\#1010](https://github.com/riemann/riemann/pull/1010) ([arpitjindal97](https://github.com/arpitjindal97))
- Add ClickHouse Support [\#1009](https://github.com/riemann/riemann/pull/1009) ([chhetripradeep](https://github.com/chhetripradeep))
- Fix SSLSocketfactory not supported on JDK 9+ issue [\#1008](https://github.com/riemann/riemann/pull/1008) ([chhetripradeep](https://github.com/chhetripradeep))

## [0.3.8](https://github.com/riemann/riemann/tree/0.3.8) (2021-12-13)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.7...0.3.8)

**Closed issues:**

- Which versions of riemann, if any, are susceptible to log4shell \(log4j vulnerability\)? [\#1001](https://github.com/riemann/riemann/issues/1001)

## [0.3.7](https://github.com/riemann/riemann/tree/0.3.7) (2021-11-26)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.6...0.3.7)

**Closed issues:**

- Implement InfluxDB 2.0 plugin [\#993](https://github.com/riemann/riemann/issues/993)
- Riemann writes not all messages to InfluxDB [\#992](https://github.com/riemann/riemann/issues/992)
- Auto-reload the Kafka Consumer [\#991](https://github.com/riemann/riemann/issues/991)
- Questions: prometheus-batch exceptions [\#990](https://github.com/riemann/riemann/issues/990)
- rabbitmq routing-key as a function rather than a static string [\#988](https://github.com/riemann/riemann/issues/988)
- Latest release 0.3.6 is not on clojars.org. [\#984](https://github.com/riemann/riemann/issues/984)
- `forward` does not reconnect to when connection interrupted [\#983](https://github.com/riemann/riemann/issues/983)

**Merged pull requests:**

- Fix Linux detection for Netty [\#999](https://github.com/riemann/riemann/pull/999) ([mcorbin](https://github.com/mcorbin))
- InfluxDB v2 support [\#996](https://github.com/riemann/riemann/pull/996) ([chhetripradeep](https://github.com/chhetripradeep))
- Add reaper option to keep all keys [\#995](https://github.com/riemann/riemann/pull/995) ([mreinhardt](https://github.com/mreinhardt))
- feat\(stream\): rabbitmq stream to use function or string as routing key [\#989](https://github.com/riemann/riemann/pull/989) ([wimoMisterX](https://github.com/wimoMisterX))
- fix bash get JAVA\_VERSION [\#986](https://github.com/riemann/riemann/pull/986) ([x1e](https://github.com/x1e))
- Bump jackson-databind to mitigate vulnerabilities [\#985](https://github.com/riemann/riemann/pull/985) ([sgerrand](https://github.com/sgerrand))

## [0.3.6](https://github.com/riemann/riemann/tree/0.3.6) (2020-09-06)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.5...0.3.6)

**Closed issues:**

- Query Regarding async-queue! [\#979](https://github.com/riemann/riemann/issues/979)
- Inconsistency in time across streams [\#948](https://github.com/riemann/riemann/issues/948)
- Forward events if one of downstream down\(unreachable\). [\#771](https://github.com/riemann/riemann/issues/771)

**Merged pull requests:**

- Update issue templates [\#982](https://github.com/riemann/riemann/pull/982) ([jamtur01](https://github.com/jamtur01))
- Create CODE\_OF\_CONDUCT.md [\#981](https://github.com/riemann/riemann/pull/981) ([jamtur01](https://github.com/jamtur01))
- Update packaging to produce EL8 packages too [\#978](https://github.com/riemann/riemann/pull/978) ([faxm0dem](https://github.com/faxm0dem))
- Pass http options to elasticsearch [\#976](https://github.com/riemann/riemann/pull/976) ([smortex](https://github.com/smortex))
- added pushover priorities support [\#975](https://github.com/riemann/riemann/pull/975) ([mrkooll](https://github.com/mrkooll))
- Do not record events if \*results\* is nil. [\#974](https://github.com/riemann/riemann/pull/974) ([sanel](https://github.com/sanel))
- refactoring send-lines fn in GraphiteUDPClient [\#972](https://github.com/riemann/riemann/pull/972) ([ertugrulcetin](https://github.com/ertugrulcetin))
- Fix InfluxDB typo and improve copy [\#971](https://github.com/riemann/riemann/pull/971) ([maddenp](https://github.com/maddenp))
- fix\(transport\): potential msg decoding error is handled in rabbitmq-transport [\#970](https://github.com/riemann/riemann/pull/970) ([serge-medvedev](https://github.com/serge-medvedev))
- docs\(rabbitmq-transport\): comments are tidied up [\#969](https://github.com/riemann/riemann/pull/969) ([serge-medvedev](https://github.com/serge-medvedev))
- feat\(stream\): rabbitmq stream is introduced [\#968](https://github.com/riemann/riemann/pull/968) ([serge-medvedev](https://github.com/serge-medvedev))
- refactor\(rabbitmq-transport\): configuration made even more flexible [\#967](https://github.com/riemann/riemann/pull/967) ([serge-medvedev](https://github.com/serge-medvedev))
- fix\(transport\): service/conflict? doesn't fail if rabbitmq-transport isn't started [\#965](https://github.com/riemann/riemann/pull/965) ([serge-medvedev](https://github.com/serge-medvedev))
- refactor\(transport\): rabbitmq-transport configuration made more flexible [\#964](https://github.com/riemann/riemann/pull/964) ([serge-medvedev](https://github.com/serge-medvedev))
- feat\(transport\): rabbitmq-transport got index querying support [\#963](https://github.com/riemann/riemann/pull/963) ([serge-medvedev](https://github.com/serge-medvedev))
- docs: typo's fixed; rabbitmq-related functions are either documented or made private [\#962](https://github.com/riemann/riemann/pull/962) ([serge-medvedev](https://github.com/serge-medvedev))
- feat\(transport\): rabbitmq consumer is introduced [\#961](https://github.com/riemann/riemann/pull/961) ([serge-medvedev](https://github.com/serge-medvedev))

## [0.3.5](https://github.com/riemann/riemann/tree/0.3.5) (2019-11-23)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.4...0.3.5)

**Closed issues:**

- websockets stream broken in 0.3.4 [\#959](https://github.com/riemann/riemann/issues/959)
- Explicit Java requirements [\#958](https://github.com/riemann/riemann/issues/958)
- Unable to use Env variables in riemann.config - Riemann 0.3.2 [\#956](https://github.com/riemann/riemann/issues/956)
- Trouble including functions in Riemann 0.3.3 [\#954](https://github.com/riemann/riemann/issues/954)
- Receiving exception on reload: java.lang.IllegalStateException: Could not find a suitable classloader to modify from clojure.lang.LazySeq@aa452d23 [\#950](https://github.com/riemann/riemann/issues/950)

**Merged pull requests:**

- revert http-kit to eliminate websocket interleaving bug [\#960](https://github.com/riemann/riemann/pull/960) ([dch](https://github.com/dch))

## [0.3.4](https://github.com/riemann/riemann/tree/0.3.4) (2019-09-27)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.3...0.3.4)

**Closed issues:**

- Deb package for v0.3.3 does not exist in release section [\#949](https://github.com/riemann/riemann/issues/949)

**Merged pull requests:**

- Add syntax highlighting to datadog example in docs [\#955](https://github.com/riemann/riemann/pull/955) ([rwaweber](https://github.com/rwaweber))
- Add predict-linear stream [\#953](https://github.com/riemann/riemann/pull/953) ([boernd](https://github.com/boernd))
- Added circleci config [\#952](https://github.com/riemann/riemann/pull/952) ([jamtur01](https://github.com/jamtur01))
- riemann.bin: Refactor ensure-dynamic-classloader and call on reload [\#951](https://github.com/riemann/riemann/pull/951) ([198d](https://github.com/198d))

## [0.3.3](https://github.com/riemann/riemann/tree/0.3.3) (2019-06-22)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.2...0.3.3)

**Closed issues:**

- Might need to rebuild RPM package for v0.3.2  [\#946](https://github.com/riemann/riemann/issues/946)
- InfluxDB new-stream missing fields [\#943](https://github.com/riemann/riemann/issues/943)
- Getting data from influxDB to Riemann Streams to use the metrics ?  [\#942](https://github.com/riemann/riemann/issues/942)
- many netty warning in log file [\#937](https://github.com/riemann/riemann/issues/937)
- folds/quotient-sloppy divides by zero [\#934](https://github.com/riemann/riemann/issues/934)
- Reloading configuration with OpenJDK 10 throws a java.lang.NoClassDefFoundError [\#928](https://github.com/riemann/riemann/issues/928)
- Which part-time constructor to use? [\#884](https://github.com/riemann/riemann/issues/884)
- How to have dynamic set of email users configured to send notifications based on Alert Rules? [\#826](https://github.com/riemann/riemann/issues/826)

**Merged pull requests:**

- Modernize rpm packaging [\#947](https://github.com/riemann/riemann/pull/947) ([smortex](https://github.com/smortex))

## [0.3.2](https://github.com/riemann/riemann/tree/0.3.2) (2019-05-12)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.1...0.3.2)

**Closed issues:**

- How to filter riemann-health to send only numeric metric for cpu event instead of all the process data. [\#936](https://github.com/riemann/riemann/issues/936)
- Riemann to Nagios Integration [\#933](https://github.com/riemann/riemann/issues/933)
- Is Riemann able to detect a service is up ?? [\#931](https://github.com/riemann/riemann/issues/931)
- adding a new tool [\#930](https://github.com/riemann/riemann/issues/930)
- Question: How to determine fields exist and give default value? [\#927](https://github.com/riemann/riemann/issues/927)
- Meet java.util.concurrent.RejectedExecutionException  after running some days [\#913](https://github.com/riemann/riemann/issues/913)

**Merged pull requests:**

- Add send batch to prometheus [\#945](https://github.com/riemann/riemann/pull/945) ([yershalom](https://github.com/yershalom))
- Escape backslash in ns-string to allow for compilation [\#944](https://github.com/riemann/riemann/pull/944) ([slipset](https://github.com/slipset))
- add stream untag \(inverse of tag\) [\#940](https://github.com/riemann/riemann/pull/940) ([deoqc](https://github.com/deoqc))
- Adding external dependencies fails due to missing class EntityReplacementMap. [\#939](https://github.com/riemann/riemann/pull/939) ([cresh](https://github.com/cresh))
- Add Zabbix support [\#938](https://github.com/riemann/riemann/pull/938) ([vortura](https://github.com/vortura))
- Handle div by 0 in quotient-sloppy [\#935](https://github.com/riemann/riemann/pull/935) ([jstokes](https://github.com/jstokes))
- clj-nsca: use version "0.0.4" [\#932](https://github.com/riemann/riemann/pull/932) ([mcorbin](https://github.com/mcorbin))
- Cast the time in long in the pagerduty stream. [\#929](https://github.com/riemann/riemann/pull/929) ([mcorbin](https://github.com/mcorbin))
- Allow for using Riemann as a dependency [\#881](https://github.com/riemann/riemann/pull/881) ([derekchiang](https://github.com/derekchiang))

## [0.3.1](https://github.com/riemann/riemann/tree/0.3.1) (2018-05-23)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.0...0.3.1)

**Closed issues:**

- TLS Certificate Chains [\#920](https://github.com/riemann/riemann/issues/920)
- Opsgenie API v2 [\#918](https://github.com/riemann/riemann/issues/918)
- NPE with Riemann 0.3.0 and inject! [\#917](https://github.com/riemann/riemann/issues/917)
- Could not find a suitable classloader to modify from clojure.lang.LazySeq [\#914](https://github.com/riemann/riemann/issues/914)
- Execute custom script on a state trigger / stream [\#899](https://github.com/riemann/riemann/issues/899)
- Provide example for testing with junit xml output [\#897](https://github.com/riemann/riemann/issues/897)

**Merged pull requests:**

- Upgraded to netty 4.1.25 [\#926](https://github.com/riemann/riemann/pull/926) ([jamtur01](https://github.com/jamtur01))
- Support for Opsgenie v2 API [\#925](https://github.com/riemann/riemann/pull/925) ([mcorbin](https://github.com/mcorbin))
- Docker improvements [\#923](https://github.com/riemann/riemann/pull/923) ([xrstf](https://github.com/xrstf))
- Provide a Docker image [\#922](https://github.com/riemann/riemann/pull/922) ([xrstf](https://github.com/xrstf))
- Pull in less-awful-ssl 1.0.3 [\#921](https://github.com/riemann/riemann/pull/921) ([MichaelDoyle](https://github.com/MichaelDoyle))
- Fix incorrect assertions in tests [\#919](https://github.com/riemann/riemann/pull/919) ([Mongey](https://github.com/Mongey))
- Improve the formatting of the API documentation [\#915](https://github.com/riemann/riemann/pull/915) ([mcorbin](https://github.com/mcorbin))
- config: lexicographically sort files before including them [\#912](https://github.com/riemann/riemann/pull/912) ([pyr](https://github.com/pyr))
- Enable TCP-TLS in logstash plugins [\#911](https://github.com/riemann/riemann/pull/911) ([epabced](https://github.com/epabced))
- Add multiple events support to the Graphite stream [\#910](https://github.com/riemann/riemann/pull/910) ([mcorbin](https://github.com/mcorbin))
- Add support for tags in Graphite input and output [\#909](https://github.com/riemann/riemann/pull/909) ([mcorbin](https://github.com/mcorbin))
- Add missing atom deref to properly close kafka client [\#908](https://github.com/riemann/riemann/pull/908) ([peffenberger](https://github.com/peffenberger))

## [0.3.0](https://github.com/riemann/riemann/tree/0.3.0) (2018-01-15)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.14...0.3.0)

**Closed issues:**

- Create new release [\#901](https://github.com/riemann/riemann/issues/901)
- InfluxDB: field type conflict on "riemann executor agg accepted rate" [\#898](https://github.com/riemann/riemann/issues/898)
- Issue when updating com.cemerick/pomegranate [\#890](https://github.com/riemann/riemann/issues/890)
- riemann server ws in/out rate always 0 [\#880](https://github.com/riemann/riemann/issues/880)
- Lein deps fails with lein 2.8.1 [\#870](https://github.com/riemann/riemann/issues/870)
- Error using clojure 1.9 / campfire integration [\#866](https://github.com/riemann/riemann/issues/866)
- Java Classloading [\#859](https://github.com/riemann/riemann/issues/859)
- riemann index   name a bit repetition puzzle [\#857](https://github.com/riemann/riemann/issues/857)
- Reimann fails Installation on Cent OS using RPM [\#856](https://github.com/riemann/riemann/issues/856)
- Extract riemann.streams to a separate library [\#853](https://github.com/riemann/riemann/issues/853)
- KQueue [\#851](https://github.com/riemann/riemann/issues/851)
- Catch exceptions during event expiration [\#850](https://github.com/riemann/riemann/issues/850)
- \[regression\] InfluxDB lib should be fed consistent data type [\#848](https://github.com/riemann/riemann/issues/848)
- inject-intervals! test helper [\#839](https://github.com/riemann/riemann/issues/839)
- Add index support in test mode [\#838](https://github.com/riemann/riemann/issues/838)
- Question: how to replace multi pattern [\#835](https://github.com/riemann/riemann/issues/835)
- java.lang.NullPointerException: null in Riemann [\#834](https://github.com/riemann/riemann/issues/834)
- riemann.streams/call-rescue is catching java.lang.Error [\#833](https://github.com/riemann/riemann/issues/833)
- Riemann internal metrics from haproxy to graphite [\#832](https://github.com/riemann/riemann/issues/832)
- gh-pages: quickstart, syntax highlighting bug  [\#796](https://github.com/riemann/riemann/issues/796)
- provide streams like runs but on duration [\#788](https://github.com/riemann/riemann/issues/788)
- Notification from zombie events [\#768](https://github.com/riemann/riemann/issues/768)
- riemann executor rejected rate is always 0 [\#727](https://github.com/riemann/riemann/issues/727)
- by-builder accepts multiple "forms" but passes events only to the last form [\#699](https://github.com/riemann/riemann/issues/699)

**Merged pull requests:**

- Fix logsize-rotate [\#900](https://github.com/riemann/riemann/pull/900) ([asonix](https://github.com/asonix))
- initiates controlled time in with-test-env [\#893](https://github.com/riemann/riemann/pull/893) ([mcorbin](https://github.com/mcorbin))
- Add a modifiable classloader for pomegranate. [\#892](https://github.com/riemann/riemann/pull/892) ([mcorbin](https://github.com/mcorbin))
- bump kinsky dependency [\#891](https://github.com/riemann/riemann/pull/891) ([mcorbin](https://github.com/mcorbin))
- Update project dependencies [\#889](https://github.com/riemann/riemann/pull/889) ([mcorbin](https://github.com/mcorbin))
- fix ws out rate metric and remove ws latency metric [\#888](https://github.com/riemann/riemann/pull/888) ([mcorbin](https://github.com/mcorbin))
- Refactor test imports [\#887](https://github.com/riemann/riemann/pull/887) ([mcorbin](https://github.com/mcorbin))
- Upgrade kinsky to 0.1.20 [\#885](https://github.com/riemann/riemann/pull/885) ([boernd](https://github.com/boernd))
- Add a not-expired stream [\#882](https://github.com/riemann/riemann/pull/882) ([mcorbin](https://github.com/mcorbin))
- Updates to support Java9 [\#879](https://github.com/riemann/riemann/pull/879) ([jamtur01](https://github.com/jamtur01))
- Use clj-nsca fork [\#878](https://github.com/riemann/riemann/pull/878) ([mcorbin](https://github.com/mcorbin))
- Allow for not using `index-suffix` [\#875](https://github.com/riemann/riemann/pull/875) ([derekchiang](https://github.com/derekchiang))
- Refactor test imports and fix indentation [\#873](https://github.com/riemann/riemann/pull/873) ([mcorbin](https://github.com/mcorbin))
- Modify the :host configuration in influx tests [\#872](https://github.com/riemann/riemann/pull/872) ([mcorbin](https://github.com/mcorbin))
- Mock a core in test mode, refactoring reaper [\#871](https://github.com/riemann/riemann/pull/871) ([mcorbin](https://github.com/mcorbin))
- Add the "riemann" tag in index instrumentation [\#869](https://github.com/riemann/riemann/pull/869) ([mcorbin](https://github.com/mcorbin))
- Remove campfire integration [\#867](https://github.com/riemann/riemann/pull/867) ([mcorbin](https://github.com/mcorbin))
- Allows to generate test results in junit format [\#862](https://github.com/riemann/riemann/pull/862) ([mcorbin](https://github.com/mcorbin))
- Refactoring the telegram output [\#861](https://github.com/riemann/riemann/pull/861) ([mcorbin](https://github.com/mcorbin))
- Fix the where macro for clojure 1.9 [\#858](https://github.com/riemann/riemann/pull/858) ([mcorbin](https://github.com/mcorbin))
- Catch AssertionError in stream.clj and time.clj [\#855](https://github.com/riemann/riemann/pull/855) ([mcorbin](https://github.com/mcorbin))
- Add a try/catch where events are expired [\#854](https://github.com/riemann/riemann/pull/854) ([mcorbin](https://github.com/mcorbin))
- Adds Netty Kqueue support [\#852](https://github.com/riemann/riemann/pull/852) ([jamtur01](https://github.com/jamtur01))
- Converts clojure.lang.BigInt to double in influx stream [\#849](https://github.com/riemann/riemann/pull/849) ([mcorbin](https://github.com/mcorbin))
- Remove support for JDK7 and add JDK9 [\#846](https://github.com/riemann/riemann/pull/846) ([jamtur01](https://github.com/jamtur01))
- Fix by-builder stream [\#843](https://github.com/riemann/riemann/pull/843) ([mcorbin](https://github.com/mcorbin))
- Replace Throwable by Exception [\#842](https://github.com/riemann/riemann/pull/842) ([mcorbin](https://github.com/mcorbin))
- Upgrade influxdb-java dependency [\#840](https://github.com/riemann/riemann/pull/840) ([mcorbin](https://github.com/mcorbin))
- Add Pagerduty v2 API support [\#837](https://github.com/riemann/riemann/pull/837) ([mcorbin](https://github.com/mcorbin))
- Netuitive Payload Size Reduction [\#831](https://github.com/riemann/riemann/pull/831) ([TheConnMan](https://github.com/TheConnMan))
- Add ms teams output [\#830](https://github.com/riemann/riemann/pull/830) ([boernd](https://github.com/boernd))
- Remove all \*warn-on-reflection\* warning in the influxdb stream [\#829](https://github.com/riemann/riemann/pull/829) ([mcorbin](https://github.com/mcorbin))
- Add sflatten stream [\#825](https://github.com/riemann/riemann/pull/825) ([mcorbin](https://github.com/mcorbin))
- Added some env variables for Travis testing [\#823](https://github.com/riemann/riemann/pull/823) ([jamtur01](https://github.com/jamtur01))
- bump clj-http to 3.5.0 - http-integrations over a sniproxy \o/ [\#814](https://github.com/riemann/riemann/pull/814) ([andrerocker](https://github.com/andrerocker))
- Replace refs in moving-time-window by an atom [\#811](https://github.com/riemann/riemann/pull/811) ([mcorbin](https://github.com/mcorbin))
- Extend pushover functionality [\#808](https://github.com/riemann/riemann/pull/808) ([boernd](https://github.com/boernd))
- Refactoring Elasticsearch output [\#804](https://github.com/riemann/riemann/pull/804) ([mcorbin](https://github.com/mcorbin))
- Updated a number of project dependencies [\#800](https://github.com/riemann/riemann/pull/800) ([jamtur01](https://github.com/jamtur01))
- replace refs in fixed-time-window-fn by an atom [\#797](https://github.com/riemann/riemann/pull/797) ([mcorbin](https://github.com/mcorbin))

## [0.2.14](https://github.com/riemann/riemann/tree/0.2.14) (2017-07-10)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.13...0.2.14)

**Closed issues:**

- When using keepalived+ LVS TCP check , Riemann will have Connection reset by peer error [\#828](https://github.com/riemann/riemann/issues/828)
- How to change Riemann internal metrics interval [\#827](https://github.com/riemann/riemann/issues/827)
- \[Feature Request\]  Add support for routing SNMP events  [\#824](https://github.com/riemann/riemann/issues/824)
- Add dummy environment variables to TravisCI [\#822](https://github.com/riemann/riemann/issues/822)
- Riemann output support multi graphite? [\#820](https://github.com/riemann/riemann/issues/820)
- Email not getting triggered in logstash 5.2.1 [\#819](https://github.com/riemann/riemann/issues/819)
- How to keep Graphite format in Riemann kafka output [\#818](https://github.com/riemann/riemann/issues/818)
- Need directory paths for riemann rpm in RHEL 7.2 [\#817](https://github.com/riemann/riemann/issues/817)
- Need directory locations for riemann rpm file RHEL 7.2 [\#816](https://github.com/riemann/riemann/issues/816)
- Unexpected behavior regarding events with missing fields specified in by macro [\#813](https://github.com/riemann/riemann/issues/813)
- Riemann-health not working [\#809](https://github.com/riemann/riemann/issues/809)
- \[feature request\] Support batch functionality in elasticsearch output [\#791](https://github.com/riemann/riemann/issues/791)
- Reload not working on RHEL 7.1 [\#780](https://github.com/riemann/riemann/issues/780)
- \[InfluxDB\] Allow :tag-fields to be set per-event  [\#742](https://github.com/riemann/riemann/issues/742)
- Document supported influxdb versions [\#723](https://github.com/riemann/riemann/issues/723)
- Riemann notifications seems to be delayed [\#713](https://github.com/riemann/riemann/issues/713)

## [0.2.13](https://github.com/riemann/riemann/tree/0.2.13) (2017-04-06)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.12...0.2.13)

**Closed issues:**

- Pretty print test output [\#787](https://github.com/riemann/riemann/issues/787)
- Override logging config from logback.xml \(or similar logback config\) [\#783](https://github.com/riemann/riemann/issues/783)
- running a single test \(single namespace\) [\#775](https://github.com/riemann/riemann/issues/775)
- \[Feature Request\] Add kafka as input option [\#767](https://github.com/riemann/riemann/issues/767)
- 0.2.12 not published to clojars [\#764](https://github.com/riemann/riemann/issues/764)
- Accept more Twilio params and do not force 'from' [\#761](https://github.com/riemann/riemann/issues/761)
- \[Question\] Callbacks that runs periodically [\#759](https://github.com/riemann/riemann/issues/759)
- Stream state and \(by\) [\#758](https://github.com/riemann/riemann/issues/758)
- \[Feature request\] keep streams state between reloads [\#743](https://github.com/riemann/riemann/issues/743)
- \[Feature request\] Improve influxdb support [\#739](https://github.com/riemann/riemann/issues/739)
- \[Feature request\] support microseconds in :time [\#738](https://github.com/riemann/riemann/issues/738)
- release: prometheus [\#733](https://github.com/riemann/riemann/issues/733)
- \[Feature request\] Influxdb supports dynamic database & retention policy targeting [\#730](https://github.com/riemann/riemann/issues/730)
- netty 64bit shared library fails on non-glibc systems [\#725](https://github.com/riemann/riemann/issues/725)
- Include distribution files in GitHub releases? [\#716](https://github.com/riemann/riemann/issues/716)

**Merged pull requests:**

- Fix "bootstrap.servers" typo [\#795](https://github.com/riemann/riemann/pull/795) ([mcorbin](https://github.com/mcorbin))
- Configure logging via logback.configurationFile environment variable [\#794](https://github.com/riemann/riemann/pull/794) ([ggrossetie](https://github.com/ggrossetie))
- uses humane-test-output to pretty print test outputs [\#790](https://github.com/riemann/riemann/pull/790) ([mcorbin](https://github.com/mcorbin))
- add kafka as input option [\#781](https://github.com/riemann/riemann/pull/781) ([boernd](https://github.com/boernd))
- Remove trailing whitespaces on the netuitive code [\#779](https://github.com/riemann/riemann/pull/779) ([mcorbin](https://github.com/mcorbin))
- add ability to run single test \(namespace\), fixes \#775 [\#776](https://github.com/riemann/riemann/pull/776) ([andrusieczko](https://github.com/andrusieczko))
- Remove capacitor dependency [\#774](https://github.com/riemann/riemann/pull/774) ([mcorbin](https://github.com/mcorbin))
- Add an `:options` parameter to the pagerduty stream [\#773](https://github.com/riemann/riemann/pull/773) ([mcorbin](https://github.com/mcorbin))
- Bump nrepl to 0.2.12 [\#769](https://github.com/riemann/riemann/pull/769) ([mcorbin](https://github.com/mcorbin))
- Fix some tests for time micro [\#765](https://github.com/riemann/riemann/pull/765) ([mcorbin](https://github.com/mcorbin))
- Revert coalesce stream state storage [\#763](https://github.com/riemann/riemann/pull/763) ([mcorbin](https://github.com/mcorbin))
- Twilio : refactor options [\#762](https://github.com/riemann/riemann/pull/762) ([mcorbin](https://github.com/mcorbin))
- Add kafka as an output option [\#760](https://github.com/riemann/riemann/pull/760) ([boernd](https://github.com/boernd))
- Restore netty "queue size" metric [\#757](https://github.com/riemann/riemann/pull/757) ([mcorbin](https://github.com/mcorbin))
- Add "riemann" tag to Instrumented transports and services [\#756](https://github.com/riemann/riemann/pull/756) ([mcorbin](https://github.com/mcorbin))
- Fix typo in the doc string of Instrumented protocol. [\#755](https://github.com/riemann/riemann/pull/755) ([avichalp](https://github.com/avichalp))
- Allow specifying HTTP basic authentication credentials when writing to Elasticsearch [\#754](https://github.com/riemann/riemann/pull/754) ([dhruvbansal](https://github.com/dhruvbansal))
- Add Netuitive plugin [\#753](https://github.com/riemann/riemann/pull/753) ([joepusateri](https://github.com/joepusateri))
- Clarify throttle doc-string [\#751](https://github.com/riemann/riemann/pull/751) ([Ben-M](https://github.com/Ben-M))
- Slack : cast event :tags into vector [\#749](https://github.com/riemann/riemann/pull/749) ([mcorbin](https://github.com/mcorbin))
- coalesce: simplify coalesce state storage [\#748](https://github.com/riemann/riemann/pull/748) ([pyr](https://github.com/pyr))
- Prometheus label / body only support some char. [\#747](https://github.com/riemann/riemann/pull/747) ([shinji62](https://github.com/shinji62))
- Keep coalesce state between reloads [\#744](https://github.com/riemann/riemann/pull/744) ([mcorbin](https://github.com/mcorbin))
- \[Need review\] Refactoring influxdb [\#741](https://github.com/riemann/riemann/pull/741) ([mcorbin](https://github.com/mcorbin))
- Telegram notification support [\#714](https://github.com/riemann/riemann/pull/714) ([islander](https://github.com/islander))

## [0.2.12](https://github.com/riemann/riemann/tree/0.2.12) (2016-12-06)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.11...0.2.12)

**Implemented enhancements:**

- Suppress exception logging if the exception is handled by `exception-stream` [\#726](https://github.com/riemann/riemann/issues/726)
- Add support for KairosDB HTTP integration and metric TTLs [\#627](https://github.com/riemann/riemann/pull/627) ([ryancrum](https://github.com/ryancrum))

**Fixed bugs:**

- async-queue! \(threadpool-service\) never go above core-pool-size [\#668](https://github.com/riemann/riemann/issues/668)

**Closed issues:**

- Writing empty tag field values to Influx is failing [\#732](https://github.com/riemann/riemann/issues/732)
- ws-server binds to all interfaces/IP addresses even if config requires otherwise [\#721](https://github.com/riemann/riemann/issues/721)
- \[question/help\] multiple async-queues [\#720](https://github.com/riemann/riemann/issues/720)
- Hosting of RPMs & DEBs [\#718](https://github.com/riemann/riemann/issues/718)
- Influx 0.13.0 with Riemann 0.2.10 - database is required [\#715](https://github.com/riemann/riemann/issues/715)
- Added lein plugin template [\#709](https://github.com/riemann/riemann/issues/709)
- Can't use default user and image with Slack integration [\#707](https://github.com/riemann/riemann/issues/707)
- TCP backlog [\#705](https://github.com/riemann/riemann/issues/705)
- REST interface for integrating with local services [\#703](https://github.com/riemann/riemann/issues/703)
- \[question\] forward "mapped" metrics into influxdb [\#700](https://github.com/riemann/riemann/issues/700)
- Question : lein run on the cloned Riemann git  [\#698](https://github.com/riemann/riemann/issues/698)
- Question : Riemann to InfluxDB connection [\#697](https://github.com/riemann/riemann/issues/697)
- Query : Riemann Slack integration [\#688](https://github.com/riemann/riemann/issues/688)
- Its not an issue but a question [\#687](https://github.com/riemann/riemann/issues/687)
- Client might be not reading acks fast enough or network is broken [\#686](https://github.com/riemann/riemann/issues/686)
- Events not being reinjected to the main streams after TTL is expired. [\#676](https://github.com/riemann/riemann/issues/676)
- graphite pickle/batched writes [\#671](https://github.com/riemann/riemann/issues/671)
- influxdb JSON write protocol has been deprecated [\#669](https://github.com/riemann/riemann/issues/669)
- Documentation: Default port used for tcp-server [\#662](https://github.com/riemann/riemann/issues/662)

**Merged pull requests:**

- Fix sse listening address [\#737](https://github.com/riemann/riemann/pull/737) ([mcorbin](https://github.com/mcorbin))
- Fix RuntimeException in udp graphite-server [\#736](https://github.com/riemann/riemann/pull/736) ([mcorbin](https://github.com/mcorbin))
- fix websocket listening address [\#735](https://github.com/riemann/riemann/pull/735) ([mcorbin](https://github.com/mcorbin))
- Remove tags and fields if value is nil or empty [\#734](https://github.com/riemann/riemann/pull/734) ([mcorbin](https://github.com/mcorbin))
- don't log exceptions if in exception-stream [\#729](https://github.com/riemann/riemann/pull/729) ([mcorbin](https://github.com/mcorbin))
- revert previous 'fix' closing unwritable channels [\#724](https://github.com/riemann/riemann/pull/724) ([abailly](https://github.com/abailly))
- Add riemann.elasticsearch [\#722](https://github.com/riemann/riemann/pull/722) ([eguven](https://github.com/eguven))
- Add HTML body support for mailgun [\#719](https://github.com/riemann/riemann/pull/719) ([jerray](https://github.com/jerray))
- Fix nested escaping of strings [\#717](https://github.com/riemann/riemann/pull/717) ([frankiesardo](https://github.com/frankiesardo))
- Add ChannelOption/SO\_BACKLOG to TCP server [\#706](https://github.com/riemann/riemann/pull/706) ([pradeepchhetri](https://github.com/pradeepchhetri))
- Clarify GC behavior of \(by\) streams [\#704](https://github.com/riemann/riemann/pull/704) ([blalor](https://github.com/blalor))
- Add the ability to read SNS credentials from the default credential chain [\#701](https://github.com/riemann/riemann/pull/701) ([hexedpackets](https://github.com/hexedpackets))
- Removed deprecated functions: within, without and combine [\#696](https://github.com/riemann/riemann/pull/696) ([jamtur01](https://github.com/jamtur01))
- Fixes \#374 - Librato error without metric [\#695](https://github.com/riemann/riemann/pull/695) ([jamtur01](https://github.com/jamtur01))
- Updating netty to 4.1.0 [\#694](https://github.com/riemann/riemann/pull/694) ([pradeepchhetri](https://github.com/pradeepchhetri))
- Added Prometheus Plugin [\#692](https://github.com/riemann/riemann/pull/692) ([pradeepchhetri](https://github.com/pradeepchhetri))
- Added Druid plugin [\#691](https://github.com/riemann/riemann/pull/691) ([pradeepchhetri](https://github.com/pradeepchhetri))
- Renamed com.aphyr to io.riemann in Riemann core [\#685](https://github.com/riemann/riemann/pull/685) ([jamtur01](https://github.com/jamtur01))
- Include api changes to help others in upgrading [\#683](https://github.com/riemann/riemann/pull/683) ([agile](https://github.com/agile))
- Allow explicit config of Slack HTTP connection params [\#681](https://github.com/riemann/riemann/pull/681) ([dbriones](https://github.com/dbriones))
- Add batch forwarding for datadog [\#679](https://github.com/riemann/riemann/pull/679) ([thearthur](https://github.com/thearthur))
- Fixed deprecation warning to use correct new function. [\#678](https://github.com/riemann/riemann/pull/678) ([jamtur01](https://github.com/jamtur01))
- slack: fix tags handling [\#677](https://github.com/riemann/riemann/pull/677) ([mfournier](https://github.com/mfournier))

## [0.2.11](https://github.com/riemann/riemann/tree/0.2.11) (2016-04-20)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.10...0.2.11)

**Implemented enhancements:**

- hipchat no longer honors :from, sends server in post body [\#643](https://github.com/riemann/riemann/issues/643)
- streams: add by-builder and an additional arg to new-fork [\#661](https://github.com/riemann/riemann/pull/661) ([pyr](https://github.com/pyr))
- index: update is now a reserved name [\#660](https://github.com/riemann/riemann/pull/660) ([pyr](https://github.com/pyr))
- project: move to clojure 1.8 [\#653](https://github.com/riemann/riemann/pull/653) ([pyr](https://github.com/pyr))
- Allow Cloudwatch to authenticate using IAM Instance Roles [\#645](https://github.com/riemann/riemann/pull/645) ([iramello](https://github.com/iramello))
- close channel when it becomes unwritable [\#640](https://github.com/riemann/riemann/pull/640) ([abailly](https://github.com/abailly))
- Add tags to opsgenie events [\#596](https://github.com/riemann/riemann/pull/596) ([vixns](https://github.com/vixns))

**Fixed bugs:**

- error in fixed-time-window example in how-to [\#650](https://github.com/riemann/riemann/issues/650)
- riemann stops working properly following OOME [\#623](https://github.com/riemann/riemann/issues/623)
- querying the index stalls with numeric custom attributes [\#564](https://github.com/riemann/riemann/issues/564)

**Closed issues:**

- Twilio integration is not working [\#666](https://github.com/riemann/riemann/issues/666)
- Pagerduty with proxy settings. [\#664](https://github.com/riemann/riemann/issues/664)
- RejectedExecutionException in logs [\#655](https://github.com/riemann/riemann/issues/655)
- API documentation is missing [\#642](https://github.com/riemann/riemann/issues/642)
- java.lang.NullPointerException Sending an email [\#638](https://github.com/riemann/riemann/issues/638)
- support for influxdb subscriptions  [\#635](https://github.com/riemann/riemann/issues/635)
- Why is version specific influxdb constructors not private [\#634](https://github.com/riemann/riemann/issues/634)
- stream with http REST to send SMS，just like email [\#631](https://github.com/riemann/riemann/issues/631)
- Undefined behavior in index expire [\#616](https://github.com/riemann/riemann/issues/616)
- Trap SNMP events in riemann [\#613](https://github.com/riemann/riemann/issues/613)
- Environment Variable [\#610](https://github.com/riemann/riemann/issues/610)
- Make sure or document that commas fail in InfluxDB 0.9 [\#602](https://github.com/riemann/riemann/issues/602)
- Sending a UnixNano timestamp causes IntOverflow in InfluxDB [\#600](https://github.com/riemann/riemann/issues/600)
- InvalidProtocolBufferException on big tcp package [\#597](https://github.com/riemann/riemann/issues/597)
- Memory leak in netty 4.0.24.Final [\#594](https://github.com/riemann/riemann/issues/594)
- Update api documentation [\#593](https://github.com/riemann/riemann/issues/593)
- Question on calculating rate of events using given timestamp [\#592](https://github.com/riemann/riemann/issues/592)
- question [\#591](https://github.com/riemann/riemann/issues/591)
- Nanoseconds precision for timestamps [\#590](https://github.com/riemann/riemann/issues/590)
- java.lang.IllegalArgumentException: Level cannot be null or an empty String when using nagios export [\#589](https://github.com/riemann/riemann/issues/589)
- Consider switching from log4j? [\#588](https://github.com/riemann/riemann/issues/588)
- How to enable InfluxDB 0.9 support intoduced in 0.2.10? [\#587](https://github.com/riemann/riemann/issues/587)
- InfluxDB 0.9 integration broken [\#574](https://github.com/riemann/riemann/issues/574)
- java.lang.NoClassDefFoundError: riemann/bin [\#573](https://github.com/riemann/riemann/issues/573)
- How to start Riemann in Windows 7 [\#570](https://github.com/riemann/riemann/issues/570)
- fixed-time-window doesn't seem to fire the vector of events by itself [\#563](https://github.com/riemann/riemann/issues/563)
- Trouble with fixed time window [\#558](https://github.com/riemann/riemann/issues/558)
- Delete old log files [\#556](https://github.com/riemann/riemann/issues/556)
- Process locks up when receiving too much information [\#554](https://github.com/riemann/riemann/issues/554)
- Riemann testing gives unexpected output [\#552](https://github.com/riemann/riemann/issues/552)
- logging to multiple files breaks when using :logsize-rotate [\#551](https://github.com/riemann/riemann/issues/551)
-  \(depend plugin artifact version options\) seems broken whith {offline? false} [\#547](https://github.com/riemann/riemann/issues/547)
- Sending reserved protocol-buffer fields results in weird behaviour [\#545](https://github.com/riemann/riemann/issues/545)
- Hyperloglog Stream [\#537](https://github.com/riemann/riemann/issues/537)
- Enhancement: RPM init scripts return proper errors when startup fails \(but not reload\) [\#524](https://github.com/riemann/riemann/issues/524)
- links to source in api docs broken [\#519](https://github.com/riemann/riemann/issues/519)

**Merged pull requests:**

- test.clj: test-stream-intervals: minor typo [\#680](https://github.com/riemann/riemann/pull/680) ([Anvil](https://github.com/Anvil))
- Updated CHANGELOG for 0.2.11 [\#672](https://github.com/riemann/riemann/pull/672) ([jamtur01](https://github.com/jamtur01))
- hipchat: remove threading as dissoc takes key sequnce as arguments [\#670](https://github.com/riemann/riemann/pull/670) ([thenonameguy](https://github.com/thenonameguy))
- slack: properly setup :fallback in bundled formatters [\#667](https://github.com/riemann/riemann/pull/667) ([mfournier](https://github.com/mfournier))
- Twilio REST API POST parameter names are updated. [\#663](https://github.com/riemann/riemann/pull/663) ([metebalci](https://github.com/metebalci))
- folds: add modes and mode, fixes \#363 [\#652](https://github.com/riemann/riemann/pull/652) ([pyr](https://github.com/pyr))
- Fixed PagerDuty documentation [\#641](https://github.com/riemann/riemann/pull/641) ([jamtur01](https://github.com/jamtur01))
- Added support for a PagerDuty formatter for events [\#639](https://github.com/riemann/riemann/pull/639) ([jamtur01](https://github.com/jamtur01))
- Fix percentiles documentation for correct service name [\#626](https://github.com/riemann/riemann/pull/626) ([jamtur01](https://github.com/jamtur01))
- Allow overriding graphite metric conversion method [\#625](https://github.com/riemann/riemann/pull/625) ([agile](https://github.com/agile))
- Bump the nREPL dep to 0.2.11 [\#619](https://github.com/riemann/riemann/pull/619) ([bbatsov](https://github.com/bbatsov))
- Xymon: fixes, scalability, multiple xymon host, error handling [\#615](https://github.com/riemann/riemann/pull/615) ([Anvil](https://github.com/Anvil))
- VictorOps integration [\#614](https://github.com/riemann/riemann/pull/614) ([mallman](https://github.com/mallman))
- Fix misplaced docstring in riemann.slack [\#612](https://github.com/riemann/riemann/pull/612) ([greglook](https://github.com/greglook))
- Fix InfluxDB 0.9 tags [\#611](https://github.com/riemann/riemann/pull/611) ([ghost](https://github.com/ghost))
- add insecure flag for influxdb in case cert is self-signed for https [\#609](https://github.com/riemann/riemann/pull/609) ([jeanpralo](https://github.com/jeanpralo))
- Freshen WIP InfluxDB 0.9 support [\#608](https://github.com/riemann/riemann/pull/608) ([ghost](https://github.com/ghost))
- Xymon: ability to support more message types. Enable/Disable messages implementation [\#607](https://github.com/riemann/riemann/pull/607) ([Anvil](https://github.com/Anvil))
- fill-in-last\*: apply arbitrary function to last event [\#606](https://github.com/riemann/riemann/pull/606) ([Anvil](https://github.com/Anvil))
- Add -v and version command to display Lein or POM version [\#604](https://github.com/riemann/riemann/pull/604) ([aphyr](https://github.com/aphyr))
- Add ChannelOption/RCVBUF\_ALLOCATOR to UDP server [\#603](https://github.com/riemann/riemann/pull/603) ([MichaelDoyle](https://github.com/MichaelDoyle))
- Fix logging, use logback instead log4j [\#598](https://github.com/riemann/riemann/pull/598) ([juise](https://github.com/juise))
- Updating netty to 4.0.30.Final [\#595](https://github.com/riemann/riemann/pull/595) ([hiloboy0119](https://github.com/hiloboy0119))
- logging: fallback to "riemann" when an unknown layout is provided [\#586](https://github.com/riemann/riemann/pull/586) ([vincentbernat](https://github.com/vincentbernat))
- Allow graphite to take a function as host name [\#582](https://github.com/riemann/riemann/pull/582) ([joerayme](https://github.com/joerayme))
- \[WIP\] Add first draft of encoding influxdb line protocol [\#575](https://github.com/riemann/riemann/pull/575) ([timbuchwaldt](https://github.com/timbuchwaldt))

## [0.2.10](https://github.com/riemann/riemann/tree/0.2.10) (2015-07-21)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.9...0.2.10)

**Closed issues:**

- tapped index allows duplicate host/service pairs [\#565](https://github.com/riemann/riemann/issues/565)
- Events' reinjection and expiration not working correctly [\#538](https://github.com/riemann/riemann/issues/538)
- Start up failure on OpenJDK 1.6.0\_34 [\#535](https://github.com/riemann/riemann/issues/535)
- Riemann API docs site shows 0.2.7 instead of 0.2.8 [\#518](https://github.com/riemann/riemann/issues/518)

**Merged pull requests:**

- JVM opt for disabling epoll [\#584](https://github.com/riemann/riemann/pull/584) ([pharaujo](https://github.com/pharaujo))
- Convert TSDB tags to custom fields [\#583](https://github.com/riemann/riemann/pull/583) ([pharaujo](https://github.com/pharaujo))
- Do not escape slack markup formatting in custom formatter. [\#580](https://github.com/riemann/riemann/pull/580) ([zackdever](https://github.com/zackdever))
- add test/lookup [\#566](https://github.com/riemann/riemann/pull/566) ([mfournier](https://github.com/mfournier))
- fixing problem with “name” change and rejected null values in influxdb rc31/rc32 [\#562](https://github.com/riemann/riemann/pull/562) ([mbuczko](https://github.com/mbuczko))
- email: allow sequentials as input [\#560](https://github.com/riemann/riemann/pull/560) ([pyr](https://github.com/pyr))
- Updated slack default-formatter and extended-formatter [\#557](https://github.com/riemann/riemann/pull/557) ([pradeepchhetri](https://github.com/pradeepchhetri))
- Add InfluxDB 0.9 output [\#555](https://github.com/riemann/riemann/pull/555) ([greglook](https://github.com/greglook))
- Add support for private servers; Migrate to v2 API [\#553](https://github.com/riemann/riemann/pull/553) ([jjungnickel](https://github.com/jjungnickel))
- Add Pushover [\#549](https://github.com/riemann/riemann/pull/549) ([amir](https://github.com/amir))
- check emails recipients are strings [\#543](https://github.com/riemann/riemann/pull/543) ([tcrayford](https://github.com/tcrayford))
- instrument the number of unique events in the index [\#541](https://github.com/riemann/riemann/pull/541) ([tcrayford](https://github.com/tcrayford))
- Faxmodem so rcvbuf [\#540](https://github.com/riemann/riemann/pull/540) ([faxm0dem](https://github.com/faxm0dem))
- bump jdk version dependency [\#536](https://github.com/riemann/riemann/pull/536) ([mfournier](https://github.com/mfournier))
- expire by branch if event is expired [\#525](https://github.com/riemann/riemann/pull/525) ([itaifrenkel](https://github.com/itaifrenkel))

## [0.2.9](https://github.com/riemann/riemann/tree/0.2.9) (2015-03-09)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.8...0.2.9)

**Closed issues:**

- unclean close of SSE transport connection causes high cpu, memory leak [\#522](https://github.com/riemann/riemann/issues/522)

**Merged pull requests:**

- Put EXTRA\_CLASSPATH after riemann.jar [\#520](https://github.com/riemann/riemann/pull/520) ([md5](https://github.com/md5))
- Specify seconds resolution in output to InfluxDB [\#517](https://github.com/riemann/riemann/pull/517) ([fhalim](https://github.com/fhalim))
- Retain time specified in event [\#516](https://github.com/riemann/riemann/pull/516) ([fhalim](https://github.com/fhalim))

## [0.2.8](https://github.com/riemann/riemann/tree/0.2.8) (2015-01-09)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.7...0.2.8)

## [0.2.7](https://github.com/riemann/riemann/tree/0.2.7) (2015-01-06)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.6...0.2.7)

## [0.2.6](https://github.com/riemann/riemann/tree/0.2.6) (2014-07-18)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.5...0.2.6)

## [0.2.5](https://github.com/riemann/riemann/tree/0.2.5) (2014-04-30)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.4...0.2.5)

## [0.2.4](https://github.com/riemann/riemann/tree/0.2.4) (2013-11-25)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.3...0.2.4)

## [0.2.3](https://github.com/riemann/riemann/tree/0.2.3) (2013-11-12)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.2...0.2.3)

## [0.2.2](https://github.com/riemann/riemann/tree/0.2.2) (2013-06-05)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.1...0.2.2)

## [0.2.1](https://github.com/riemann/riemann/tree/0.2.1) (2013-04-08)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.0...0.2.1)

## [0.2.0](https://github.com/riemann/riemann/tree/0.2.0) (2013-03-17)

[Full Changelog](https://github.com/riemann/riemann/compare/0.1.5...0.2.0)

## [0.1.5](https://github.com/riemann/riemann/tree/0.1.5) (2012-12-04)

[Full Changelog](https://github.com/riemann/riemann/compare/0.1.4...0.1.5)

## [0.1.4](https://github.com/riemann/riemann/tree/0.1.4) (2012-12-04)

[Full Changelog](https://github.com/riemann/riemann/compare/0.1.3...0.1.4)

## [0.1.3](https://github.com/riemann/riemann/tree/0.1.3) (2012-11-13)

[Full Changelog](https://github.com/riemann/riemann/compare/0.1.2...0.1.3)

## [0.1.2](https://github.com/riemann/riemann/tree/0.1.2) (2012-06-17)

[Full Changelog](https://github.com/riemann/riemann/compare/0.1.1...0.1.2)

## [0.1.1](https://github.com/riemann/riemann/tree/0.1.1) (2012-05-24)

[Full Changelog](https://github.com/riemann/riemann/compare/0.1.0...0.1.1)

## [0.1.0](https://github.com/riemann/riemann/tree/0.1.0) (2012-03-08)

[Full Changelog](https://github.com/riemann/riemann/compare/d87e6dfa1a8d9cd12de324d60b4adfa37afa0d4f...0.1.0)



\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*
