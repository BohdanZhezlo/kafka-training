package com.lohika.kafka

import java.io._

import akka.actor.{Actor, Props}
import com.lohika.kafka.SimpleCsvFileWriter.{FlushAndClose, WriteSingle, WriteMultiple}
import com.opencsv.CSVWriter

import scala.util.Try

class SimpleCsvFileWriter(settings: Settings) extends Actor {

  var csvWriter: CSVWriter = _

  override def preStart(): Unit = {
    csvWriter = new CSVWriter(new FileWriter(new File(settings.csv.destFileName)))
    write(settings.csv.headers)
  }

  override def postStop(): Unit = Try {
    csvWriter.flush()
    csvWriter.close()
  }

  def receive: Receive = {
    case WriteSingle(content) =>
      write(content)
    case WriteMultiple(contents) =>
      contents.foreach { content =>
        csvWriter.writeNext(content)
      }
      csvWriter.flushQuietly()
    case FlushAndClose =>
      context.stop(self)
  }

  def write(data: Array[String]): Unit = {
    csvWriter.writeNext(data)
    csvWriter.flushQuietly()
  }
}

object SimpleCsvFileWriter {

  def props(settings: Settings) =
    Props(classOf[SimpleCsvFileWriter], settings).withDispatcher("file-writer-dispatcher")

  case class WriteSingle(content: Array[String])
  case class WriteMultiple(contents: Seq[Array[String]])
  case object FlushAndClose

}
