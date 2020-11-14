import Dependencies._

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        organization := "com.telefonica.baikal",
        version := "0.1.0-SNAPSHOT",
        scalaVersion := "2.12.12",
        updateOptions := updateOptions.value.withCachedResolution(true)
      )
    ),
    libraryDependencies ++= deps,
    name := "event-hub-kafka-poc"
  )

publishMavenStyle in ThisBuild := true
assemblyOutputPath in assembly := new File(s"dist/thor-analyser.jar")
assemblyMergeStrategy in assembly := {
  case "reference.conf" => MergeStrategy.concat
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", "services", xs@_*) => MergeStrategy.concat
  case PathList("META-INF", xs) if xs.endsWith(".SF") || xs.endsWith(".DSA") || xs.endsWith(".RSA") => MergeStrategy.discard
  case PathList("META-INF", xs@_*) => MergeStrategy.first
  case _ => MergeStrategy.first
}


