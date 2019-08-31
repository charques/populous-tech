import sbt.Keys._
import sbt._

object Dependencies {

  val slf4jVersion = "1.7.5"
  val logbackVersion = "1.1.7"
  val junitVersion = "4.12"
  val junitInterfaceVersion = "0.8"
  val flinkVersion = "1.8.1"


  //val slf4j = Seq( "org.slf4j" % "slf4j-api" % slf4jVersion )

  //val logback = Seq( "ch.qos.logback" % "logback-classic" % logbackVersion )

  //val junit = Seq( "junit" % "junit" % junitVersion % "test" )

  //val junitInterface = Seq( "com.novocode" % "junit-interface" % junitInterfaceVersion % "test" )

  val flinkDependencies = Seq(
    "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
    "org.apache.flink" %% "flink-connector-twitter" % flinkVersion,
    "org.apache.flink" %% "flink-connector-elasticsearch2"  % flinkVersion,
    "commons-logging" % "commons-logging" % "1.2",
    "net.liftweb" %% "lift-json" % "2.6-M4",
    "com.github.nscala-time" %% "nscala-time" % "2.14.0")

}
