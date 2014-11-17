package org.haxors.battlegame.server.controllers

import java.util.UUID
import org.haxors.battlegame.server.helpers._
import org.haxors.battlegame.server.models._
import org.json4s._
import org.json4s.jackson.Serialization.write
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
  private val playerService: PlayerService = new PlayerService(players)

  before() {
    contentType = formats("json")
  }

  post("/api/accesstoken") {
    val token: String = UUID.randomUUID.toString
    val name: String = params("name")
    players += ((name, new Player(token, name)))
    "accesstoken" -> token
  }

  atmosphere("/ws/lobby") {
    new AtmosphereClient {
      def receive = {
        case Connected =>
          println("Client %s has connected" format uuid)
        case Disconnected(ClientDisconnected, _) =>
          println("Client %s has disconnected" format uuid)
          val msg = playerService.disconnectPlayer(uuid)
          if (msg != null) {
            broadcast(write(msg), Everyone)
          }
        case Disconnected(ServerDisconnected, _) =>
          println("Client %s has disconnected" format uuid)
          val msg = playerService.disconnectPlayer(uuid)
          if (msg != null) {
            broadcast(write(msg), Everyone)
          }
        case JsonMessage(json) =>
          val player: Player = (json \ "player").extract[Player]
          if (playerService.authenticatePlayer(player)) {
            val event: String = (json \ "event").extract[String]
            event match {
              case "PLAYER_JOINED" =>
                val name = (json \ "payload").extract[String]
                val msg = playerService.addPlayerToLobby(name, uuid)
                broadcast(write(msg), Everyone)
              case "CHAT_MESSAGE" =>
                val chat = (json \ "payload").extract[String]
                val msg = new ChatMessage(new ChatMessagePayload(player.name, chat))
                broadcast(write(msg), Everyone)
            }
          }
          else {
            val msg = UnauthorizedMessage("Invalid Token")
            broadcast(write(msg), Everyone)
          }
      }
    }
  }
}