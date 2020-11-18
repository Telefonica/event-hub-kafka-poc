package com.telefonica.baikal

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{FileIO, Framing, Sink, Source}
import akka.util.ByteString
import com.telefonica.baikal.eventhub.{EventHubConsumer, EventHubProducer}
import org.apache.kafka.clients.producer.{Callback, ProducerRecord, RecordMetadata}
import org.rogach.scallop.{ScallopConf, ScallopOption, ValueConverter, singleArgConverter}
import org.apache.logging.log4j.LogManager


object EventHubPOC extends App {

  object Args extends ScallopConf(args) {

    implicit def pathConverter: ValueConverter[Path] = singleArgConverter[Path](Paths.get(_))

    val mode: ScallopOption[String] = choice(Seq("consumer", "producer"), required = true, name = "mode", descr = "Consume or produce messages to the desired EventHub")
    val namespace: ScallopOption[String] = opt[String](required = true, name = "namespace", descr = "EventHub namespace name")
    val eventhub: ScallopOption[String] = opt[String](required = true, name = "eventhub", descr = "EventHub name")
    val sasConnection: ScallopOption[String] = opt[String](required = true, name = "sas", descr = "SAS connection string between \"\"").map(_.replaceAll("\"", ""))
    val inputData: ScallopOption[Path] = opt[Path](required = false, name = "input", descr = "Input data file", validate = path => Files.exists(path))
    val outputData: ScallopOption[Path] = opt[Path](required = false, name = "output", descr = "Output data file")

    banner("EventHub consumer/producer using Kafka interface")
    footer(
      """
        |- Producer example: --mode=producer --namespace=ns-test --eventhub=eh-test --sas="Endpoint=sb://ns-test.servicebus.windows.net/;SharedAccessKeyName=sas-keys;SharedAccessKey=xxx;EntityPath=eh-test" --input=/path/to/data/1gb.json
        |- Consumer example: --mode=consumer --namespace=ns-test --eventhub=eh-test --sas="Endpoint=sb://ns-test.servicebus.windows.net/;SharedAccessKeyName=sas-keys;SharedAccessKey=xxx;EntityPath=eh-test" --output=/path/to/data/out.json
      """.stripMargin)
    verify()
  }

  private val logger = LogManager.getLogger(getClass)

  private val namesapce = Args.namespace()
  private val evethub = Args.eventhub()
  private val sasConnection = Args.sasConnection()
  private val mode = Args.mode()

  private implicit val system: ActorSystem = ActorSystem("eventhub-poc")
  private implicit val materializer: Materializer = Materializer(system)

  {
    mode match {
      case "consumer" =>
        val output = Option(Args.outputData()).getOrElse(throw new IllegalArgumentException("--output path is mandatory in consumer mode"))
        val consumer = new EventHubConsumer(namesapce, evethub, sasConnection)
        for {
          _ <- Future.successful(())
          _ <- {
            Source.tick(initialDelay = 0 seconds, interval = 1 second, NotUsed)
              .map(_ => consumer.read())
              .flatMapConcat(messages => {
                Source(messages.map(bytes => ByteString(bytes) ++ ByteString(System.lineSeparator)).toList)
              })
              .map(Some(_))
              .keepAlive(1 minute, () => None)
              .takeWhile(_.isDefined)
              .map(_.get)
              .withAttributes(ActorAttributes.supervisionStrategy(ex => {
                logger.error("An error occurred in the main flow", ex)
                Supervision.Stop
              }))
              .runWith(FileIO.toPath(output))
          }
        } yield consumer.close()
      case "producer" =>
        val input = Option(Args.inputData()).getOrElse(throw new IllegalArgumentException("--input path is mandatory in producer mode"))
        val bufferSize = 200000
        val producer = new EventHubProducer(namesapce, sasConnection)
        for {
          _ <- {
            FileIO.fromPath(input)
              .via(Framing.delimiter(ByteString(System.lineSeparator), maximumFrameLength = 1000000, allowTruncation = true))
              .map(_.utf8String)
              .map { msg =>
                try {
                  producer.producer.send(new ProducerRecord[String, Array[Byte]](evethub, msg.getBytes), (metadata: RecordMetadata, ex: Exception) => {
                    if (ex != null) logger.error("ERROR", ex)
                  })
                } catch {
                  case e: Throwable => logger.error("ERROR SEND", e)
                }
              }
              .run()
          }
        } yield producer.close()
      case mode => throw new IllegalArgumentException(s"Unknown mode option '$mode'")
    }
  }.onComplete(_ => system.terminate())

}
