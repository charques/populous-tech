package io.populoustech.manager

import io.vertx.core.Vertx
import io.vertx.lang.scala.ScalaVerticle

object HttpVerticleRunner {

  def main(args: Array[String]): Unit = {
    val vertx = Vertx.vertx
    vertx.deployVerticle(ScalaVerticle.nameForVerticle[HttpVerticle])
  }
}
