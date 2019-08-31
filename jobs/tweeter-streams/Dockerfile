#FROM openjdk:8
#
#ENV SBT_VERSION 1.2.8
#
#RUN \
#  curl -L -o sbt-${SBT_VERSION}.deb http://dl.bintray.com/sbt/debian/sbt-${SBT_VERSION}.deb && \
#  dpkg -i sbt-${SBT_VERSION}.deb && \
#  rm sbt-${SBT_VERSION}.deb && \
#  apt-get update && \
#  apt-get install sbt && \
#  sbt sbtVersion
#
#WORKDIR /streams
#COPY . /streams
##CMD sbt run
#CMD sbt clean assembly

FROM flink:1.9.0-scala_2.12

ENV FLINK_APPLICATION_JAR_DESTINATION /app/application.jar
ENV FLINK_APPLICATION_JAR_ORIGIN target/scala-2.11/tweeter-streams-assembly-0.1-SNAPSHOT.jar
ENV FLINK_APPLICATION_MAIN_CLASS io.populoustech.TweeterStreamToFileJob
ENV FLINK_APPLICATION_ARGS ""

# SBT & Scala
ENV SCALA_VERSION=2.11.12
ENV SBT_VERSION=1.2.8

COPY submit.sh /

RUN apt-get update \
      && apt-get install dnsutils -y

RUN apt-get update \
      && curl -fsL http://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C / \
      && echo >> /.bashrc \
      && echo 'export PATH=~/scala-$SCALA_VERSION/bin:$PATH' >> /.bashrc \
      && curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb \
      && dpkg -i sbt-$SBT_VERSION.deb \
      && rm sbt-$SBT_VERSION.deb \
      && apt-get update \
      && apt-get install sbt \
      && apt-get clean \
      && rm -rf /var/lib/apt/lists/* \
      && chmod +x /submit.sh \
      #&& update-java-alternatives -s java-1.8.0-openjdk-amd64 \
      && mkdir -p /app \
      && mkdir -p /usr/src/app

COPY . /usr/src/app
RUN cd /usr/src/app \
#        && sbt clean package
         && sbt clean assembly

CMD ["/bin/bash", "/submit.sh"]