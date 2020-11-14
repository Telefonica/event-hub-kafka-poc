package com.telefonica.baikal

import java.nio.file.{Files, Path, Paths}
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{FileIO, Framing}
import akka.util.ByteString
import com.telefonica.baikal.eventhub.EventHubProducer
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
        ???
      case "producer" =>
        val bufferSize = 20000
        val producer = new EventHubProducer(namesapce, sasConnection)
        for {
          _ <- {
            FileIO.fromPath(Args.inputData())
              .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 1000000, allowTruncation = true).map(_.utf8String))
              .mapAsyncUnordered(bufferSize)(producer.send(evethub, _))
              .withAttributes(Attributes.inputBuffer(initial = bufferSize, max = bufferSize))
              .withAttributes(ActorAttributes.supervisionStrategy(ex => {
                logger.error("An error occurred in the main flow", ex)
                Supervision.Stop
              }))
              .run()
          }
        } yield producer.close()
      case mode => throw new IllegalArgumentException(s"Unknown mode option '$mode'")
    }
  }.onComplete(_ => system.terminate())

}
