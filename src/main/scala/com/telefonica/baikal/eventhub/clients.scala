package com.telefonica.baikal.eventhub

import java.util.UUID
import java.time.{Duration => JDuration}
import scala.collection.JavaConverters._
import scala.concurrent.{Future, Promise, blocking}
import scala.concurrent.ExecutionContext.Implicits.global

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, KafkaConsumer}
import org.apache.kafka.clients.producer._

class EventHubProducer(namespace: String, sasConnection: String) {

  private val config = Map(
    CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG -> s"$namespace.servicebus.windows.net:9093",
    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.ByteArraySerializer",
  ) ++ EventHubClientConfiguration.getSaslConfig(sasConnection) ++ EventHubClientConfiguration.producer

  private val producer = new KafkaProducer[String, Array[Byte]](config.asJava)

  def send(eventhub: String, message: String): Future[RecordMetadata] = {
    val result = Promise[RecordMetadata]()
    producer.send(new ProducerRecord[String, Array[Byte]](eventhub, message.getBytes), new Callback {
      override def onCompletion(metadata: RecordMetadata, ex: Exception): Unit =
        Option(ex) match {
          case None => result.success(metadata)
          case Some(ex) => result.failure(ex)
        }
    })
    result.future
  }


  def partitionsFor(eventhub: String) =
    producer.partitionsFor(eventhub)
      .asScala
      .size

  def close(): Unit = {
    producer.flush()
    producer.close()
  }

}

class EventHubConsumer(namespace: String, eventhub: String, sasConnection: String) {

  private val config = Map(
    CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG -> s"$namespace.servicebus.windows.net:9093",
    ConsumerConfig.GROUP_ID_CONFIG -> UUID.randomUUID().toString,
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer",
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.ByteArrayDeserializer",
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
  ) ++ EventHubClientConfiguration.getSaslConfig(sasConnection) ++ EventHubClientConfiguration.consumer

  private val consumer = {
    val ehc = new KafkaConsumer[String, Array[Byte]](config.asJava)
    ehc.subscribe(Seq(eventhub).asJava)
    ehc
  }

  def read(): Iterable[Array[Byte]] =
    consumer.poll(JDuration.ofSeconds(1))
      .asScala
      .map(_.value())


  def partitionsFor(eventhub: String) =
    consumer.partitionsFor(eventhub)
      .asScala
      .size

  def close(): Unit = {
    consumer.unsubscribe()
    consumer.close()
  }

}


