FROM clojure:lein AS builder

WORKDIR /riemann
ADD . .

RUN lein uberjar && \
    mv target/riemann-*-standalone.jar target/riemann.jar

FROM openjdk:10.0-jre
MAINTAINER Christoph Mewes <git@xrstf.de>

EXPOSE 5555/tcp 5555/udp 5556
CMD ["/bin/riemann", "/etc/riemann.config"]

COPY --from=builder /riemann/pkg/tar/riemann.config /etc/riemann.config
COPY --from=builder /riemann/pkg/tar/riemann /bin/riemann
COPY --from=builder /riemann/target/riemann.jar /lib/riemann.jar
