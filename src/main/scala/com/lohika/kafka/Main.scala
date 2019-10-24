package com.lohika.kafka

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.http.scaladsl.settings.ServerSettings
import com.lohika.kafka.consumer.ConsumerRoutes

object Main extends App {

  implicit val system = ActorSystem("kafka-course")
  implicit val executionContext = system.dispatcher

  val settings = Settings(system)
  val actors = TopLevelActors(system)

  new RestApi(actors).startServer(
    settings.http.interface,
    settings.http.port,
    ServerSettings(system.settings.config),
    system
  )
}

class RestApi(actors: TopLevelActors)(implicit val system: ActorSystem) extends HttpApp {

  override protected def routes: Route = {
    ConsumerRoutes(actors.consumerManager)
  }

}
