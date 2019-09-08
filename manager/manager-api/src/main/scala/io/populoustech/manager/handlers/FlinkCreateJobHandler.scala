package io.populoustech.manager.handlers

import com.typesafe.scalalogging.Logger
import io.populoustech.manager.domain.{CreateJobRequest, FlinkJarsUploadResponse, FlinkRunJarRequest}
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.scala.circuitbreaker.{CircuitBreaker, CircuitBreakerOptions}
import io.vertx.scala.core.{Promise, Vertx}
import io.vertx.scala.ext.web.RoutingContext
import io.vertx.scala.ext.web.client.{HttpResponse, WebClient}
import io.vertx.scala.ext.web.multipart.MultipartForm
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class FlinkCreateJobHandler(override val vertx: Vertx, implicit val context: VertxExecutionContext) extends CircuitBreakerHandler(vertx = vertx) {

  private val log: Logger = Logger(LoggerFactory.getLogger(classOf[FlinkCreateJobHandler].getName))

  private val circuitBreakerOptions: CircuitBreakerOptions = CircuitBreakerOptions()
    .setMaxFailures(3)
    .setTimeout(30000)
    .setFallbackOnFailure(true)
    .setResetTimeout(60000);

  val circuitBreaker: CircuitBreaker = CircuitBreaker.create("flink-create-job", vertx, circuitBreakerOptions)
  circuitBreaker.openHandler((v) => log.info("{} circuit breaker is open", "flink-create-job"))
  circuitBreaker.closeHandler((v) => log.info("{} circuit breaker is closed", "flink-create-job"))
  circuitBreaker.halfOpenHandler((v) => log.info("{} circuit breaker is half open", "flink-create-job"))

  override def handle(event: RoutingContext): Unit = {

    def command(future: Promise[String]): Promise[String] = {
      getBody(event, classOf[CreateJobRequest]) match {
        case None => future.fail("HTTP error")
        case Some(job) => {
          uploadJar(job.tag.get)
            .flatMap(runJob).onComplete {
              case Success(httpResponse) => future.complete(httpResponse.bodyAsString().get)
              case Failure(cause) => future.fail(cause.getMessage)
          }
        }
      }
      future
    }

    def fallback(v: java.lang.Throwable): String = {
      "Downstream service unavailable."
    }

    circuitBreaker.executeCommandWithFallbackFuture(command, fallback).onComplete {
      case Success(result) => {
        log.info("JobService create OK")
        event.response().setStatusCode(202)
        event.response().end(result.toString)
      }
      case Failure(cause) => {
        log.error(cause.getMessage, cause)
        event.response().setStatusCode(500).end(cause.getMessage)
      }
    }
  }

  private def uploadJar(jobTag: String): Future[String] = {
    val form = MultipartForm.create()
      .binaryFileUpload("jarfile", FlinkConfigValues.JAR_FILE_NAME, FlinkConfigValues.JAR_PATH, FlinkConfigValues.JAR_MEDIA_TYPE)

    WebClient.create(vertx)
      .post(FlinkConfigValues.FLINK_PORT, FlinkConfigValues.FLINK_HOST, "/jars/upload").sendMultipartFormFuture(form)
      .flatMap(response => {
        Future {
          log.info("uploadJar: " + response.body().toString)
          // get jarid
          getObjectFromJsonString(response.bodyAsString(), classOf[FlinkJarsUploadResponse]) match {
            case Some(jarsUploadResponse) => {
              val parts: Array[String] = jarsUploadResponse.filename.get.split("/")
              val jarid = parts(parts.length - 1)
              log.info("jarId: " + jarid)
              jarid
            }
            case None => ""
          }
        }
      })
  }

  private def runJob(jarId: String): Future[HttpResponse[Buffer]] = {
    val flinkRunJarRequest = FlinkRunJarRequest(FlinkConfigValues.JOB_ENTRY_CLASS)

    log.info("runJob: " +  jarId + " : " + flinkRunJarRequest.entryClass)
    WebClient.create(vertx)
      .post(FlinkConfigValues.FLINK_PORT, FlinkConfigValues.FLINK_HOST, "/jars/" + jarId + "/run")
      .putHeader("content-type", "application/json")
      .sendJsonFuture(flinkRunJarRequest)
  }

}

object FlinkCreateJobHandler {

  def apply(vertx: Vertx, context: VertxExecutionContext) = new FlinkCreateJobHandler(vertx, context)

}

object FlinkConfigValues {
  val FLINK_HOST = "localhost"
  val FLINK_PORT = 8081

  val JAR_FILE_NAME = "application.jar"
  val JAR_PATH = "/Users/charques/Dev/populous-tech/shared/tweeter-streams/application.jar"
  val JAR_MEDIA_TYPE = "application/x-java-archive"
  val JOB_ENTRY_CLASS = "io.populoustech.TweeterStreamToFileJob"
}
