package io.populoustech.manager.services.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import io.populoustech.manager.domain.Job
import io.populoustech.manager.services.JobsService
import io.vertx.lang.scala.{ScalaLogger, VertxExecutionContext}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.WebClient

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent._
import ExecutionContext.Implicits.global

class JobsBasicService(val vertx: Vertx, implicit val context: VertxExecutionContext) extends JobsService {

  private val log = ScalaLogger.getLogger(classOf[JobsBasicService].getName)

  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  override def list(): Future[Iterable[Job]] = {
    val p = Promise[Iterable[Job]]
    WebClient.create(vertx)
      .get(8081, "localhost", "/jobs/overview").sendFuture().onComplete {
        case Success(result) => {
          log.info(s"Received response with status code ${result.statusCode()}")
          val job: Job = new Job(Option("0"),Option("mytag"))
          p.success(List(job))
        }
        case Failure(cause) => {
          log.error(s"$cause")
          p.failure(cause)
        }
      }
    p.future
  }

}

object JobsBasicService {
  def apply(vertx: Vertx, context: VertxExecutionContext) = new JobsBasicService(vertx, context)
}
