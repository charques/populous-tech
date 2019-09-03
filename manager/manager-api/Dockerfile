FROM openjdk:8

# SBT & Scala
ENV SCALA_VERSION=2.12.6
ENV SBT_VERSION=1.2.8

#COPY run_jar.sh /

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
      #&& chmod +x /run_jar.sh \
      #&& update-java-alternatives -s java-1.8.0-openjdk-amd64 \
      && mkdir -p /app \
      && mkdir -p /usr/src/app

COPY . /usr/src/app
RUN cd /usr/src/app \
#        && sbt clean package
         && sbt clean assembly

CMD java $* -jar /usr/src/app/target/scala-2.12/app-assembly-0.1.0-SNAPSHOT.jar
#RUN mkdir -p /opt/app
#WORKDIR /opt/app

#COPY ./run_jar.sh ./target/scala-2.12/manager-api-assembly-0.1.0-SNAPSHOT.jar ./

#ENTRYPOINT ["./run_jar.sh"]