package com.lohika.kafka.consumer

import java.time.Duration
import java.util
import java.util.Properties

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.lohika.kafka.Settings
import com.lohika.kafka.SimpleCsvFileWriter.WriteMultiple
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer

import scala.jdk.CollectionConverters._

class Consumer(id: Long, settings: Settings, csvWriter: ActorRef) extends Actor with ActorLogging {

  import Consumer._

  val consumer: KafkaConsumer[String, String] = new KafkaConsumer(createProperties)

  override def postStop(): Unit = {
    log.info(s"Consumer $id, Thread: ${Thread.currentThread().getName} - Stopping")
    super.postStop()
    terminate()
  }

  override def receive: Receive = {
    case Start =>
      log.info(s"Consumer $id, Thread: ${Thread.currentThread().getName} - Starting")
      consumer.subscribe(util.Arrays.asList(settings.consumer.topic))
      self ! Poll

    case Poll =>
      log.info(s"Consumer $id, Thread: ${Thread.currentThread().getName} - Polling")
      poll()

    case d: ProcessData =>
      log.info(s"Consumer $id, Thread: ${Thread.currentThread().getName} - Processing ${d.data.size} records")
      processData(d)
      self ! Poll
  }

  def terminate(): Unit = {
    try {
      consumer.wakeup()
      consumer.unsubscribe()
      consumer.close()
    } catch {
      case t: Throwable =>
        log.warning(s"Consumer $id, Thread: ${Thread.currentThread().getName} - Error while terminating consumer: {}", t)
    }
  }

  def poll(): Unit = {
    try {
      val records = consumer.poll(Duration.ofSeconds(5))
      log.info(s"Consumer $id, Thread: ${Thread.currentThread().getName} - Fetched ${records.count()} records")
      val data = ProcessData(records.asScala.map { record =>
        Data(record.partition(), record.offset(), record.value())
      }.toSeq)
      self ! data
    } catch {
      case t: Throwable =>
        log.warning(s"Consumer $id, Thread: ${Thread.currentThread().getName} - Error while polling: {}", t)
        self ! ProcessData(Seq.empty)
    }
  }

  def processData(data: ProcessData): Unit = {
    if (data.data.nonEmpty) {
      val toWrite = data.data.map { d =>
        Array(
          id.toString,
          d.partition.toString,
          d.offset.toString,
          d.value
        )
      }
      csvWriter ! WriteMultiple(toWrite)
    }
  }

  def createProperties: Properties = {
    val properties = new Properties()
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.consumer.server)
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, settings.consumer.groupId)
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    properties
  }
}

object Consumer {

  def props(id: Long, settings: Settings, csvWriter: ActorRef): Props =
    Props(classOf[Consumer], id, settings, csvWriter)

  case object Start
  case object Poll
  case class ProcessData(data: Seq[Data])
  case class Data(partition: Int, offset: Long, value: String)

}
