package io.populoustech.manager

import io.populoustech.manager.routing.ManagerRoutes
import io.vertx.lang.scala.{ScalaLogger, ScalaVerticle}

import scala.concurrent.Future

class HttpVerticle extends ScalaVerticle {

  private val log = ScalaLogger.getLogger(classOf[HttpVerticle].getName)

  override def startFuture(): Future[_] = {
    log.info("HttpVerticle startFuture()")

    vertx
      .createHttpServer()
      .requestHandler(ManagerRoutes(vertx, executionContext))
      .listenFuture(PopulousManagerServer.PORT, PopulousManagerServer.HOST)
  }

}

object PopulousManagerServer {
  val PORT = 8666
  val HOST = "localhost"
}