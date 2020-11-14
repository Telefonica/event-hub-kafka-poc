package com.telefonica.baikal.eventhub

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs

object EventHubClientConfiguration {

  /**
    * Microsoft recommended configuration https://github.com/Azure/azure-event-hubs-for-kafka/blob/master/CONFIGURATION.md
    * */
  val producer: Map[String, Object] = {
    val requestTimeout = 60000
    val linger = 100
    val retries = 300000
    Map(
      ProducerConfig.METADATA_MAX_AGE_CONFIG -> "180000",
      ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG -> "180000",
      ProducerConfig.LINGER_MS_CONFIG -> s"$linger",
      ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG -> s"$requestTimeout",
      ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG -> s"${(requestTimeout + linger) * retries}", // (request.timeout.ms + linger.ms) * retries (MAX allowed: 2147483647)
      ProducerConfig.RETRIES_CONFIG -> s"$retries",
      ProducerConfig.MAX_REQUEST_SIZE_CONFIG -> "1000000",
      ProducerConfig.METADATA_MAX_AGE_CONFIG -> "60000",
      ProducerConfig.METADATA_MAX_IDLE_CONFIG -> "180000",
      ProducerConfig.COMPRESSION_TYPE_CONFIG -> "none",
      ProducerConfig.ACKS_CONFIG -> "all",
      ProducerConfig.BATCH_SIZE_CONFIG -> "1000000"
    )
  }

  val consumer: Map[String, Object] = Map(
    ConsumerConfig.METADATA_MAX_AGE_CONFIG -> "180000",
    ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG -> "180000",
    ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG -> "30000",
    ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG -> "3000"
  )

  def getSaslConfig(sasConnection: String): Map[String, Object] = {
    val username = "$ConnectionString"
    val jaas = s"""org.apache.kafka.common.security.plain.PlainLoginModule required username="$username" password="$sasConnection";"""
    Map(
      CommonClientConfigs.SECURITY_PROTOCOL_CONFIG -> "SASL_SSL",
      SaslConfigs.SASL_MECHANISM -> "PLAIN",
      SaslConfigs.SASL_JAAS_CONFIG -> jaas
    )
  }

}
