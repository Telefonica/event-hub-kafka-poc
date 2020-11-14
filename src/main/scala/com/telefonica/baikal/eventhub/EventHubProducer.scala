package com.telefonica.baikal.eventhub

import scala.collection.JavaConverters._
import scala.concurrent.{Future, Promise}

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer._

class EventHubProducer(namespace: String, sasConnection: String) {

  private val config = Map(
    CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG -> s"$namespace.servicebus.windows.net:9093",
    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",
    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.ByteArraySerializer",
  ) ++ EventHubClientConfiguration.getSaslConfig(sasConnection) ++ EventHubClientConfiguration.producer

  val producer = new KafkaProducer[String, Array[Byte]](config.asJava)

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


