package com.mwronski.flink.table

import com.mwronski.flink.avro.KafkaDeserializationSchema
import io.confluent.examples.streams.avro.{PlayEvent, Song}
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.table.api.TableEnvironment

/**
  * Sample reads music data generate by confluent sample and summarizes time of played songs.
  *
  * Run before in your shell in order to create Kafka locally and produce sample content:
  * $docker-compose up
  */
object TableExample {

  def main(args: Array[String]): Unit = {

    val params = ParameterTool.fromArgs(Array(
      "--topic-play-events", "play-events",
      "--topic-songs", "song-feed",
      "--bootstrap.servers", "localhost:9092",
      "--zookeeper.connect", "localhost:32181",
      "--schema-registry", "http://localhost:8081"
    ))
    println(s"topic-play-events: ${params.get("topic-play-events")}")

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tEnv = TableEnvironment.getTableEnvironment(env)
    env.getConfig.disableSysoutLogging
    env.getConfig.setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000))
    env.enableCheckpointing(5000)
    env.getConfig.setGlobalJobParameters(params)
    env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime)

    val playEventsConsumer = new FlinkKafkaConsumer010[PlayEvent](
      params.get("topic-play-events"),
      new KafkaDeserializationSchema(classOf[PlayEvent], params.get("schema-registry"), params.get("topic-play-events")),
      params.getProperties
    )

    val songsConsumer = new FlinkKafkaConsumer010[Song](
      params.get("topic-songs"),
      new KafkaDeserializationSchema(classOf[Song], params.get("schema-registry"), params.get("topic-songs")),
      params.getProperties
    )
    songsConsumer.setStartFromEarliest()

    tEnv.registerDataStream("Songs", env.addSource(songsConsumer))
    tEnv.registerDataStream("PLayEvents", env.addSource(playEventsConsumer))

    val tSongs = tEnv.fromDataStream(env.addSource(songsConsumer))
    val tPlayEvents = tEnv.fromDataStream(env.addSource(playEventsConsumer))

    tSongs
      .select("*")

    env.execute("Table Example")
  }

}

