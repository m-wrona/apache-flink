package com.mwronski.flink.stream

import com.mwronski.flink.avro.KafkaDeserializationSchema
import io.confluent.examples.streams.avro.{PlayEvent, Song}
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010

/**
  * Sample reads music data generate by confluent sample and summarizes time of played songs.
  * Output is generated for some short time only since song stream (topic songs) is short & limited.
  *
  * Run before in your shell in order to create Kafka locally and produce sample content:
  * $docker-compose up
  */
object Kafka010Example {

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

    env
      .addSource(playEventsConsumer)
      .join(env.addSource(songsConsumer))
      .where(_.getSongId)
      .equalTo(_.getId)
      .window(TumblingEventTimeWindows.of(Time.seconds(5)))
      .apply((e, s) => (e.getSongId, s.getName, e.getDuration))
      .keyBy(0)
      .sum(2)
      .print()

    env.execute("Kafka Example")
  }

}

