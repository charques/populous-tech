package io.populoustech.manager.routing

import io.vertx.core.Handler
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.http.HttpServerRequest
import io.vertx.scala.ext.web.Router

abstract class Routes(val vertx: Vertx) {

  lazy val router: Router = Router.router(vertx)

  def attachRoutes(): Routes

  def apply(): Handler[HttpServerRequest] = {
    attachRoutes()
    req => router.accept(req)
  }

}

