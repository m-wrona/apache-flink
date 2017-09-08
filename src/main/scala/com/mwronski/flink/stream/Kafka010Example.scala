package com.mwronski.flink

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer010, FlinkKafkaProducer010}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

/**
  * An example that shows how to read from and write to Kafka. This will read String messages
  * from the input topic, prefix them by a configured prefix and output to the output topic.
  *
  * Please pass the following arguments to run the example:
  * {{{
  * --input-topic test-input
  * --output-topic test-output
  * --bootstrap.servers localhost:9092
  * --zookeeper.connect localhost:2181
  * --group.id myconsumer
  * }}}
  */
object Kafka010Example {

  def main(args: Array[String]): Unit = {

    // parse input arguments
    val params = ParameterTool.fromArgs(args)

    if (params.getNumberOfParameters < 4) {
      println(
        "Missing parameters!\n"
          + "Usage: Kafka --input-topic <topic> --output-topic <topic> "
          + "--bootstrap.servers <kafka brokers> "
          + "--zookeeper.connect <zk quorum> --group.id <some id> [--prefix <prefix>]"
      )
      return
    }

    val prefix = params.get("prefix", "PREFIX:")


    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.disableSysoutLogging
    env.getConfig.setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000))
    env.enableCheckpointing(5000)
    env.getConfig.setGlobalJobParameters(params)

    val kafkaConsumer = new FlinkKafkaConsumer010(
      params.getRequired("input-topic"),
      new SimpleStringSchema,
      params.getProperties
    )

    val messageStream = env
      .addSource(kafkaConsumer)
      .map(in => prefix + in)

    // create a Kafka producer for Kafka 0.10.x
    val kafkaProducer = new FlinkKafkaProducer010(
      params.getRequired("output-topic"),
      new SimpleStringSchema,
      params.getProperties
    )

    // write data into Kafka
    messageStream.addSink(kafkaProducer)

    env.execute("Kafka Example")
  }

}