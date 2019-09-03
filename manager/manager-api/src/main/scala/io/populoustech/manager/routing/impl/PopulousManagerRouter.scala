package io.populoustech.manager.routing.impl

import io.populoustech.manager.domain.Job
import io.populoustech.manager.routing.{JacksonRoutes, Routes}
import io.vertx.core.Handler
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.http.HttpServerRequest
import io.vertx.scala.ext.web.client.WebClient

import scala.util.{Failure, Success}

sealed class PopulousManagerRouter (override val vertx: Vertx, implicit val context: VertxExecutionContext) extends JacksonRoutes(vertx = vertx) {

  override def attachRoutes(): Routes = {
    super.attachRoutes()

    router
      .get("/hello")
      .handler(_.response().end("world"))

    router
      .get("/jobs")
      .handler((rc: io.vertx.scala.ext.web.RoutingContext) => {

        val client = WebClient.create(vertx)
        client.get(8081, "localhost", "/jobs").sendFuture().onComplete{
          case Success(result) => {
            //log.info(s"Received response with status code ${result.statusCode()}")
            rc.response().setStatusCode(result.statusCode())
            rc.response().headers.setAll(result.headers)
            rc.response().write(result.bodyAsString().get)
            rc.response().end()
          }
          case Failure(cause) => {
            //log.error(s"$cause")
            rc.response().setStatusCode(500)
            rc.response().end(cause.getMessage);
          }
        } (context)
      })

    router.post("/jobs").handler((rc: io.vertx.scala.ext.web.RoutingContext) => {
      getBody(rc, classOf[Job]) match {
        case None => rc.fail(400)
        case Some(job) => {
          rc.response().setStatusCode(201)
          rc.response().end(job.tag.get)
          //mapService.create(todo) onSuccess { case newTodo => rc.setPayload(newTodo) }
        }
      }
    })


    this
  }

  /*restAPI.route.handler(BodyHandler.create())

  restAPI
    .get("/hello")
    .handler(_.response().end("world"))

  restAPI
    .get("/jobs")
    .handler((rc: io.vertx.scala.ext.web.RoutingContext) => {

      val client = WebClient.create(vertx)
      client.get(8081, "localhost", "/jobs").sendFuture().onComplete{
        case Success(result) => {
          log.info(s"Received response with status code ${result.statusCode()}")
          rc.response().setStatusCode(result.statusCode())
          rc.response().headers.setAll(result.headers)
          rc.response().write(result.bodyAsString().get)
          rc.response().end()
        }
        case Failure(cause) => {
          log.error(s"$cause")
          rc.response().setStatusCode(500)
          rc.response().end(cause.getMessage);
        }
      }
    })

  restAPI
    .post("/jobs")
    .handler((rc: io.vertx.scala.ext.web.RoutingContext) => {
      rc.response().putHeader("content-type", "application/json")

      val body = rc.getBodyAsJson()
      body match {
        case x if x.isDefined => {
          x.get.getJsonArray("tags") match {
          nulpointer al acceder a tags
            case tags if !tags.isEmpty => {
              rc.response().setStatusCode(201)
              rc.response().end()
            }
            case _ => {
              log.info("bad request: no tags")
              rc.fail(400)
            }
          }
        }
        case _ => {
          log.info("bad request: no body")
          rc.fail(400)
        }
      }
    })

  restAPI.route.handler((rc: io.vertx.scala.ext.web.RoutingContext) => {
    rc.fail(404)
  })*/

}

object PopulousManagerRouter {

  def apply(vertx: Vertx, context: VertxExecutionContext): Handler[HttpServerRequest] = {
    new PopulousManagerRouter(vertx, context).apply()
  }

}





