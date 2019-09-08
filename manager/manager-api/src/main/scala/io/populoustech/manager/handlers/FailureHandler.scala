package io.populoustech.manager.handlers

import com.codahale.metrics.{Counter, SharedMetricRegistries}
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.typesafe.scalalogging.Logger
import io.vertx.core.Handler
import io.vertx.scala.ext.web.RoutingContext
import org.slf4j.LoggerFactory

object FailureHandler {
  def apply() = new FailureHandler()
}

class FailureHandler extends Handler[RoutingContext] {

  private val log: Logger = Logger(LoggerFactory.getLogger(classOf[FailureHandler].getName))

  val validationErrorsCounter: Counter = SharedMetricRegistries.getDefault.counter("validationErrors")

  def handle(context: RoutingContext): Unit = {
    val thrown = context.failure
    val userId = context.request.getHeader("Authorization")
    recordError(userId, thrown)
    if (thrown.isInstanceOf[MismatchedInputException] || thrown.isInstanceOf[NoSuchElementException])
      context.response.setStatusCode(400).end(thrown.getMessage)
    else
      context.response.setStatusCode(500).end(thrown.getMessage)
  }

  private def recordError(userId: Option[String], thrown: Throwable): Unit = {
    var dynamicMetadata = ""
    if (userId != null) dynamicMetadata = String.format("userId=%s ", userId)
    validationErrorsCounter.inc()
    log.error(dynamicMetadata + thrown.getMessage)
  }
}
