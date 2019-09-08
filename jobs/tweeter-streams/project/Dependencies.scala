import sbt._

object Dependencies {

  val flinkVersion = "1.9.0"

  val logs: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "commons-logging" % "commons-logging" % "1.2" )

  val flinkDependencies: Seq[ModuleID] = Seq(
    "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % flinkVersion,
    "org.apache.flink" %% "flink-connector-twitter" % flinkVersion,
    "org.apache.flink" %% "flink-connector-elasticsearch2"  % flinkVersion,
    "net.liftweb" %% "lift-json" % "3.3.0",
    "com.github.nscala-time" %% "nscala-time" % "2.14.0")

}
