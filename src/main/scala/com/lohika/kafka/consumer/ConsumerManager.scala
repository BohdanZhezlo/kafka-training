package com.lohika.kafka.consumer

import akka.Done
import akka.actor.{Actor, ActorRef, Props}
import com.lohika.kafka.Settings
import com.lohika.kafka.model.{ConsumerEntries, ConsumerEntry}

class ConsumerManager(settings: Settings, csvWriter: ActorRef) extends Actor {

  import Consumer._
  import ConsumerManager._

  var consumers: Map[Long, ActorRef] = Map.empty
  var id: Long = 0

  override def receive: Receive = {
    case Add =>
      val consumer = context.actorOf(Consumer.props(id, settings, csvWriter), s"kafka-consume-$id")
      consumers = consumers + (id -> consumer)
      consumer ! Start
      id += 1
      sender ! Done

    case Remove(id) =>
      consumers.get(id) match {
        case Some(c) =>
          context.stop(c)
          consumers = consumers - id
          sender ! Some(ConsumerEntry(id))
        case None =>
          sender ! None
      }

    case Get(id) =>
      sender ! consumers.get(id).map(_ => ConsumerEntry(id))

    case GetAll =>
      sender ! ConsumerEntries(consumers.keys.toSeq.sorted.map(id => ConsumerEntry(id)))
  }
}

object ConsumerManager {

  def props(settings: Settings, csvWriter: ActorRef): Props =
    Props(classOf[ConsumerManager], settings, csvWriter)

  case object Add
  case class Remove(id: Int)
  case class Get(id: Int)
  case object GetAll

}
