FROM openjdk:8-jre-slim as builder
RUN apt-get update && \
    apt-get install -y --no-install-recommends apt-transport-https apt-utils bc dirmngr gnupg && \
    echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 && \
    apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y --no-install-recommends sbt
COPY . /ergo-explorer
WORKDIR /ergo-explorer
RUN sbt reload clean assembly
RUN mv `find . -name ergo-explorer-assembly*.jar` /ergo-explorer.jar
CMD ["/usr/bin/java", "-jar", "/ergo-explorer.jar"]

FROM openjdk:8-jre-slim
MAINTAINER Aleksei Terekhin <daron666@yandex.ru>
COPY --from=builder /ergo-explorer.jar /ergo-explorer.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/ergo-explorer.jar"]
CMD [""]
