package io.populoustech.manager.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import io.vertx.core.Handler
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.RoutingContext

abstract class BaseHandler(val vertx: Vertx) extends Handler[RoutingContext] {

  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  protected def getObjectFromJsonString[T](jsonString: Option[String], clazz: Class[T]): Option[T] = {
    jsonString.map(body => mapper.readValue(body, clazz))
  }

  protected def getJsonStringFromObject(o: Object): String = {
    mapper.writeValueAsString(o)
  }

  protected def getBody[T](ctx: RoutingContext, clazz: Class[T]): Option[T] = {
    ctx.getBodyAsString.map(body => mapper.readValue(body, clazz))
  }
}
