package org.haxors.battlegame.server.controllers

import java.util.UUID
import org.haxors.battlegame.server.models._
import org.json4s._
import org.json4s.JsonDSL._
import org.scalatra.json._
import org.scalatra._
import org.scalatra.atmosphere._
import scala.collection.concurrent._

import scala.concurrent.ExecutionContext.Implicits.global


class BattlegameController extends ScalatraServlet with SessionSupport
  with ScalatraBase with JacksonJsonSupport
  with AtmosphereSupport with JValueResult {

  protected implicit val jsonFormats: Formats = DefaultFormats
  private var players: TrieMap[String, Player] = new TrieMap[String, Player]()

  before() {
    contentType = formats("json")
  }

  post("/api/accesstoken") {
    val token: String = UUID.randomUUID.toString
    val name: String = params("name")
    players += ((name, new Player(token)))
    "accesstoken" -> token
  }

  atmosphere("/ws/lobby") {
    new AtmosphereClient {
      def receive = {
        case Connected =>
          broadcast(("event" -> "PLAYER_JOINED") ~ ("payload" -> ("players" -> players.keys)), Everyone)
      }
    }
  }
}