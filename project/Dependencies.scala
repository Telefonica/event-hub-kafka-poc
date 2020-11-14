import sbt._

object Dependencies {
  lazy val kafkaClients = "org.apache.kafka" % "kafka-clients" % "2.6.0"
  lazy val jacksonCore = "com.fasterxml.jackson.core" % "jackson-databind" % "2.11.3"
  
  lazy val scallop = "org.rogach" %% "scallop" % "3.3.1"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.2"

  lazy val log4jApi = "org.apache.logging.log4j" % "log4j-api" % "2.14.0"
  lazy val log4jCore = "org.apache.logging.log4j" % "log4j-core" % "2.14.0"
  lazy val slf4jImpl = "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.14.0"

  val AkkaVersion = "2.6.10"
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % AkkaVersion

  lazy val deps = Seq(
    log4jApi,
    log4jCore,
    slf4jImpl,
    scallop,
    kafkaClients,
    jacksonCore,
    akkaStream,
    
    scalaTest % Test
  )
}
