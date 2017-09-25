package com.mwronski.flink

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer010, FlinkKafkaProducer010}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

/**
  * Sample reads data from Kafka's topic and groups them once a while.
  *
  * Run before in your shell in order to create Kafka locally and produce sample content:
  * $docker-compose up
  */
object Kafka010Example {

  def main(args: Array[String]): Unit = {

    val params = ParameterTool.fromArgs(Array(
      "--input-topic", "play-events",
      "--bootstrap.servers", "localhost:9092",
      "--zookeeper.connect", "localhost:32181"
    ))
    println(s"Input-topic: ${params.get("input-topic")}")

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.disableSysoutLogging
    env.getConfig.setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000))
    env.enableCheckpointing(5000)
    env.getConfig.setGlobalJobParameters(params)

    val kafkaConsumer = new FlinkKafkaConsumer010(
      params.get("input-topic"),
      new SimpleStringSchema,
      params.getProperties
    )

    env
      .addSource(kafkaConsumer)
      .rebalance
      .map {
        (_, 1)
      }
      .keyBy(0)
      .timeWindow(Time.seconds(5))
      .sum(1)
      .print()

    env.execute("Kafka Example")
  }

}