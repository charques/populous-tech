FROM openjdk:8

ENV SBT_VERSION 1.2.8
ENV FILEBEAT_VERSION 7.3.0

RUN \
  curl -L -o sbt-${SBT_VERSION}.deb http://dl.bintray.com/sbt/debian/sbt-${SBT_VERSION}.deb && \
  dpkg -i sbt-${SBT_VERSION}.deb && \
  rm sbt-${SBT_VERSION}.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

WORKDIR /populous
COPY . /populous
CMD sbt run
