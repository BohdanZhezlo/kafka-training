import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.lohika"
ThisBuild / organizationName := "lohika"

lazy val root = (project in file("."))
  .settings(
    name := "kafka-training",
    libraryDependencies ++= Seq(
      kafka,
      akkaActors,
      openCsv,
      akkaHttp,
      akkaStreams,
      akkaHttpSprayJson
    )
  )
