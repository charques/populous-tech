import sbt.Package._
import sbt._

scalaVersion := "2.12.6"

libraryDependencies ++= Vector (
  Library.vertx_lang_scala,
  Library.vertx_web,
  Library.vertx_web_client,
  Library.jackson_module_scala,
  Library.scalaTest       % "test",
  // Uncomment for clustering
  // Library.vertx_hazelcast,

  //required to get rid of some warnings emitted by the scala-compile
  Library.vertx_codegen
)

packageOptions += ManifestAttributes(
  ("Main-Verticle", "scala:io.populoustech.manager.HttpVerticle"))

