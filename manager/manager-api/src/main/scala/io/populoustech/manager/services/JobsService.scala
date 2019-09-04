package io.populoustech.manager.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import io.populoustech.manager.domain.Job
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.{ScalaLogger, VertxExecutionContext}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.{HttpResponse, WebClient}

import scala.concurrent.Future
import scala.concurrent._
import ExecutionContext.Implicits.global

trait JobsService {
  def list(): Future[HttpResponse[Buffer]]
  def create(job: Job): Future[Boolean]
  //def update(id: String, job: Job): Future[Option[Job]]
  //def retrieve(id: String): Future[Option[Job]]
}

class JobsServiceImpl(val vertx: Vertx, implicit val context: VertxExecutionContext) extends JobsService {

  private val log = ScalaLogger.getLogger(classOf[JobsServiceImpl].getName)

  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  override def list(): Future[HttpResponse[Buffer]] = {
    WebClient.create(vertx)
      .get(8081, "localhost", "/jobs/overview").sendFuture()
  }

  override def create(job: Job): Future[Boolean] = {
    Future {
      // TODO post job to flink
      true
    }
  }
}

object JobsServiceImpl {
  def apply(vertx: Vertx, context: VertxExecutionContext) = new JobsServiceImpl(vertx, context)
}