package com.mwronski.flink.stream

import java.util
import java.util.{Collections, Map}

import io.confluent.examples.streams.avro.PlayEvent
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.{GenericAvroDeserializer, SpecificAvroDeserializer, SpecificAvroSerde, SpecificAvroSerializer}
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer010, FlinkKafkaProducer010}
import org.apache.flink.streaming.util.serialization.DeserializationSchema
import org.apache.kafka.common.serialization.Serdes

/**
  * Sample reads music data generate by confluent sample and summarizes time of played songs.
  *
  * Run before in your shell in order to create Kafka locally and produce sample content:
  * $docker-compose up
  */
object Kafka010Example {

  def main(args: Array[String]): Unit = {

    val params = ParameterTool.fromArgs(Array(
      "--input-topic", "play-events",
      "--bootstrap.servers", "localhost:9092",
      "--zookeeper.connect", "localhost:32181",
      "--schema-registry", "http://localhost:8081"
    ))
    println(s"Input-topic: ${params.get("input-topic")}")

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.disableSysoutLogging
    env.getConfig.setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000))
    env.enableCheckpointing(5000)
    env.getConfig.setGlobalJobParameters(params)

    val kafkaConsumer = new FlinkKafkaConsumer010(
      params.get("input-topic"),
      new PlayEventDeserializationSchema(params.get("schema-registry"), params.get("input-topic")),
      params.getProperties
    )

    env
      .addSource(kafkaConsumer)
      .rebalance
      .map { e =>
        (e.getSongId, e.getDuration)
      }
      .keyBy(0)
      .timeWindow(Time.seconds(5))
      .sum(1)
      .print()

    env.execute("Kafka Example")
  }


  private class PlayEventDeserializationSchema(schemaUrl: String, topicName: String, isDeserializerForKeys: Boolean = false) extends DeserializationSchema[PlayEvent] {

    @transient private lazy val playEventSerializer = new SpecificAvroDeserializer[PlayEvent]() {
      {
        configure(Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), isDeserializerForKeys)
      }
    }

    override def isEndOfStream(nextElement: PlayEvent): Boolean = false

    override def deserialize(message: Array[Byte]): PlayEvent = playEventSerializer.deserialize(topicName, message)

    override def getProducedType: TypeInformation[PlayEvent] = TypeExtractor.getForClass(classOf[PlayEvent])
  }

}

