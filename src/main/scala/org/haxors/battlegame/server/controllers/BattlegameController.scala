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
          disconnectPlayer(uuid)
        case Disconnected(ServerDisconnected, _) =>
          println("Client %s has disconnected" format uuid)
          disconnectPlayer(uuid)
        case JsonMessage(json) =>
          val player: Player = (json \ "player").extract[Player]
          if (playerService.authenticatePlayer(player)) {
            val event: String = (json \ "event").extract[String]
            event match {
              case "PLAYER_JOINED" =>
                addPlayerToLobby(player, uuid)
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

      def disconnectPlayer(uuid: String) = {
        val player: Option[Player] = playerService.getPlayerByUUID(uuid)
        player match {
          case Some(p) =>
            val name = players.remove(p.name).get.name
            val playersInLobby = playerService.getPlayersInLobby
            val payload = new PlayerLeftPayload(name, playersInLobby)
            val msg = new PlayerLeftMessage(payload)
            broadcast(write(msg), Everyone)
          case None =>
        }
      }

      def addPlayerToLobby(player: Player, uuid: String) = {
        if (playerService.getPlayerByUUID(uuid).nonEmpty) {
          val msg = new UnauthorizedMessage("Only one player per connection")
          broadcast(write(msg), Everyone)
        }
        else {
          player.inLobby = true
          player.uuid = uuid
          val msg = new PlayerJoinedMessage(playerService.getPlayersInLobby)
          broadcast(write(msg), Everyone)
        }
      }
    }
  }
}