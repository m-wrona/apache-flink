package com.mwronski.flink.table

import java.util.Collections

import io.confluent.examples.streams.avro.{PlayEvent, Song}
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.streaming.util.serialization.DeserializationSchema
import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.table.api.java.Tumble
import org.apache.flink.table.plan.logical.TumblingGroupWindow

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
      new PlayEventDeserializationSchema(classOf[PlayEvent], params.get("schema-registry"), params.get("topic-play-events")),
      params.getProperties
    )

    val songsConsumer = new FlinkKafkaConsumer010[Song](
      params.get("topic-songs"),
      new PlayEventDeserializationSchema(classOf[Song], params.get("schema-registry"), params.get("topic-songs")),
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


  private class PlayEventDeserializationSchema[T](clazz: Class[T], schemaUrl: String, topicName: String, isDeserializerForKeys: Boolean = false) extends DeserializationSchema[T] {

    @transient private lazy val playEventSerializer = new SpecificAvroDeserializer[PlayEvent]() {
      {
        configure(Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), isDeserializerForKeys)
      }
    }

    override def isEndOfStream(nextElement: T): Boolean = false

    override def deserialize(message: Array[Byte]): T = playEventSerializer.deserialize(topicName, message).asInstanceOf[T]

    override def getProducedType: TypeInformation[T] = TypeExtractor.getForClass(clazz)
  }

}

