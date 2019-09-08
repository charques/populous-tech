package io.populoustech.manager.verticle

import java.net.{InetAddress, UnknownHostException}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.typesafe.scalalogging.Logger
import io.populoustech.manager.handlers.{FailureHandler, FlinkCreateJobHandler, FlinkGetJobsHandler}
import io.populoustech.manager.{ConfigurationKeys, EventBusChannels}
import io.vertx.core.Handler
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.ext.web.handler.BodyHandler
import io.vertx.scala.ext.web.{Router, RoutingContext}
import org.slf4j.{LoggerFactory, MDC}

import scala.util.{Failure, Success}

class JobManagerVerticle extends ScalaVerticle {

  private val logger = Logger(LoggerFactory.getLogger(classOf[JobManagerVerticle].getName))

  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  override def start(): Unit = {
    logger.info("HttpVerticle startFuture()")

    configureLogging()
    configureEventBus()

    val router: Router = configureRouter(config)
    val host = config.getString(ConfigurationKeys.MANAGER_API_HOST)
    val port = config.getInteger(ConfigurationKeys.MANAGER_API_PORT)

    vertx.createHttpServer()
      .requestHandler(router)
      .listenFuture(port, host).onComplete {
      case Success(_) => logger.info("HttpServer initialized")
      case Failure(cause) => logger.error(cause.getMessage)
    }
  }

  private def configureRouter(config: JsonObject): Router = {

    val setJsonContentType: Handler[RoutingContext] = ctx => {
      ctx.response.headers.add("content-type", "application/json")
      ctx.next
    }

    val sendPayloadAsJson: Handler[RoutingContext] = ctx => {
      ctx.getBody() match {
        case None => ctx.response.setStatusCode(204).end
        case Some(value) => ctx.response.end(mapper.writeValueAsString(value))
      }
    }

    val router: Router = Router.router(vertx)
    router.route.handler(setJsonContentType)
    router.route.handler(BodyHandler.create.handle)
    router.route.last.handler(sendPayloadAsJson)

    val failureHandler = new FailureHandler
    val flinkGetJobsHandler = FlinkGetJobsHandler(vertx, executionContext)
    val flinkCreateJobHandler = FlinkCreateJobHandler(vertx, executionContext)

    router
      .get("/hello")
      .handler(_.response().end("world"))

    router
      .get("/jobs")
      .handler(flinkGetJobsHandler)
      .failureHandler(failureHandler)

    router
      .post("/jobs")
      .handler(flinkCreateJobHandler)
      .failureHandler(failureHandler)

    router
  }

  private def configureEventBus(): Unit = {
    vertx.eventBus.consumer(EventBusChannels.CONFIGURATION_CHANGED)
      .completionHandler((message) => {
        logger.debug("Configuration has changed, verticle {} is updating...", deploymentID)
        //configureRoutes(message)
        logger.debug("Configuration has changed, verticle {} has been updated...", deploymentID)
      })
  }

  private def configureLogging(): Unit = {
    MDC.put("application", "populous-manager-api")
    MDC.put("version", "1.0.0")
    MDC.put("release", "canary")
    try MDC.put("hostname", InetAddress.getLocalHost.getHostName)
    catch {
      case e: UnknownHostException =>
      // Silent error, we can live without it
    }
  }

}

object JobManagerVerticle {
  def apply(): JobManagerVerticle = {
    new JobManagerVerticle()
  }
}
