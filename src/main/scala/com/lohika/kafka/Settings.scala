package com.lohika.kafka

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config

import scala.jdk.CollectionConverters._

class Settings(config: Config) extends Extension {

  object http {
    private val c = config.getConfig("kafka-course.http")
    val interface: String = c.getString("interface")
    val port: Int = c.getInt("port")
  }

  object csv {
    private val c = config.getConfig("kafka-course.csv")
    val destFileName: String = c.getString("file-name")
    val headers: Array[String] = c.getStringList("headers").asScala.toArray[String]
  }

  object consumer {
    private val c = config.getConfig("kafka-course.consumer")
    val topic: String = c.getString("topic")
    val groupId: String = c.getString("group-id")
    val server: String = c.getString("server")
  }

}

object Settings extends ExtensionId[Settings] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): Settings =
    new Settings(system.settings.config)

  override def lookup(): ExtensionId[_ <: Extension] = Settings
}

