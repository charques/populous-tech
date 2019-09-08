import sbt._
import Dependencies._

ThisBuild / resolvers ++= Seq(
    "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
    "Spray IO Repository" at "http://repo.spray.io/",
    "Maven Central" at "https://repo1.maven.org/maven2/",
    "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
    Resolver.mavenLocal
)

name := "tweeter-streams"
version := "0.1-SNAPSHOT"
organization := "io.populoustech"

ThisBuild / scalaVersion := "2.12.6"

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= logs ++ flinkDependencies
  )

assembly / mainClass := Some("io.populoustech.TweeterStreamToFileJob")

// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / fullClasspath,
                                   Compile / run / mainClass,
                                   Compile / run / runner
                                  ).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// exclude Scala library from assembly
assembly / assemblyOption  := (assembly / assemblyOption).value.copy(includeScala = false)
