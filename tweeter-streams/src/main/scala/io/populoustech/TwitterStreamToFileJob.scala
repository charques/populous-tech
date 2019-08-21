package io.populoustech

import java.util.Properties
import java.util.concurrent.TimeUnit

import com.twitter.hbc.core.endpoint.{StatusesFilterEndpoint, StreamingEndpoint}
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
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions

object TwitterStreamToFileJob {

  val LOG = LoggerFactory.getLogger(TwitterStreamToFileJob.getClass)

  def main(args: Array[String]): Unit = {

    LOG.info("TwitterStreamToFileJob init")

    val fileOutput = true
    val output = "/var/log/tweeter-streams"

    // Checking input parameters
    val params = ParameterTool.fromArgs(args)

    // set up the execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    // make parameters available in the web interface
    env.getConfig.setGlobalJobParameters(params)

    env.setParallelism(params.getInt("parallelism", 1))
    env.enableCheckpointing(10000L)
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, Time.of(10L, TimeUnit.SECONDS)))

    val props = new Properties()
    props.load(TwitterStreamToFileJob.getClass.getResourceAsStream("/twitter.properties"))
    val twitterSource = new TwitterSource(props)
    twitterSource.setCustomEndpointInitializer(new FilterEndpoint("#pp", "#vox"))
    val streamSource: DataStream[String] = env.addSource(twitterSource)

    val tweets: DataStream[String] = streamSource
    // selecting English tweets and splitting to (word, 1)
    //.flatMap(new SelectEnglishAndTokenizeFlatMap)
    // group by words and sum their occurrences
    //.keyBy(0).sum(1)

    // emit result
    if (fileOutput) {
      LOG.info("Printing result to file output")

      val sink: StreamingFileSink[String] = StreamingFileSink
        .forRowFormat(new Path(output), new SimpleStringEncoder[String]("UTF-8"))
        .withRollingPolicy(OnCheckpointRollingPolicy.build())
        .build()

      tweets.addSink(sink)
      tweets.print()

    } else {
      LOG.info("Printing result to stdout")
      tweets.print()
    }

    // execute program
    env.execute("TwitterStreamToFileJob execute")
  }

  private class FilterEndpoint(tags: String*) extends TwitterSource.EndpointInitializer with Serializable {
    override def createEndpoint: StreamingEndpoint = {
      val ep = new StatusesFilterEndpoint
      ep.trackTerms(JavaConversions.seqAsJavaList(tags.toList))
      ep
    }
  }
}
