package com.lohika.kafka.consumer

import akka.Done
import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes.{NotFound, OK}
import akka.http.scaladsl.server.Directives.{complete, delete, get, onSuccess, path, pathPrefix, post, _}
import akka.http.scaladsl.server.PathMatchers.IntNumber
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.lohika.kafka.consumer.ConsumerManager.{Add, Get, GetAll, Remove}
import com.lohika.kafka.model.{ConsumerEntries, ConsumerEntry}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.duration._

object ConsumerRoutes {

  object protocol extends DefaultJsonProtocol {
    implicit val consumerFormat: RootJsonFormat[ConsumerEntry] = jsonFormat1(ConsumerEntry)
    implicit val consumersFormat: RootJsonFormat[ConsumerEntries] = jsonFormat1(ConsumerEntries)
  }

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import protocol._

  implicit val timeout: Timeout = 10.seconds

  def apply(consumerManager: ActorRef): Route = {
    pathPrefix("consumer") {

      path(IntNumber) { id =>
        get {
          onSuccess(consumerManager ? Get(id)) {
            case Some(entry: ConsumerEntry) => complete(entry)
            case None => complete(NotFound)
          }
        } ~ delete {
          onSuccess(consumerManager ? Remove(id)) {
            case Some(_) => complete(OK)
            case None => complete(NotFound)
          }
        }
      } ~ post {
        onSuccess(consumerManager ? Add) {
          case Done => complete(OK)
        }
      }
    } ~ pathPrefix("consumers") {
      get {
        onSuccess(consumerManager ? GetAll) {
          case res: ConsumerEntries => complete(res)
        }
      }
    }
  }
}
