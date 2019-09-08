import sbt.Package._
import sbt._

scalaVersion := "2.12.6"

libraryDependencies ++= Vector (
  Library.vertx_lang_scala,
  Library.vertx_web,
  Library.vertx_web_client,
  Library.vertx_config,
  Library.vertx_dropwizard_metrics,
  Library.vertx_health_check,
  Library.vertx_circuit_breaker,
  Library.jackson_module_scala,
  Library.logback_classic,
  Library.scala_logging,
  Library.scalaTest       % "test",
  // Uncomment for clustering
  // Library.vertx_hazelcast,

  //required to get rid of some warnings emitted by the scala-compile
  Library.vertx_codegen
)

packageOptions += ManifestAttributes(
  ("Main-Verticle", "scala:io.populoustech.manager.HttpVerticle"))

