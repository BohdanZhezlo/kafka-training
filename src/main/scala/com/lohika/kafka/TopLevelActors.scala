package com.lohika.kafka

import akka.actor.{ActorRef, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.lohika.kafka.consumer.ConsumerManager

object TopLevelActors extends ExtensionId[TopLevelActors] with ExtensionIdProvider {

  override def lookup(): ExtensionId[_ <: Extension] = TopLevelActors

  override def createExtension(system: ExtendedActorSystem): TopLevelActors = {
    val settings = Settings(system)
    val csvWriter = system.actorOf(SimpleCsvFileWriter.props(settings), "simple-csv-writer")
    val consumerManager = system.actorOf(ConsumerManager.props(settings, csvWriter), "consumer-manager")
    new TopLevelActors(
      csvWriter = csvWriter,
      consumerManager = consumerManager
    )
  }
}

class TopLevelActors(val csvWriter: ActorRef, val consumerManager: ActorRef) extends Extension
