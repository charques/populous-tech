package io.populoustech.manager.routing

import io.populoustech.manager.domain.CreateJobRequest
import io.populoustech.manager.services.FlinkJobsServiceImpl
import io.vertx.core.Handler
import io.vertx.lang.scala.{ScalaLogger, VertxExecutionContext}
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.http.HttpServerRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

sealed class ManagerRoutes(override val vertx: Vertx, implicit val context: VertxExecutionContext) extends Routes(vertx = vertx) {

  private val log = ScalaLogger.getLogger(classOf[ManagerRoutes].getName)

  lazy val jobsService = FlinkJobsServiceImpl(vertx, context)

  override def attachRoutes(): Routes = {
    super.attachRoutes()

    router
      .get("/hello")
      .handler(_.response().end("world"))

    router
      .get("/jobs")
      .handler((rc: io.vertx.scala.ext.web.RoutingContext) => {

        jobsService.list().onComplete {
          case Success(result) => {
            log.info("JobService list OK")
            rc.response().setStatusCode(200)
            rc.response().headers.setAll(result.headers)
            rc.response().end(result.body().get)
          }
          case Failure(cause) => {
            log.info("JobService list KO ", cause.getMessage)
            rc.response().setStatusCode(500)
            rc.response().end(cause.getMessage)
          }
        }
      })

    router.post("/jobs").handler((rc: io.vertx.scala.ext.web.RoutingContext) => {
      getBody(rc, classOf[CreateJobRequest]) match {
        case None => rc.fail(400)
        case Some(job) => {
          jobsService.create(job.tag.get).onComplete {
            case Success(result) => {
              log.info("JobService create OK")
              rc.response().setStatusCode(202)
              rc.response().end(result.bodyAsString().get)
            }
            case Failure(cause) => {
              log.info("JobService create KO ", cause.getMessage)
              rc.response().setStatusCode(500)
              rc.response().end(cause.getMessage)
            }
          }
        }
      }
    })

    this
  }
}

object ManagerRoutes {

  def apply(vertx: Vertx, context: VertxExecutionContext): Handler[HttpServerRequest] = {
    new ManagerRoutes(vertx, context).apply()
  }

}





