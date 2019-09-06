package io.populoustech.manager.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import io.vertx.scala.core.Vertx

abstract class Service(val vertx: Vertx) {

  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)

  protected def getObjectFromJsonString[T](jsonString: Option[String], clazz: Class[T]): Option[T] = {
    jsonString.map(body => mapper.readValue(body, clazz))
  }

  protected def getJsonStringFromObject(o: Object): String = {
    mapper.writeValueAsString(o)
  }
}
