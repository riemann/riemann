# Changelog

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

- Updated slf4j-over-log4j to 1.7.32
- Updated cloure/tools.logging to 1.2.1

## [0.3.7](https://github.com/riemann/riemann/tree/0.3.7) (2021-11-26)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.6...0.3.7)

- Fix Linux detection for Netty
- InfluxDB v2 support
- Add reaper option to keep all keys
- feat(stream): rabbitmq stream to use function or string as routing key
- fix bash get JAVA_VERSION
- Bump jackson-databind to mitigate vulnerabilities

## [0.3.6](https://github.com/riemann/riemann/tree/0.3.6) (2020-09-06)

[Full Changelog](https://github.com/riemann/riemann/compare/0.3.5...0.3.6)

**Closed issues:**

- Query Regarding async-queue! [\#979](https://github.com/riemann/riemann/issues/979)
- Inconsistency in time across streams [\#948](https://github.com/riemann/riemann/issues/948)
- Forward events if one of downstream down\(unreachable\). [\#771](https://github.com/riemann/riemann/issues/771)

**Merged pull requests:**

- Update issue templates [\#982](https://github.com/riemann/riemann/pull/982) ([jamtur01](https://github.com/jamtur01))
- Create CODE_OF_CONDUCT.md [\#981](https://github.com/riemann/riemann/pull/981) ([jamtur01](https://github.com/jamtur01))
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

- Might need to rebuild RPM package for v0.3.2 [\#946](https://github.com/riemann/riemann/issues/946)
- InfluxDB new-stream missing fields [\#943](https://github.com/riemann/riemann/issues/943)
- Getting data from influxDB to Riemann Streams to use the metrics ? [\#942](https://github.com/riemann/riemann/issues/942)
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
- Meet java.util.concurrent.RejectedExecutionException after running some days [\#913](https://github.com/riemann/riemann/issues/913)

**Merged pull requests:**

- Add send batch to prometheus [\#945](https://github.com/riemann/riemann/pull/945) ([yershalom](https://github.com/yershalom))
- Escape backslash in ns-string to allow for compilation [\#944](https://github.com/riemann/riemann/pull/944) ([slipset](https://github.com/slipset))
- add stream untag \(inverse of tag\) [\#940](https://github.com/riemann/riemann/pull/940) ([deoqc](https://github.com/deoqc))
- Adding external dependencies fails due to missing class EntityReplacementMap. [\#939](https://github.com/riemann/riemann/pull/939) ([cresh](https://github.com/cresh))
- Add Zabbix support [\#938](https://github.com/riemann/riemann/pull/938) ([vortura](https://github.com/vortura))
- Handle div by 0 in quotient-sloppy [\#935](https://github.com/riemann/riemann/pull/935) ([jstokes](https://github.com/jstokes))
- clj-nsca: use version "0.0.4" [\#932](https://github.com/riemann/riemann/pull/932) ([mcorbin](https://github.com/mcorbin))
- Cast the time in long in the pagerduty stream. [\#929](https://github.com/riemann/riemann/pull/929) ([mcorbin](https://github.com/mcorbin))
- Docker documentation [\#924](https://github.com/riemann/riemann/pull/924) ([xrstf](https://github.com/xrstf))
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
- riemann index name a bit repetition puzzle [\#857](https://github.com/riemann/riemann/issues/857)
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
- gh-pages: quickstart, syntax highlighting bug [\#796](https://github.com/riemann/riemann/issues/796)
- provide streams like runs but on duration [\#788](https://github.com/riemann/riemann/issues/788)
- Notification from zombie events [\#768](https://github.com/riemann/riemann/issues/768)
- riemann executor rejected rate is always 0 [\#727](https://github.com/riemann/riemann/issues/727)
- by-builder accepts multiple "forms" but passes events only to the last form [\#699](https://github.com/riemann/riemann/issues/699)

**Merged pull requests:**

- Add pagerduty v2 documentation [\#902](https://github.com/riemann/riemann/pull/902) ([mcorbin](https://github.com/mcorbin))
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

## [0.2.14](https://github.com/riemann/riemann/tree/0.2.14) (2017-07-10)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.13...0.2.14)

**Closed issues:**

- When using keepalived+ LVS TCP check , Riemann will have Connection reset by peer error [\#828](https://github.com/riemann/riemann/issues/828)
- How to change Riemann internal metrics interval [\#827](https://github.com/riemann/riemann/issues/827)
- \[Feature Request\] Add support for routing SNMP events [\#824](https://github.com/riemann/riemann/issues/824)
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
- \[InfluxDB\] Allow :tag-fields to be set per-event [\#742](https://github.com/riemann/riemann/issues/742)
- Document supported influxdb versions [\#723](https://github.com/riemann/riemann/issues/723)
- Riemann notifications seems to be delayed [\#713](https://github.com/riemann/riemann/issues/713)

**Merged pull requests:**

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
- Configure logging via logback.configurationFile environment variable [\#794](https://github.com/riemann/riemann/pull/794) ([Mogztter](https://github.com/Mogztter))
- uses humane-test-output to pretty print test outputs [\#790](https://github.com/riemann/riemann/pull/790) ([mcorbin](https://github.com/mcorbin))
- add kafka as input option [\#781](https://github.com/riemann/riemann/pull/781) ([boernd](https://github.com/boernd))
- Remove trailing whitespaces on the netuitive code [\#779](https://github.com/riemann/riemann/pull/779) ([mcorbin](https://github.com/mcorbin))
- add ability to run single test \(namespace\), fixes \#775 [\#776](https://github.com/riemann/riemann/pull/776) ([andrusieczko](https://github.com/andrusieczko))
- Add an `:options` parameter to the pagerduty stream [\#773](https://github.com/riemann/riemann/pull/773) ([mcorbin](https://github.com/mcorbin))
- Bump nrepl to 0.2.12 [\#769](https://github.com/riemann/riemann/pull/769) ([mcorbin](https://github.com/mcorbin))
- Fix some tests for time micro [\#765](https://github.com/riemann/riemann/pull/765) ([mcorbin](https://github.com/mcorbin))
- Revert coalesce stream state storage [\#763](https://github.com/riemann/riemann/pull/763) ([mcorbin](https://github.com/mcorbin))
- Twilio : refactor options [\#762](https://github.com/riemann/riemann/pull/762) ([mcorbin](https://github.com/mcorbin))
- Add kafka as an output option [\#760](https://github.com/riemann/riemann/pull/760) ([boernd](https://github.com/boernd))
- Restore netty "queue size" metric [\#757](https://github.com/riemann/riemann/pull/757) ([mcorbin](https://github.com/mcorbin))
- Fix typo in the doc string of Instrumented protocol. [\#755](https://github.com/riemann/riemann/pull/755) ([avichalp](https://github.com/avichalp))
- Allow specifying HTTP basic authentication credentials when writing to Elasticsearch [\#754](https://github.com/riemann/riemann/pull/754) ([dhruvbansal](https://github.com/dhruvbansal))
- Add Netuitive plugin [\#753](https://github.com/riemann/riemann/pull/753) ([joepusateri](https://github.com/joepusateri))
- Clarify throttle doc-string [\#751](https://github.com/riemann/riemann/pull/751) ([Ben-M](https://github.com/Ben-M))
- Slack : cast event :tags into vector [\#749](https://github.com/riemann/riemann/pull/749) ([mcorbin](https://github.com/mcorbin))
- coalesce: simplify coalesce state storage [\#748](https://github.com/riemann/riemann/pull/748) ([pyr](https://github.com/pyr))
- Prometheus label / body only support some char. [\#747](https://github.com/riemann/riemann/pull/747) ([shinji62](https://github.com/shinji62))
- Fix typo [\#746](https://github.com/riemann/riemann/pull/746) ([mcorbin](https://github.com/mcorbin))
- Keep coalesce state between reloads [\#744](https://github.com/riemann/riemann/pull/744) ([mcorbin](https://github.com/mcorbin))
- \[Need review\] Refactoring influxdb [\#741](https://github.com/riemann/riemann/pull/741) ([mcorbin](https://github.com/mcorbin))
- Telegram notification support [\#714](https://github.com/riemann/riemann/pull/714) ([islander](https://github.com/islander))

## [0.2.12](https://github.com/riemann/riemann/tree/0.2.12) (2016-12-06)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.11...0.2.12)

**Implemented enhancements:**

- Suppress exception logging if the exception is handled by `exception-stream` [\#726](https://github.com/riemann/riemann/issues/726)
- Improve error messages for librato with missing metrics [\#374](https://github.com/riemann/riemann/issues/374)
- Add support for KairosDB HTTP integration and metric TTLs [\#627](https://github.com/riemann/riemann/pull/627) ([ryancrum](https://github.com/ryancrum))

**Fixed bugs:**

- async-queue! \(threadpool-service\) never go above core-pool-size [\#668](https://github.com/riemann/riemann/issues/668)
- Graphite UDP server gets ClassCastException [\#509](https://github.com/riemann/riemann/issues/509)
- fold-interval-metric doesn't run on second inject! [\#433](https://github.com/riemann/riemann/issues/433)

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
- Question : lein run on the cloned Riemann git [\#698](https://github.com/riemann/riemann/issues/698)
- Question : Riemann to InfluxDB connection [\#697](https://github.com/riemann/riemann/issues/697)
- Query : Riemann Slack integration [\#688](https://github.com/riemann/riemann/issues/688)
- Its not an issue but a question [\#687](https://github.com/riemann/riemann/issues/687)
- Client might be not reading acks fast enough or network is broken [\#686](https://github.com/riemann/riemann/issues/686)
- Events not being reinjected to the main streams after TTL is expired. [\#676](https://github.com/riemann/riemann/issues/676)
- graphite pickle/batched writes [\#671](https://github.com/riemann/riemann/issues/671)
- influxdb JSON write protocol has been deprecated [\#669](https://github.com/riemann/riemann/issues/669)
- Documentation: Default port used for tcp-server [\#662](https://github.com/riemann/riemann/issues/662)
- Log should roll up repeated messages [\#495](https://github.com/riemann/riemann/issues/495)
- Debian init.d script swallows init errors [\#459](https://github.com/riemann/riemann/issues/459)

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
- Add ChannelOption/SO_BACKLOG to TCP server [\#706](https://github.com/riemann/riemann/pull/706) ([pradeepchhetri](https://github.com/pradeepchhetri))
- Clarify GC behavior of \(by\) streams [\#704](https://github.com/riemann/riemann/pull/704) ([blalor](https://github.com/blalor))
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
- support for influxdb subscriptions [\#635](https://github.com/riemann/riemann/issues/635)
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
- \(depend plugin artifact version options\) seems broken whith {offline? false} [\#547](https://github.com/riemann/riemann/issues/547)
- Sending reserved protocol-buffer fields results in weird behaviour [\#545](https://github.com/riemann/riemann/issues/545)
- Hyperloglog Stream [\#537](https://github.com/riemann/riemann/issues/537)
- Enhancement: RPM init scripts return proper errors when startup fails \(but not reload\) [\#524](https://github.com/riemann/riemann/issues/524)
- links to source in api docs broken [\#519](https://github.com/riemann/riemann/issues/519)
- rollup on slack does not show event details [\#510](https://github.com/riemann/riemann/issues/510)
- riemann-health: NoMethodError undefined method `map' for \#\<Riemann::Event:0x000000024c9e90\> [\#506](https://github.com/riemann/riemann/issues/506)
- java.util.concurrent.ExecutionException: java.lang.UnsatisfiedLinkError: /tmp/libnetty-transport-native-epoll6734735497094555703.so: /tmp/libnetty-transport-native-epoll6734735497094555703.so: wrong ELF class: ELFCLASS64 \(Possible cause: architecture word width mismatch\) [\#505](https://github.com/riemann/riemann/issues/505)
- UDP issue in 0.2.7 [\#488](https://github.com/riemann/riemann/issues/488)
- Init script returns OK on reload with errors in the configuration [\#467](https://github.com/riemann/riemann/issues/467)
- Some keys don't work when accessing the dashboard [\#460](https://github.com/riemann/riemann/issues/460)
- RPM package defaults to specify ENV vars to daemonize? [\#457](https://github.com/riemann/riemann/issues/457)
- Logstash should not filter events based on their metric [\#425](https://github.com/riemann/riemann/issues/425)
- deftest does not expire events [\#415](https://github.com/riemann/riemann/issues/415)
- If a TTL isn't specified on an event before it is processed by rate it will never expire [\#390](https://github.com/riemann/riemann/issues/390)
- streams/top may not be expiring events to the demote stream [\#389](https://github.com/riemann/riemann/issues/389)
- every! with negative delay drops first call to f [\#368](https://github.com/riemann/riemann/issues/368)
- folds/mode [\#363](https://github.com/riemann/riemann/issues/363)
- Integrating a plugin [\#355](https://github.com/riemann/riemann/issues/355)
- Discussion: custom attributes support in query language [\#300](https://github.com/riemann/riemann/issues/300)
- committed .lein-classpath file causes lein to fail due to classpath problems. [\#246](https://github.com/riemann/riemann/issues/246)
- Read through custom attributes, confirm tests exist and work, etc. [\#129](https://github.com/riemann/riemann/issues/129)
- Provide optional "sort" and "limit" parameters to query [\#125](https://github.com/riemann/riemann/issues/125)

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
- Add ChannelOption/RCVBUF_ALLOCATOR to UDP server [\#603](https://github.com/riemann/riemann/pull/603) ([MichaelDoyle](https://github.com/MichaelDoyle))
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
- Start up failure on OpenJDK 1.6.0_34 [\#535](https://github.com/riemann/riemann/issues/535)
- Riemann API docs site shows 0.2.7 instead of 0.2.8 [\#518](https://github.com/riemann/riemann/issues/518)
- InfluxDB should be async [\#411](https://github.com/riemann/riemann/issues/411)
- \*config-file\* does not get bound to an included file when processing a directory [\#403](https://github.com/riemann/riemann/issues/403)
- Too many WebSocket connections causes performance problem [\#310](https://github.com/riemann/riemann/issues/310)

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
- Problem querying index: values shows up only after reloading config file \(sighup\) [\#513](https://github.com/riemann/riemann/issues/513)
- RPM package depends on the daemonize package which is not available in CentOS 7 / RHEL 7 and EPEL 7 [\#504](https://github.com/riemann/riemann/issues/504)
- doc improvement suggestion: which is the authoritative protocol buffer definition? [\#503](https://github.com/riemann/riemann/issues/503)
- fixed time window null point exceptions [\#498](https://github.com/riemann/riemann/issues/498)
- Add EXTRA_CLASSPATH to tarball script [\#461](https://github.com/riemann/riemann/issues/461)
- RPM package does not work on CentOS 7 [\#453](https://github.com/riemann/riemann/issues/453)
- where\* evaluates predicate expression every time [\#381](https://github.com/riemann/riemann/issues/381)
- deprecation notices should only print once [\#362](https://github.com/riemann/riemann/issues/362)

**Merged pull requests:**

- Put EXTRA_CLASSPATH after riemann.jar [\#520](https://github.com/riemann/riemann/pull/520) ([md5](https://github.com/md5))
- Specify seconds resolution in output to InfluxDB [\#517](https://github.com/riemann/riemann/pull/517) ([fhalim](https://github.com/fhalim))
- Retain time specified in event [\#516](https://github.com/riemann/riemann/pull/516) ([fhalim](https://github.com/fhalim))
- Fix :ttl conversion to LIFETIME in xymon forward [\#512](https://github.com/riemann/riemann/pull/512) ([mirwan](https://github.com/mirwan))
- An example of what I'd like to achieve with extra properties from katja [\#511](https://github.com/riemann/riemann/pull/511) ([robashton](https://github.com/robashton))
- Enable native epoll on Linux/amd64 only [\#508](https://github.com/riemann/riemann/pull/508) ([msantos](https://github.com/msantos))
- cache maven downloads between builds [\#501](https://github.com/riemann/riemann/pull/501) ([tcrayford](https://github.com/tcrayford))
- Implemented Boundary integration. [\#500](https://github.com/riemann/riemann/pull/500) ([blkt](https://github.com/blkt))
- Update clj-http [\#499](https://github.com/riemann/riemann/pull/499) ([AeroNotix](https://github.com/AeroNotix))
- Fix deb and rpm ownerships [\#497](https://github.com/riemann/riemann/pull/497) ([mfournier](https://github.com/mfournier))
- Add streams/fixed-wall-clock-time-window [\#496](https://github.com/riemann/riemann/pull/496) ([juise](https://github.com/juise))
- Emit a deprecation warning only once [\#494](https://github.com/riemann/riemann/pull/494) ([AeroNotix](https://github.com/AeroNotix))
- restore one netty metric [\#492](https://github.com/riemann/riemann/pull/492) ([sgran](https://github.com/sgran))
- Fix init script output on RedHat-based distros [\#491](https://github.com/riemann/riemann/pull/491) ([pharaujo](https://github.com/pharaujo))
- upgrade to high-scale-lib v1.0.6 in Maven Central [\#490](https://github.com/riemann/riemann/pull/490) ([bfritz](https://github.com/bfritz))
- update version for clj-time [\#489](https://github.com/riemann/riemann/pull/489) ([sgran](https://github.com/sgran))
- Include original event when capturing call rescue exception [\#485](https://github.com/riemann/riemann/pull/485) ([tcrayford](https://github.com/tcrayford))

## [0.2.8](https://github.com/riemann/riemann/tree/0.2.8) (2015-01-09)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.7...0.2.8)

**Closed issues:**

- Question on configuration - pipeline event [\#486](https://github.com/riemann/riemann/issues/486)
- Prevent TLS renegotiation attacks [\#475](https://github.com/riemann/riemann/issues/475)

**Merged pull requests:**

- Fix Netty native transport issue [\#487](https://github.com/riemann/riemann/pull/487) ([dbriones](https://github.com/dbriones))

## [0.2.7](https://github.com/riemann/riemann/tree/0.2.7) (2015-01-06)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.6...0.2.7)

**Closed issues:**

- Queries always return \[\] when tcp-server changes host [\#473](https://github.com/riemann/riemann/issues/473)
- Docu Nitpicks [\#464](https://github.com/riemann/riemann/issues/464)
- Debian packaging [\#455](https://github.com/riemann/riemann/issues/455)
- weird behaviour of coalesce [\#441](https://github.com/riemann/riemann/issues/441)
- HTTPS Download Link [\#438](https://github.com/riemann/riemann/issues/438)
- riemann stable api bug [\#418](https://github.com/riemann/riemann/issues/418)
- Is it suitable to use functions in time.clj in a transaction? [\#416](https://github.com/riemann/riemann/issues/416)
- Instrumentation seemingly performs unnecessary work if not enabled [\#413](https://github.com/riemann/riemann/issues/413)
- java.lang.ClassCastException: lamina.core.result.ResultChannel cannot be cast to lamina.core.channel.IChannel : On Startup [\#412](https://github.com/riemann/riemann/issues/412)
- sdo special cases for no args and one arg. [\#398](https://github.com/riemann/riemann/issues/398)
- Ways to Test configs [\#205](https://github.com/riemann/riemann/issues/205)

**Merged pull requests:**

- Stackdriver plugin bug-fixes [\#484](https://github.com/riemann/riemann/pull/484) ([k7d](https://github.com/k7d))
- Remove extra travis config [\#483](https://github.com/riemann/riemann/pull/483) ([budnik](https://github.com/budnik))
- Make with handle lists of events [\#478](https://github.com/riemann/riemann/pull/478) ([tcrayford](https://github.com/tcrayford))
- Add OpenTSDB server support [\#470](https://github.com/riemann/riemann/pull/470) ([pharaujo](https://github.com/pharaujo))
- streams/sdo: Special case for 0 and 1 arg versions [\#469](https://github.com/riemann/riemann/pull/469) ([algernon](https://github.com/algernon))
- Add twilio forwarder [\#466](https://github.com/riemann/riemann/pull/466) ([johndagostino](https://github.com/johndagostino))
- Fix example and add a hint [\#465](https://github.com/riemann/riemann/pull/465) ([iggy](https://github.com/iggy))
- Make opsgenie more readable [\#463](https://github.com/riemann/riemann/pull/463) ([avsej](https://github.com/avsej))
- Fix missing bracket in slack documentation [\#462](https://github.com/riemann/riemann/pull/462) ([calston](https://github.com/calston))
- Allow using incoming webhooks endpoints in the Slack plugin [\#458](https://github.com/riemann/riemann/pull/458) ([AeroNotix](https://github.com/AeroNotix))
- Add mailgun output plugin to riemann.config ns [\#454](https://github.com/riemann/riemann/pull/454) ([dlobue](https://github.com/dlobue))
- Added Datadog plugin [\#451](https://github.com/riemann/riemann/pull/451) ([pradeepchhetri](https://github.com/pradeepchhetri))
- Added EC2 Cloudwatch plugin [\#450](https://github.com/riemann/riemann/pull/450) ([pradeepchhetri](https://github.com/pradeepchhetri))
- Add riemann.opsgenie [\#449](https://github.com/riemann/riemann/pull/449) ([glebpom](https://github.com/glebpom))
- Fix mailgun output unit test [\#448](https://github.com/riemann/riemann/pull/448) ([dlobue](https://github.com/dlobue))
- Add mailgun output [\#447](https://github.com/riemann/riemann/pull/447) ([dlobue](https://github.com/dlobue))
- Add test cases for reload bug in riemann.config/index [\#446](https://github.com/riemann/riemann/pull/446) ([dlobue](https://github.com/dlobue))
- Add riemann.xymon feature and corresponding tests [\#445](https://github.com/riemann/riemann/pull/445) ([mirwan](https://github.com/mirwan))
- Added event-forwarder for Blueflood metric processing system [\#444](https://github.com/riemann/riemann/pull/444) ([GeorgeJahad](https://github.com/GeorgeJahad))
- Fixed the doc of defaults in threadpool-service [\#442](https://github.com/riemann/riemann/pull/442) ([GeorgeJahad](https://github.com/GeorgeJahad))
- Remove unneeded info logging [\#440](https://github.com/riemann/riemann/pull/440) ([strika](https://github.com/strika))
- Logentries integration [\#439](https://github.com/riemann/riemann/pull/439) ([strika](https://github.com/strika))
- Support external log4j configuration [\#434](https://github.com/riemann/riemann/pull/434) ([dlobue](https://github.com/dlobue))
- Upgrade to netty 4.0.21 [\#432](https://github.com/riemann/riemann/pull/432) ([udoprog](https://github.com/udoprog))
- Fix: Cannot use shinken adapter in config [\#431](https://github.com/riemann/riemann/pull/431) ([aviau](https://github.com/aviau))
- Add support for passing a map to sorted-sample [\#430](https://github.com/riemann/riemann/pull/430) ([SegFaultAX](https://github.com/SegFaultAX))
- Add support for the `test` command to the startup scripts [\#428](https://github.com/riemann/riemann/pull/428) ([dbriones](https://github.com/dbriones))
- Allow dynamically loaded namespace and provide \(depend\) [\#427](https://github.com/riemann/riemann/pull/427) ([pyr](https://github.com/pyr))
- Send events to Logstash, even if they don't have metrics [\#426](https://github.com/riemann/riemann/pull/426) ([blalor](https://github.com/blalor))
- kill leftover println [\#421](https://github.com/riemann/riemann/pull/421) ([pyr](https://github.com/pyr))
- Provide a websocket transport separated from aleph [\#420](https://github.com/riemann/riemann/pull/420) ([pyr](https://github.com/pyr))
- Debian packaging improvements [\#419](https://github.com/riemann/riemann/pull/419) ([benley](https://github.com/benley))

## [0.2.6](https://github.com/riemann/riemann/tree/0.2.6) (2014-07-18)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.5...0.2.6)

**Closed issues:**

- `where` rewriting of `tagged` requires `riemann.streams` to be required [\#414](https://github.com/riemann/riemann/issues/414)
- Require streams, test/tap, test/io in test/tests ns. [\#409](https://github.com/riemann/riemann/issues/409)
- Index not being preserved across reloads [\#405](https://github.com/riemann/riemann/issues/405)
- Unable to resolve symbol: log-error in this context [\#402](https://github.com/riemann/riemann/issues/402)
- What may be causing "No channels available"? [\#396](https://github.com/riemann/riemann/issues/396)
- iso8601-\>unix returning seconds instead of milliseconds on OS X [\#392](https://github.com/riemann/riemann/issues/392)
- Event Stream Processor [\#379](https://github.com/riemann/riemann/issues/379)
- Email's API to provide function as subject can't be serialized using Cheshire [\#377](https://github.com/riemann/riemann/issues/377)
- Search in index is too slow [\#367](https://github.com/riemann/riemann/issues/367)

**Merged pull requests:**

- Add riemann.shinken an adapter for shinken. [\#417](https://github.com/riemann/riemann/pull/417) ([aviau](https://github.com/aviau))
- Added stackdriver plugin. [\#410](https://github.com/riemann/riemann/pull/410) ([pradeepchhetri](https://github.com/pradeepchhetri))
- Don't use default port names for transport test [\#408](https://github.com/riemann/riemann/pull/408) ([eric](https://github.com/eric))
- Ensure that indexes are retained across reloads [\#407](https://github.com/riemann/riemann/pull/407) ([eric](https://github.com/eric))
- Extend changed to optionally capture previous value [\#404](https://github.com/riemann/riemann/pull/404) ([blalor](https://github.com/blalor))
- Changed to give the influxdb series name as the first word of the servic... [\#401](https://github.com/riemann/riemann/pull/401) ([pradeepchhetri](https://github.com/pradeepchhetri))
- When recursively loading configuration files, only include those files with a `.clj` or `.config` extension [\#399](https://github.com/riemann/riemann/pull/399) ([dbriones](https://github.com/dbriones))
- Εxtended formatter for slack using message attachments [\#397](https://github.com/riemann/riemann/pull/397) ([alkar](https://github.com/alkar))
- Fixes \#392, update tests so that Unix time is calculated in seconds [\#394](https://github.com/riemann/riemann/pull/394) ([jwinter](https://github.com/jwinter))
- Option to supply a custom message formatter for Slack integration [\#393](https://github.com/riemann/riemann/pull/393) ([dm3](https://github.com/dm3))
- Use a set to filter services. [\#385](https://github.com/riemann/riemann/pull/385) ([ulises](https://github.com/ulises))
- Document the use of sets as predicates to \(where...\) [\#382](https://github.com/riemann/riemann/pull/382) ([ulises](https://github.com/ulises))
- Added map matching to common Match protocl [\#378](https://github.com/riemann/riemann/pull/378) ([kurtstoll](https://github.com/kurtstoll))
- Add ewma stream [\#346](https://github.com/riemann/riemann/pull/346) ([DanielleSucher](https://github.com/DanielleSucher))

## [0.2.5](https://github.com/riemann/riemann/tree/0.2.5) (2014-04-30)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.4...0.2.5)

**Closed issues:**

- folds/count should use riemann.common/event [\#365](https://github.com/riemann/riemann/issues/365)
- deprecation notices should include source file and line [\#361](https://github.com/riemann/riemann/issues/361)
- folds/maximum and folds/minimum can't deal with empty lists. [\#357](https://github.com/riemann/riemann/issues/357)
- set time automatically in dynamically created events [\#351](https://github.com/riemann/riemann/issues/351)
- Expired events stop working when decode-graphite-line return nil or malformed event [\#338](https://github.com/riemann/riemann/issues/338)
- It's not possible to disable logging to console [\#336](https://github.com/riemann/riemann/issues/336)
- Graphite metrics separated by other whitespace [\#330](https://github.com/riemann/riemann/issues/330)
- Programmatic way to shutdown server [\#327](https://github.com/riemann/riemann/issues/327)
- HipChat output [\#322](https://github.com/riemann/riemann/issues/322)
- "index" pubsub regression [\#318](https://github.com/riemann/riemann/issues/318)
- sending events to logstash fails unless block-start is set to true [\#317](https://github.com/riemann/riemann/issues/317)
- Small typos [\#316](https://github.com/riemann/riemann/issues/316)
- Config files for deb and tar don't work [\#315](https://github.com/riemann/riemann/issues/315)
- Performance Improvements for coalesce [\#314](https://github.com/riemann/riemann/issues/314)
- Exception when enqeuing instrumentation results crashes the instrumentation reporting [\#313](https://github.com/riemann/riemann/issues/313)
- riemann memory consumption grows in time [\#312](https://github.com/riemann/riemann/issues/312)
- Change index to implement IFn; get rid of update-index? [\#301](https://github.com/riemann/riemann/issues/301)
- HTTP server: NullPointerException in Lamina. [\#282](https://github.com/riemann/riemann/issues/282)
- Nag stream [\#263](https://github.com/riemann/riemann/issues/263)
- Incomplete implementation on streams/fixed-time-window [\#259](https://github.com/riemann/riemann/issues/259)
- Websockets interface security [\#249](https://github.com/riemann/riemann/issues/249)
- webhook support [\#243](https://github.com/riemann/riemann/issues/243)
- Exception due to local hostname is unresolvable [\#233](https://github.com/riemann/riemann/issues/233)
- Explain custom attributes on concepts page [\#232](https://github.com/riemann/riemann/issues/232)
- riemann fails to start if the graphite server is down [\#224](https://github.com/riemann/riemann/issues/224)
- Pagerduty adapter does not behave right with a vector of events \(from rollup\) [\#177](https://github.com/riemann/riemann/issues/177)
- folds/count [\#165](https://github.com/riemann/riemann/issues/165)
- When netty fails to create a thread, it hangs the connection [\#149](https://github.com/riemann/riemann/issues/149)
- Provide a way to use regular expressions in queries [\#124](https://github.com/riemann/riemann/issues/124)
- Document the relationship between clients, dashboard, and ws connections. [\#106](https://github.com/riemann/riemann/issues/106)
- Replace riemann-tools dep on yajl with something that works on jruby [\#95](https://github.com/riemann/riemann/issues/95)

**Merged pull requests:**

- More relaxed decoding of graphite messages [\#375](https://github.com/riemann/riemann/pull/375) ([lotia](https://github.com/lotia))
- Add source file and line to deprecation notices [\#371](https://github.com/riemann/riemann/pull/371) ([DanielleSucher](https://github.com/DanielleSucher))
- Use riemann.common/event to add :time in folds/count [\#370](https://github.com/riemann/riemann/pull/370) ([DanielleSucher](https://github.com/DanielleSucher))
- Add protocol option to documentation. [\#369](https://github.com/riemann/riemann/pull/369) ([ProTip](https://github.com/ProTip))
- Added InfluxDB plugin [\#366](https://github.com/riemann/riemann/pull/366) ([pradeepchhetri](https://github.com/pradeepchhetri))
- little tidying for hipchat [\#364](https://github.com/riemann/riemann/pull/364) ([stuarth](https://github.com/stuarth))
- update dependencies, with report from lein-ancient [\#359](https://github.com/riemann/riemann/pull/359) ([pyr](https://github.com/pyr))
- Fix two bugs [\#358](https://github.com/riemann/riemann/pull/358) ([pyr](https://github.com/pyr))
- Add slack notifications [\#354](https://github.com/riemann/riemann/pull/354) ([gsandie](https://github.com/gsandie))
- Expose `event` in config and catch events missing time [\#353](https://github.com/riemann/riemann/pull/353) ([pyr](https://github.com/pyr))
- Fix indentation in index [\#352](https://github.com/riemann/riemann/pull/352) ([eric](https://github.com/eric))
- Coalesce docstring [\#349](https://github.com/riemann/riemann/pull/349) ([mfournier](https://github.com/mfournier))
- Optimize query for host and service [\#345](https://github.com/riemann/riemann/pull/345) ([eric](https://github.com/eric))
- add support for size based log rotation [\#344](https://github.com/riemann/riemann/pull/344) ([jespada](https://github.com/jespada))
- folds/mean: Fix divide by zero on only events with nil metrics. [\#343](https://github.com/riemann/riemann/pull/343) ([udoprog](https://github.com/udoprog))
- Feature atomization [\#342](https://github.com/riemann/riemann/pull/342) ([pyr](https://github.com/pyr))
- Fixed messages not being read in logstash. [\#341](https://github.com/riemann/riemann/pull/341) ([vvision](https://github.com/vvision))
- Added OpenTSDB plugin [\#340](https://github.com/riemann/riemann/pull/340) ([pradeepchhetri](https://github.com/pradeepchhetri))
- Switch more streams from refs to atoms [\#339](https://github.com/riemann/riemann/pull/339) ([pyr](https://github.com/pyr))
- Switch changed to rely on an atom instead of a ref [\#337](https://github.com/riemann/riemann/pull/337) ([pyr](https://github.com/pyr))
- Handle spaces in graphite metric names [\#335](https://github.com/riemann/riemann/pull/335) ([sblask](https://github.com/sblask))
- Experimental support for more scalable coalesce [\#333](https://github.com/riemann/riemann/pull/333) ([dgrnbrg](https://github.com/dgrnbrg))
- Riemann initscript minor improvements [\#332](https://github.com/riemann/riemann/pull/332) ([mfournier](https://github.com/mfournier))
- Add kairosdb support [\#329](https://github.com/riemann/riemann/pull/329) ([baloo](https://github.com/baloo))
- websockets: Minor bug fixes [\#326](https://github.com/riemann/riemann/pull/326) ([udoprog](https://github.com/udoprog))
- make rpm default config same as deb/tar [\#324](https://github.com/riemann/riemann/pull/324) ([mfournier](https://github.com/mfournier))
- Hipchat multi event [\#323](https://github.com/riemann/riemann/pull/323) ([gsandie](https://github.com/gsandie))
- correctly implement IFn for indexes through a wrapper [\#319](https://github.com/riemann/riemann/pull/319) ([pyr](https://github.com/pyr))
- Use sysconfig for RPM bin [\#311](https://github.com/riemann/riemann/pull/311) ([BobbyRyterski](https://github.com/BobbyRyterski))
- top: keep events not values, demotions to top&bottom streams [\#309](https://github.com/riemann/riemann/pull/309) ([pyr](https://github.com/pyr))
- indexing an expired event should result in removal [\#308](https://github.com/riemann/riemann/pull/308) ([pyr](https://github.com/pyr))
- better fix for aphyr/riemann\#233 [\#307](https://github.com/riemann/riemann/pull/307) ([pyr](https://github.com/pyr))
- Do not assume numbers for comparisons in top. [\#306](https://github.com/riemann/riemann/pull/306) ([pyr](https://github.com/pyr))
- provide a default value for local host name lookups [\#304](https://github.com/riemann/riemann/pull/304) ([pyr](https://github.com/pyr))
- proposed fix for \#301 [\#303](https://github.com/riemann/riemann/pull/303) ([pyr](https://github.com/pyr))
- coalesce splits by host as well [\#302](https://github.com/riemann/riemann/pull/302) ([tcrayford](https://github.com/tcrayford))
- riemann.streams: Documentation and whitespace fixes [\#298](https://github.com/riemann/riemann/pull/298) ([udoprog](https://github.com/udoprog))
- riemann.time.controlled: Use of with-redefs to be slightly more forgiving. [\#297](https://github.com/riemann/riemann/pull/297) ([udoprog](https://github.com/udoprog))
- 2 packaging fixes [\#296](https://github.com/riemann/riemann/pull/296) ([mfournier](https://github.com/mfournier))
- More flexible logging configuration, JSONEvent support [\#295](https://github.com/riemann/riemann/pull/295) ([pyr](https://github.com/pyr))
- avoid extra blank lines in logs [\#294](https://github.com/riemann/riemann/pull/294) ([pyr](https://github.com/pyr))
- Give the vm a bit more time, helps openjdk6 not failing [\#293](https://github.com/riemann/riemann/pull/293) ([pyr](https://github.com/pyr))
- Load hipchat into riemann namespace [\#291](https://github.com/riemann/riemann/pull/291) ([gsandie](https://github.com/gsandie))
- Query: Add support for regexp queries with ~= [\#290](https://github.com/riemann/riemann/pull/290) ([algernon](https://github.com/algernon))
- Add riemann.folds/count-unexpired [\#288](https://github.com/riemann/riemann/pull/288) ([DanielleSucher](https://github.com/DanielleSucher))
- Add a persistent connection for librato [\#285](https://github.com/riemann/riemann/pull/285) ([hugoduncan](https://github.com/hugoduncan))

## [0.2.4](https://github.com/riemann/riemann/tree/0.2.4) (2013-11-25)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.3...0.2.4)

**Closed issues:**

- Update Postal [\#278](https://github.com/riemann/riemann/issues/278)
- Quickstart exception due to log path [\#190](https://github.com/riemann/riemann/issues/190)

**Merged pull requests:**

- Add a name attribute to riemann.bin [\#283](https://github.com/riemann/riemann/pull/283) ([kanakb](https://github.com/kanakb))
- Fix path to the clojure client [\#281](https://github.com/riemann/riemann/pull/281) ([richo](https://github.com/richo))
- Begin adding in testing helpers [\#280](https://github.com/riemann/riemann/pull/280) ([AshtonKem](https://github.com/AshtonKem))

## [0.2.3](https://github.com/riemann/riemann/tree/0.2.3) (2013-11-12)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.2...0.2.3)

**Closed issues:**

- is stable broken? [\#271](https://github.com/riemann/riemann/issues/271)
- .deb install's riemann script ignores /etc/default/riemann [\#264](https://github.com/riemann/riemann/issues/264)
- backwards compatibility for puppet template riemann.conf.erb [\#254](https://github.com/riemann/riemann/issues/254)
- Graphite path percentiles function not ideal in certain cases [\#251](https://github.com/riemann/riemann/issues/251)
- Riemann fails to start on Windows [\#244](https://github.com/riemann/riemann/issues/244)
- reload broken? [\#234](https://github.com/riemann/riemann/issues/234)
- Support for Cube \(similar to Graphite\)? [\#231](https://github.com/riemann/riemann/issues/231)
- Reconnect on network problem [\#223](https://github.com/riemann/riemann/issues/223)
- Address reflection warnings [\#206](https://github.com/riemann/riemann/issues/206)
- folds/mean should not throw when receiving a vector of zero events. [\#163](https://github.com/riemann/riemann/issues/163)
- Graphite-server really needs tests! [\#161](https://github.com/riemann/riemann/issues/161)
- Documentation questions [\#146](https://github.com/riemann/riemann/issues/146)
- Mention that 0.0.0.0 binds to all interfaces in howto. [\#141](https://github.com/riemann/riemann/issues/141)

**Merged pull requests:**

- fix typo [\#279](https://github.com/riemann/riemann/pull/279) ([also](https://github.com/also))
- change naming strategy as discussed on irc [\#277](https://github.com/riemann/riemann/pull/277) ([pyr](https://github.com/pyr))
- Implement a plugin system, for easy loading of external namespaces [\#276](https://github.com/riemann/riemann/pull/276) ([pyr](https://github.com/pyr))
- test fold edge cases [\#275](https://github.com/riemann/riemann/pull/275) ([pyr](https://github.com/pyr))
- typo in test-sse [\#274](https://github.com/riemann/riemann/pull/274) ([pyr](https://github.com/pyr))
- add graphite client and server test code [\#273](https://github.com/riemann/riemann/pull/273) ([pyr](https://github.com/pyr))
- Add SSE test. create a pubsub, publish to it and match received. [\#272](https://github.com/riemann/riemann/pull/272) ([pyr](https://github.com/pyr))
- server-sent event transport [\#269](https://github.com/riemann/riemann/pull/269) ([pyr](https://github.com/pyr))
- Fix logstash host [\#268](https://github.com/riemann/riemann/pull/268) ([carlyeks](https://github.com/carlyeks))
- Add support for passive Nagios checks [\#267](https://github.com/riemann/riemann/pull/267) ([bracki](https://github.com/bracki))
- Add logstash client based on existing graphite client [\#266](https://github.com/riemann/riemann/pull/266) ([tjake](https://github.com/tjake))
- Load defaults from /etc/default [\#265](https://github.com/riemann/riemann/pull/265) ([overthink](https://github.com/overthink))
- add missing param stats to graphite-handler [\#262](https://github.com/riemann/riemann/pull/262) ([schnipseljagd](https://github.com/schnipseljagd))
- Fix start-stop-daemon not working on older Debian versions [\#261](https://github.com/riemann/riemann/pull/261) ([bracki](https://github.com/bracki))
- Thank Federico Borgnia [\#260](https://github.com/riemann/riemann/pull/260) ([gsandie](https://github.com/gsandie))
- Added support for HipChat [\#258](https://github.com/riemann/riemann/pull/258) ([neotyk](https://github.com/neotyk))
- Exception handling [\#257](https://github.com/riemann/riemann/pull/257) ([neotyk](https://github.com/neotyk))
- added a standard deviation fold [\#253](https://github.com/riemann/riemann/pull/253) ([chillitom](https://github.com/chillitom))
- Add missing close-paren in streams docstring [\#252](https://github.com/riemann/riemann/pull/252) ([bitprophet](https://github.com/bitprophet))
- Rename riemann.common/attributes -\> custom-attributes [\#247](https://github.com/riemann/riemann/pull/247) ([samn](https://github.com/samn))
- Don't register HUP signal handler on Windows [\#245](https://github.com/riemann/riemann/pull/245) ([chillitom](https://github.com/chillitom))
- Add directoy support to 'include' [\#242](https://github.com/riemann/riemann/pull/242) ([paulgoldbaum](https://github.com/paulgoldbaum))
- fixed a typo in pkg/deb/riemann [\#241](https://github.com/riemann/riemann/pull/241) ([mburger](https://github.com/mburger))
- Exclude path information from md5 checksum file. [\#240](https://github.com/riemann/riemann/pull/240) ([asemt](https://github.com/asemt))
- Include custom attributes in emails & SNS notifications [\#238](https://github.com/riemann/riemann/pull/238) ([samn](https://github.com/samn))
- bumps to clojure 1.5.1 for memory leak patch [\#237](https://github.com/riemann/riemann/pull/237) ([bmabey](https://github.com/bmabey))
- Add support for vector of events in Librato [\#235](https://github.com/riemann/riemann/pull/235) ([mentalblock](https://github.com/mentalblock))

## [0.2.2](https://github.com/riemann/riemann/tree/0.2.2) (2013-06-05)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.1...0.2.2)

**Closed issues:**

- SSL [\#225](https://github.com/riemann/riemann/issues/225)
- suggested jre to run riemann? [\#216](https://github.com/riemann/riemann/issues/216)
- Runtime exception compiling riemann.bin [\#212](https://github.com/riemann/riemann/issues/212)
- Apdex stream [\#202](https://github.com/riemann/riemann/issues/202)
- \(throttle ...\) does not seem to pass expired events [\#178](https://github.com/riemann/riemann/issues/178)
- :keep-keys does not properly update on 'reload' [\#173](https://github.com/riemann/riemann/issues/173)
- riemann website quickstart : no checksum [\#172](https://github.com/riemann/riemann/issues/172)
- Investigate possible lack of error handling in netty pipeline [\#126](https://github.com/riemann/riemann/issues/126)
- streams/rate should stop its internal poller when TTLs go negative [\#117](https://github.com/riemann/riemann/issues/117)

**Merged pull requests:**

- Feature extra classpath [\#230](https://github.com/riemann/riemann/pull/230) ([pyr](https://github.com/pyr))
- allow extra jars on the classpath [\#229](https://github.com/riemann/riemann/pull/229) ([pyr](https://github.com/pyr))
- Support for multiple tags in where test expressions [\#228](https://github.com/riemann/riemann/pull/228) ([samn](https://github.com/samn))
- Allow tagged-any & tagged-all to be used in where exprs [\#227](https://github.com/riemann/riemann/pull/227) ([samn](https://github.com/samn))
- Add campfire output [\#222](https://github.com/riemann/riemann/pull/222) ([gsandie](https://github.com/gsandie))
- scale wraps a common smap idiom [\#221](https://github.com/riemann/riemann/pull/221) ([pyr](https://github.com/pyr))
- Use tagged release nrepl-0.2.2 instead of 0.2.0-RC1 [\#217](https://github.com/riemann/riemann/pull/217) ([dch](https://github.com/dch))
- Add some type hints [\#211](https://github.com/riemann/riemann/pull/211) ([banjiewen](https://github.com/banjiewen))
- Lookup an event directly from the index [\#210](https://github.com/riemann/riemann/pull/210) ([rrees](https://github.com/rrees))
- streams/where\*: should only evaluate children once [\#204](https://github.com/riemann/riemann/pull/204) ([sihil](https://github.com/sihil))
- Add UDP transport to graphite-server [\#201](https://github.com/riemann/riemann/pull/201) ([sihil](https://github.com/sihil))
- Remove openjdk dependency from fatdeb to improve flexibility [\#200](https://github.com/riemann/riemann/pull/200) ([knuckolls](https://github.com/knuckolls))
- AWS SNS notifier that uses the Java SDK directly [\#199](https://github.com/riemann/riemann/pull/199) ([ento](https://github.com/ento))
- Fix count-string-bytes [\#198](https://github.com/riemann/riemann/pull/198) ([richo](https://github.com/richo))

## [0.2.1](https://github.com/riemann/riemann/tree/0.2.1) (2013-04-08)

[Full Changelog](https://github.com/riemann/riemann/compare/0.2.0...0.2.1)

**Closed issues:**

- Riemann interaction with custom service ? [\#197](https://github.com/riemann/riemann/issues/197)
- Debian init script tries to let you start multiple Riemanns [\#181](https://github.com/riemann/riemann/issues/181)
- Remove paths from md5 checksum files [\#167](https://github.com/riemann/riemann/issues/167)

**Merged pull requests:**

- Bump Aleph so Lamina can be unpinned. [\#195](https://github.com/riemann/riemann/pull/195) ([mblair](https://github.com/mblair))
- fixes documentation errors in folds.clj [\#192](https://github.com/riemann/riemann/pull/192) ([spazm](https://github.com/spazm))
- Add tests for streams/runs [\#191](https://github.com/riemann/riemann/pull/191) ([mblair](https://github.com/mblair))
- adds a teaser intro to the README [\#189](https://github.com/riemann/riemann/pull/189) ([spazm](https://github.com/spazm))
- Fix docstring consistency issue [\#187](https://github.com/riemann/riemann/pull/187) ([bmhatfield](https://github.com/bmhatfield))
- Run of same [\#185](https://github.com/riemann/riemann/pull/185) ([bmhatfield](https://github.com/bmhatfield))
- Adds a CONTRIBUTING file [\#184](https://github.com/riemann/riemann/pull/184) ([spazm](https://github.com/spazm))
- Clear up signal name. [\#183](https://github.com/riemann/riemann/pull/183) ([mblair](https://github.com/mblair))
- Prevent the init script from trying to start multiple Riemanns. [\#182](https://github.com/riemann/riemann/pull/182) ([mblair](https://github.com/mblair))
- Use a LinkedBlockingQueue in riemann.pool. [\#180](https://github.com/riemann/riemann/pull/180) ([banjiewen](https://github.com/banjiewen))
- Ensure that \(:streams core\) is always a vec. [\#179](https://github.com/riemann/riemann/pull/179) ([banjiewen](https://github.com/banjiewen))
- Pin Lamina to fix the build. [\#176](https://github.com/riemann/riemann/pull/176) ([mblair](https://github.com/mblair))
- Add Java to deb dependencies. [\#175](https://github.com/riemann/riemann/pull/175) ([mblair](https://github.com/mblair))
- Add aws sns notifier [\#171](https://github.com/riemann/riemann/pull/171) ([ento](https://github.com/ento))

## [0.2.0](https://github.com/riemann/riemann/tree/0.2.0) (2013-03-17)

[Full Changelog](https://github.com/riemann/riemann/compare/0.1.5...0.2.0)

**Closed issues:**

- RFC: More structured data in riemann events. [\#168](https://github.com/riemann/riemann/issues/168)
- when TCP connections are dropped the log file is noisy [\#162](https://github.com/riemann/riemann/issues/162)
- If you kill -hup riemann too many times in quick succession, it drops TCP conns. [\#160](https://github.com/riemann/riemann/issues/160)
- Riemann hangs if endpoint for `forward` is not available [\#159](https://github.com/riemann/riemann/issues/159)
- ddt should run in real time. [\#158](https://github.com/riemann/riemann/issues/158)
- NullPointer in moving-time-window [\#157](https://github.com/riemann/riemann/issues/157)
- Remove ws-server :host for now [\#156](https://github.com/riemann/riemann/issues/156)
- \(where\) doesn't work with some types of values [\#155](https://github.com/riemann/riemann/issues/155)
- streams/project: coalesce but for vectors of predicates [\#154](https://github.com/riemann/riemann/issues/154)
- Split and friends evaluate their forms more than once. [\#153](https://github.com/riemann/riemann/issues/153)
- Transient + threading issue [\#147](https://github.com/riemann/riemann/issues/147)
- lein build failure [\#145](https://github.com/riemann/riemann/issues/145)
- Java netty client [\#143](https://github.com/riemann/riemann/issues/143)
- Thread/queue wrapper for java client. [\#139](https://github.com/riemann/riemann/issues/139)
- Preserve index state across reload. [\#138](https://github.com/riemann/riemann/issues/138)
- Experiment with worker threads for netty. [\#137](https://github.com/riemann/riemann/issues/137)
- streams/tagged-all and streams/tagged-any should return whether they matched. [\#135](https://github.com/riemann/riemann/issues/135)
- streams/where should accept zero branches, and return whether the event matched. [\#134](https://github.com/riemann/riemann/issues/134)
- Rename streams/stream to streams/sdo [\#133](https://github.com/riemann/riemann/issues/133)
- streams/counter should reset to the reset event's metric [\#132](https://github.com/riemann/riemann/issues/132)
- Write an explicit "putting riemann into production" howto guide. [\#131](https://github.com/riemann/riemann/issues/131)
- streams/tag [\#130](https://github.com/riemann/riemann/issues/130)
- riemann.email should preserve metadata [\#128](https://github.com/riemann/riemann/issues/128)
- since 0.1.5 riemann does not accept a full path to the config file, only relative [\#127](https://github.com/riemann/riemann/issues/127)
- \(config/include\) should be relative to the currently included file. [\#123](https://github.com/riemann/riemann/issues/123)
- Coalesce should handle expiry. [\#122](https://github.com/riemann/riemann/issues/122)
- Include riemann.time in config. [\#120](https://github.com/riemann/riemann/issues/120)
- Top-K streams [\#119](https://github.com/riemann/riemann/issues/119)
- Deprecate update-index in favor of index implementing IFn [\#116](https://github.com/riemann/riemann/issues/116)
- Add a deprecation macro. [\#115](https://github.com/riemann/riemann/issues/115)
- Line numbers for riemann config exceptions [\#114](https://github.com/riemann/riemann/issues/114)
- Roll up expensive grid rerenders. [\#113](https://github.com/riemann/riemann/issues/113)
- Fix dash rendering on FF. [\#112](https://github.com/riemann/riemann/issues/112)
- Dash should toast on save/load. [\#111](https://github.com/riemann/riemann/issues/111)
- Rate should emit zeroes when no events arrive. [\#109](https://github.com/riemann/riemann/issues/109)
- Make riemann.email/body- customizable [\#108](https://github.com/riemann/riemann/issues/108)
- Running without \(logging/init\) might cause weird errors [\#105](https://github.com/riemann/riemann/issues/105)
- Bring back "hyperspace core online" messages. [\#104](https://github.com/riemann/riemann/issues/104)
- running with no config == log4j error? [\#103](https://github.com/riemann/riemann/issues/103)
- Dash reconnects [\#93](https://github.com/riemann/riemann/issues/93)
- Shrink dash density [\#92](https://github.com/riemann/riemann/issues/92)
- Switch dash back to white theme [\#91](https://github.com/riemann/riemann/issues/91)
- Finish next-gen dash [\#90](https://github.com/riemann/riemann/issues/90)
- Continuous threadpool leak from tcp/udp servers [\#89](https://github.com/riemann/riemann/issues/89)
- Make sure pubsub reloads correctly [\#88](https://github.com/riemann/riemann/issues/88)
- core :services [\#87](https://github.com/riemann/riemann/issues/87)
- FAIL in \(rate-fast\) \(streams.clj:613\) [\#86](https://github.com/riemann/riemann/issues/86)
- FAIL in \(expires\) \(core.clj:115\) [\#83](https://github.com/riemann/riemann/issues/83)
- Fix threadpool leak on reload [\#79](https://github.com/riemann/riemann/issues/79)
- streams/sdo [\#78](https://github.com/riemann/riemann/issues/78)
- streams/split, streams/splitp [\#77](https://github.com/riemann/riemann/issues/77)
- Riemann 0.1.3, java 1.6 & i686 [\#76](https://github.com/riemann/riemann/issues/76)
- Riemann won't start with Oracle JDK 1.6.27 [\#74](https://github.com/riemann/riemann/issues/74)
- Riemann won't start with Icedtea 1.7.0_09 [\#73](https://github.com/riemann/riemann/issues/73)
- 2 cases of either invalid or no JSON produced off events [\#72](https://github.com/riemann/riemann/issues/72)
- int64 metrics [\#69](https://github.com/riemann/riemann/issues/69)
- FAIL in \(rate-fast\) \(streams.clj:523\) [\#49](https://github.com/riemann/riemann/issues/49)
- Add slides and presentations to Riemann's site [\#45](https://github.com/riemann/riemann/issues/45)
- Add a cookbook to the Riemann site [\#44](https://github.com/riemann/riemann/issues/44)
- Integrate nrepl for production debugging [\#36](https://github.com/riemann/riemann/issues/36)
- rollup stops when an expired event arrives [\#19](https://github.com/riemann/riemann/issues/19)
- Percentiles in Graphite assume two digits of precision [\#16](https://github.com/riemann/riemann/issues/16)

**Merged pull requests:**

- Tell Emacs that riemann configs are Clojure code. [\#166](https://github.com/riemann/riemann/pull/166) ([mblair](https://github.com/mblair))
- Fix type error in graphite server. [\#164](https://github.com/riemann/riemann/pull/164) ([PeterScott](https://github.com/PeterScott))
- Very small documentation fix [\#151](https://github.com/riemann/riemann/pull/151) ([fcuny](https://github.com/fcuny))
- repl/start-server! should take a hash-map instead of a vector [\#144](https://github.com/riemann/riemann/pull/144) ([PeterScott](https://github.com/PeterScott))
- Update netty to 3.6.1, fixes leaking thread pools [\#140](https://github.com/riemann/riemann/pull/140) ([gmanika](https://github.com/gmanika))
- Fix graphite-server method name bug [\#107](https://github.com/riemann/riemann/pull/107) ([PeterScott](https://github.com/PeterScott))
- Add delete-by stream primitive and test case [\#102](https://github.com/riemann/riemann/pull/102) ([lwf](https://github.com/lwf))
- Graphite client improvements [\#101](https://github.com/riemann/riemann/pull/101) ([lwf](https://github.com/lwf))
- Catch exceptions when processing expired events [\#100](https://github.com/riemann/riemann/pull/100) ([lwf](https://github.com/lwf))
- Do not try to encode back packets in protobuf when talking graphite [\#97](https://github.com/riemann/riemann/pull/97) ([pyr](https://github.com/pyr))
- produce unique names for snapshot debs [\#96](https://github.com/riemann/riemann/pull/96) ([pyr](https://github.com/pyr))
- Add split\* and split, as described in aphyr/riemann/\#77 [\#85](https://github.com/riemann/riemann/pull/85) ([pyr](https://github.com/pyr))
- silence closed channel exceptions [\#84](https://github.com/riemann/riemann/pull/84) ([pyr](https://github.com/pyr))
- first phase of refactor to allow pluggable transports [\#82](https://github.com/riemann/riemann/pull/82) ([pyr](https://github.com/pyr))
- Proposed implementation for aphyr/riemann/\#78 [\#80](https://github.com/riemann/riemann/pull/80) ([pyr](https://github.com/pyr))

## [0.1.5](https://github.com/riemann/riemann/tree/0.1.5) (2012-12-04)

[Full Changelog](https://github.com/riemann/riemann/compare/0.1.4...0.1.5)

## [0.1.4](https://github.com/riemann/riemann/tree/0.1.4) (2012-12-04)

[Full Changelog](https://github.com/riemann/riemann/compare/0.1.3...0.1.4)

**Closed issues:**

- Put riemann-java-client on clojars. [\#53](https://github.com/riemann/riemann/issues/53)
- Write a proper riemann.config for the packages [\#43](https://github.com/riemann/riemann/issues/43)

**Merged pull requests:**

- graphite was forgotten in the move to atoms :-\( [\#70](https://github.com/riemann/riemann/pull/70) ([pyr](https://github.com/pyr))

## [0.1.3](https://github.com/riemann/riemann/tree/0.1.3) (2012-11-13)

[Full Changelog](https://github.com/riemann/riemann/compare/0.1.2...0.1.3)

**Closed issues:**

- Clarify relationship between where-event, match, and where. [\#52](https://github.com/riemann/riemann/issues/52)
- Generated .deb file is not indexable by apt-ftparchive & friends [\#47](https://github.com/riemann/riemann/issues/47)
- Graphite reconnection fails [\#46](https://github.com/riemann/riemann/issues/46)
- Investigate a possible bug in \(where \(tagged "foo"\)\) [\#42](https://github.com/riemann/riemann/issues/42)
- Add docstring for streams/tagged [\#41](https://github.com/riemann/riemann/issues/41)
- MD5 checksum files on downloads [\#40](https://github.com/riemann/riemann/issues/40)
- Replace protobufs parser [\#39](https://github.com/riemann/riemann/issues/39)
- \(where \(service x\)\) should evaluate x. [\#38](https://github.com/riemann/riemann/issues/38)
- Librato metrics adapter [\#37](https://github.com/riemann/riemann/issues/37)
- Get a Yourkit license [\#35](https://github.com/riemann/riemann/issues/35)
- \(where ... \(else ...\)\) [\#34](https://github.com/riemann/riemann/issues/34)
- multimethods for boolean dispatch [\#13](https://github.com/riemann/riemann/issues/13)
- Deb files should be owned by root. [\#67](https://github.com/riemann/riemann/issues/67)
- Sane default config. [\#66](https://github.com/riemann/riemann/issues/66)
- Verify deb [\#65](https://github.com/riemann/riemann/issues/65)
- Verify tarball works [\#64](https://github.com/riemann/riemann/issues/64)
- Write package task [\#63](https://github.com/riemann/riemann/issues/63)
- Write tarball task [\#62](https://github.com/riemann/riemann/issues/62)
- Moar GC opts for production bin scripts. [\#61](https://github.com/riemann/riemann/issues/61)
- Switch servers from localhost to 127.0.0.1 [\#59](https://github.com/riemann/riemann/issues/59)
- Release 0.1.3 [\#58](https://github.com/riemann/riemann/issues/58)
- Write release notes [\#57](https://github.com/riemann/riemann/issues/57)
- Write a proper debian package script. [\#54](https://github.com/riemann/riemann/issues/54)

**Merged pull requests:**

- \* this should exec, rather than launch a subprocess. this way it works n... [\#60](https://github.com/riemann/riemann/pull/60) ([jib](https://github.com/jib))
- \* mention how to do it on windows as well [\#50](https://github.com/riemann/riemann/pull/50) ([jib](https://github.com/jib))
- \* document how to log to stdout [\#48](https://github.com/riemann/riemann/pull/48) ([jib](https://github.com/jib))
- add a bench-only test for indexing speed. [\#33](https://github.com/riemann/riemann/pull/33) ([pyr](https://github.com/pyr))
- Parameterize message framing pipeline. [\#32](https://github.com/riemann/riemann/pull/32) ([banjiewen](https://github.com/banjiewen))
- Fix graphite-server call [\#31](https://github.com/riemann/riemann/pull/31) ([pyr](https://github.com/pyr))
- Add travis configuration [\#30](https://github.com/riemann/riemann/pull/30) ([pyr](https://github.com/pyr))
- Change streams/moving-event-window to use take-last instead of subvec. [\#28](https://github.com/riemann/riemann/pull/28) ([banjiewen](https://github.com/banjiewen))
- Feature small cleanups [\#27](https://github.com/riemann/riemann/pull/27) ([pyr](https://github.com/pyr))
- split where into where and where-event, avoiding symbol touching [\#26](https://github.com/riemann/riemann/pull/26) ([pyr](https://github.com/pyr))
- Switch pubsub registry to an atom. [\#25](https://github.com/riemann/riemann/pull/25) ([pyr](https://github.com/pyr))
- Use an atom pointing to a simple list to store events in coalesce. [\#24](https://github.com/riemann/riemann/pull/24) ([pyr](https://github.com/pyr))
- Add a graphite listener. [\#23](https://github.com/riemann/riemann/pull/23) ([pyr](https://github.com/pyr))
- Use a pool of sockets for riemann.graphite. [\#22](https://github.com/riemann/riemann/pull/22) ([banjiewen](https://github.com/banjiewen))
- Allow custom message-decoding functions for TCP and UDP servers. [\#21](https://github.com/riemann/riemann/pull/21) ([banjiewen](https://github.com/banjiewen))
- Add streams/fold-interval and test [\#18](https://github.com/riemann/riemann/pull/18) ([b](https://github.com/b))

## [0.1.2](https://github.com/riemann/riemann/tree/0.1.2) (2012-06-17)

[Full Changelog](https://github.com/riemann/riemann/compare/0.1.1...0.1.2)

**Merged pull requests:**

- Replace :dev-dependencies with a profile for Leiningen 2 [\#15](https://github.com/riemann/riemann/pull/15) ([michaelklishin](https://github.com/michaelklishin))
- more portable bash shebang [\#14](https://github.com/riemann/riemann/pull/14) ([brianshumate](https://github.com/brianshumate))

## [0.1.1](https://github.com/riemann/riemann/tree/0.1.1) (2012-05-24)

[Full Changelog](https://github.com/riemann/riemann/compare/0.1.0...0.1.1)

**Merged pull requests:**

- Add \(adjust \(fn ...\) ...\) [\#12](https://github.com/riemann/riemann/pull/12) ([sjl](https://github.com/sjl))
- Minor style tweaks [\#11](https://github.com/riemann/riemann/pull/11) ([sjl](https://github.com/sjl))
- fill-in-last [\#10](https://github.com/riemann/riemann/pull/10) ([sjl](https://github.com/sjl))
- I'm an asshole, sorry. [\#9](https://github.com/riemann/riemann/pull/9) ([sjl](https://github.com/sjl))
- Two more minor tweaks [\#8](https://github.com/riemann/riemann/pull/8) ([sjl](https://github.com/sjl))
- Minor tweaks [\#7](https://github.com/riemann/riemann/pull/7) ([sjl](https://github.com/sjl))

## [0.1.0](https://github.com/riemann/riemann/tree/0.1.0) (2012-03-08)

[Full Changelog](https://github.com/riemann/riemann/compare/d87e6dfa1a8d9cd12de324d60b4adfa37afa0d4f...0.1.0)

**Closed issues:**

- Typo in Quickstart [\#5](https://github.com/riemann/riemann/issues/5)
- add development/testing instructions [\#1](https://github.com/riemann/riemann/issues/1)

**Merged pull requests:**

- Bernhard Riemann, 1826-1866, RIP [\#4](https://github.com/riemann/riemann/pull/4) ([jdmaturen](https://github.com/jdmaturen))
- Reimman now supports Clojure 1.3 [\#3](https://github.com/riemann/riemann/pull/3) ([snewman](https://github.com/snewman))
- Use map destructuring instead of \(or\) and \(let\) [\#2](https://github.com/riemann/riemann/pull/2) ([reiddraper](https://github.com/reiddraper))

\* _This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)_
