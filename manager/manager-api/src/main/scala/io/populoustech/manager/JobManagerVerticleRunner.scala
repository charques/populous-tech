package io.populoustech.manager

import java.util.concurrent.TimeUnit

import com.codahale.metrics.{SharedMetricRegistries, Slf4jReporter}
import io.populoustech.manager.verticle.JobManagerVerticle
import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.config.{ConfigRetriever, ConfigRetrieverOptions, ConfigStoreOptions}
import io.vertx.scala.core.{DeploymentOptions, Vertx, VertxOptions}
import io.vertx.scala.ext.dropwizard.DropwizardMetricsOptions
import org.slf4j.LoggerFactory

object JobManagerVerticleRunner {

  def main(args: Array[String]): Unit = {

    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")

    // Initialize metric registry
    val registryName = "registry"
    val registry = SharedMetricRegistries.getOrCreate(registryName)
    SharedMetricRegistries.setDefault(registryName)

    val reporter = Slf4jReporter.forRegistry(registry)
      .outputTo( LoggerFactory.getLogger(classOf[JobManagerVerticle]))
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build
    reporter.start(1, TimeUnit.MINUTES)

    // Initialize vertx with the metric registry
    val metricsOptions = DropwizardMetricsOptions().setEnabled(true).setRegistryName(registryName)
    //.setMetricRegistry(metricRegistry)
    val vertxOptions: VertxOptions = VertxOptions().setMetricsOptions(metricsOptions)
    val vertx = Vertx.vertx(vertxOptions)

    val configRetrieverOptions = getConfigRetrieverOptions
    val configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions)
    // getConfig is called for initial loading
    configRetriever.getConfig((ar) => {
        val instances = Runtime.getRuntime.availableProcessors
        val deploymentOptions = DeploymentOptions().setInstances(instances).setConfig(ar.result)
        vertx.deployVerticle(ScalaVerticle.nameForVerticle[JobManagerVerticle], deploymentOptions)
    })

    // listen is called each time configuration changes// listen is called each time configuration changes
    configRetriever.listen((change) => {
        val updatedConfiguration = change.getNewConfiguration
        vertx.eventBus.publish(EventBusChannels.CONFIGURATION_CHANGED, updatedConfiguration)
    })
  }

  private def getConfigRetrieverOptions: ConfigRetrieverOptions = {
    val classpathFileConfiguration = new JsonObject().put("path", "default.properties")
    val classpathFile = ConfigStoreOptions().setType("file")
      .setFormat("properties")
      .setConfig(classpathFileConfiguration)

    ConfigRetrieverOptions().addStore(classpathFile).setScanPeriod(5000)
  }
}
