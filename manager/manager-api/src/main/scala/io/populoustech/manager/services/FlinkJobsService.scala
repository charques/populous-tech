package io.populoustech.manager.services

import io.populoustech.manager.domain.{FlinkJarsUploadResponse, FlinkRunJarRequest}
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.{ScalaLogger, VertxExecutionContext}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.{HttpResponse, WebClient}
import io.vertx.scala.ext.web.multipart.MultipartForm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, _}

trait FlinkJobsService {
  def list(): Future[HttpResponse[Buffer]]
  def create(jobTag: String): Future[HttpResponse[Buffer]]
  //def update(id: String, job: Job): Future[Option[Job]]
  //def retrieve(id: String): Future[Option[Job]]
}

class FlinkJobsServiceImpl(override val vertx: Vertx, implicit val context: VertxExecutionContext) extends Service(vertx=vertx) with FlinkJobsService {

  private val log = ScalaLogger.getLogger(classOf[FlinkJobsServiceImpl].getName)

  override def list(): Future[HttpResponse[Buffer]] = {
    WebClient.create(vertx)
      .get(FlinkConfigValues.FLINK_PORT, FlinkConfigValues.FLINK_HOST, "/jobs/overview").sendFuture()
  }

  override def create(jobTag: String): Future[HttpResponse[Buffer]] = {
    uploadJar(jobTag)
      .flatMap(runJob)
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

object FlinkJobsServiceImpl {
  def apply(vertx: Vertx, context: VertxExecutionContext) = new FlinkJobsServiceImpl(vertx, context)
}

object FlinkConfigValues {
  val FLINK_HOST = "localhost"
  val FLINK_PORT = 8081

  val JAR_FILE_NAME = "application.jar"
  val JAR_PATH = "/Users/charques/Dev/populous-tech/shared/tweeter-streams/application.jar"
  val JAR_MEDIA_TYPE = "application/x-java-archive"
  val JOB_ENTRY_CLASS = "io.populoustech.TweeterStreamToFileJob"
}