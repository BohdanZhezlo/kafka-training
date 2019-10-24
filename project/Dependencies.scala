import sbt._

object Dependencies {
  lazy val kafka = "org.apache.kafka" % "kafka-clients" % "2.3.0"
  lazy val akkaActors = "com.typesafe.akka" %% "akka-actor" % "2.5.25"
  lazy val openCsv = "com.opencsv" % "opencsv" % "4.6"
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.1.10"
  lazy val akkaStreams = "com.typesafe.akka" %% "akka-stream" % "2.5.25"
  lazy val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10"
}
