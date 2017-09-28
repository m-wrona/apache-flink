package com.mwronski.flink.kafka

import java.util.Collections

import io.confluent.examples.streams.avro.PlayEvent
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.streaming.util.serialization.DeserializationSchema

class KafkaDeserializationSchema[T](clazz: Class[T], schemaUrl: String, topicName: String, isDeserializerForKeys: Boolean = false) extends DeserializationSchema[T] {

  @transient private lazy val playEventSerializer = new SpecificAvroDeserializer[PlayEvent]() {
    {
      configure(Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"), isDeserializerForKeys)
    }
  }

  override def isEndOfStream(nextElement: T): Boolean = false

  override def deserialize(message: Array[Byte]): T = playEventSerializer.deserialize(topicName, message).asInstanceOf[T]

  override def getProducedType: TypeInformation[T] = TypeExtractor.getForClass(clazz)
}