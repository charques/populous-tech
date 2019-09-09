package io.populoustech

import java.util.Properties
import java.util.concurrent.TimeUnit

import com.twitter.hbc.core.endpoint.{StatusesFilterEndpoint, StreamingEndpoint}
import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.api.common.time.Time
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.twitter.TwitterSource
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters
import scala.util.control.Exception.allCatch

object TweeterStreamToFileJob {

  def main(args: Array[String]): Unit = {

    val logger: Logger = LoggerFactory.getLogger(this.getClass)

    val params = ParameterTool.fromArgs(args)
    val tweeterTag = params.get("tag", "#scala")
    val parallelism = params.getInt("parallelism", 1)
    val checkpointing = params.getInt("checkpointing", 30000)
    val fileOutput = params.getBoolean("fileoutput", true)
    val outputFolder = params.get("outputfolder", "/var/log/tweeter-streams")

    logger.info("Params: " +  params.toMap.toString)

    // set up the execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    // make parameters available in the web interface
    env.getConfig.setGlobalJobParameters(params)

    env.setParallelism(parallelism)
    env.enableCheckpointing(checkpointing)
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, Time.of(10L, TimeUnit.SECONDS)))

    val props = new Properties()
    props.load(TweeterStreamToFileJob.getClass.getResourceAsStream("/twitter.properties"))
    val twitterSource = new TwitterSource(props)
    twitterSource.setCustomEndpointInitializer(new FilterEndpoint(tweeterTag))

    val streamSource: DataStream[String] = env.addSource(twitterSource)
      .uid("tweeter-source")

    val tweets: DataStream[String] = streamSource.filter(new FilterFunction[String]() {
      // filter integers
      @throws[Exception]
      override def filter(value: String): Boolean = (allCatch opt value.toDouble).isEmpty
    }).uid("stream-int-cleaner")

    // emit result
    if (fileOutput) {
      logger.info("Printing result to file output")

      val sink: StreamingFileSink[String] = StreamingFileSink
        .forRowFormat(new Path(outputFolder), new SimpleStringEncoder[String]("UTF-8"))
        .withRollingPolicy(OnCheckpointRollingPolicy.build())
        .build()

      tweets.addSink(sink).uid("stream-file-sink")
      //tweets.print().uid("stdout-sink")

    } else {
      logger.info("Printing result to stdout")
      tweets.print()
    }

    // execute program
    env.execute("TweeterStreamToFileJob - " + tweeterTag)
  }


  private class FilterEndpoint(tags: String*) extends TwitterSource.EndpointInitializer with Serializable {
    override def createEndpoint: StreamingEndpoint = {
      val ep = new StatusesFilterEndpoint
      ep.trackTerms(JavaConverters.seqAsJavaList(tags.toList))
      ep
    }
  }
}
