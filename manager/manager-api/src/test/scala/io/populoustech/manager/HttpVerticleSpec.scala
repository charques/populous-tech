package io.populoustech.manager

import io.populoustech.manager.verticle.JobManagerVerticle
import org.scalatest.Matchers

import scala.concurrent.Promise

class HttpVerticleSpec extends VerticleTesting[JobManagerVerticle] with Matchers {

  "HttpVerticle" should "bind to 8666 and answer with 'world'" in {
    val promise = Promise[String]

    vertx.createHttpClient()
      .getNow(8666, "127.0.0.1", "/hello",
        r => {
          r.exceptionHandler(promise.failure)
          r.bodyHandler(b => promise.success(b.toString))
        })

    promise.future.map(res => res should equal("world"))
  }

  "HttpVerticle" should "bind to 8666 and answer with flink jobs" in {
    val promise = Promise[Boolean]

    vertx.createHttpClient()
      .getNow(8666, "localhost", "/jobs",
        r => {
          r.exceptionHandler(promise.failure)
          r.bodyHandler(b => {
              promise.success(b.toJsonObject.containsKey("jobs"))
            })
        })

    promise.future.map(res => res should equal(true))
  }

}
