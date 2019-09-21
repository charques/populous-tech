package io.populoustech.manager.handlers

import com.typesafe.scalalogging.Logger
import io.populoustech.manager.ConfigurationKeys
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.circuitbreaker.{CircuitBreaker, CircuitBreakerOptions}
import io.vertx.scala.core.{Promise, Vertx}
import io.vertx.scala.ext.web.RoutingContext
import io.vertx.scala.ext.web.client.WebClient
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

class FlinkGetJobsHandler(override val vertx: Vertx, config: JsonObject, implicit val context: VertxExecutionContext) extends BaseHandler(vertx = vertx) {

  private val log: Logger = Logger(LoggerFactory.getLogger(classOf[FlinkGetJobsHandler].getName))

  private val FLINK_PORT: Integer = config.getInteger(ConfigurationKeys.FLINK_PORT)
  private val FLINK_HOST: String = config.getString(ConfigurationKeys.FLINK_HOST)

  private val circuitBreakerOptions: CircuitBreakerOptions = CircuitBreakerOptions()
    .setMaxFailures(3)
    .setTimeout(1000)
    .setFallbackOnFailure(true)
    .setResetTimeout(60000);

  val circuitBreaker: CircuitBreaker = CircuitBreaker.create("flink-get-jobs", vertx, circuitBreakerOptions)
  circuitBreaker.openHandler((v) => log.info("{} circuit breaker is open", "flink-get-jobs"))
  circuitBreaker.closeHandler((v) => log.info("{} circuit breaker is closed", "flink-get-jobs"))
  circuitBreaker.halfOpenHandler((v) => log.info("{} circuit breaker is half open", "flink-get-jobs"))

  override def handle(event: RoutingContext): Unit = {

    def command(future: Promise[String]): Promise[String] = {
      WebClient.create(vertx)
        .get(FLINK_PORT, FLINK_HOST, "/jobs/overview")
        .sendFuture().onComplete {
          case Success(httpResponse) => future.complete(httpResponse.bodyAsString().get)
          case Failure(cause) => future.fail(cause.getMessage)
        }
      future
    }

    def fallback(v: java.lang.Throwable): String = {
      "Downstream service unavailable."
    }

    circuitBreaker.executeCommandWithFallbackFuture(command, fallback).onComplete {
      case Success(result) => {
        log.info("JobService list OK")
        event.response.setStatusCode(200).end(result.toString)
      }
      case Failure(cause) => {
        log.error(cause.getMessage, cause)
        event.response.setStatusCode(500).end(cause.getMessage)
      }
    }

  }
}

object FlinkGetJobsHandler {

  def apply(vertx: Vertx, config: JsonObject, context: VertxExecutionContext) = new FlinkGetJobsHandler(vertx, config, context)

}
