package org.haxors.battlegame.server.controllers

import java.util.UUID
import org.atmosphere.cpr.{Broadcaster, BroadcasterFactory}
import org.haxors.battlegame.server.helpers._
import org.haxors.battlegame.engine._
import org.haxors.battlegame.model._
import org.haxors.battlegame.server.models._
import org.json4s._
import org.json4s.jackson.Serialization.write
import org.scalatra.json._
import org.scalatra._
import org.scalatra.atmosphere._
import scala.collection.concurrent._

import scala.concurrent.ExecutionContext.Implicits.global


class BattlegameController extends ScalatraServlet with SessionSupport
  with ScalatraBase with JacksonJsonSupport with CorsSupport
  with AtmosphereSupport with JValueResult {

  protected implicit val jsonFormats: Formats = DefaultFormats
  private var players: TrieMap[String, Player] = new TrieMap[String, Player]()
  private var games: TrieMap[String, Engine] = new TrieMap[String, Engine]()
  private val playerService: PlayerService = new PlayerService(players)

  before() {
    contentType = formats("json")
  }

  options("/*") {
    response.setHeader("Access-Control-Allow-Headers",
      request.getHeader("Access-Control-Request-Headers"))
  }

  post("/api/accesstoken") {
    val token: String = UUID.randomUUID.toString
    val name: String = params("username")
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
          if (playerService.authenticatePlayer(player, uuid)) {
            val event: String = (json \ "event").extract[String]
            event match {
              case "PLAYER_JOINED" =>
                addPlayerToLobby(player, uuid)
              case "CHAT_MESSAGE" =>
                val chat = (json \ "payload").extract[String]
                val serialized: String = write(ChatMessage(ChatMessagePayload(player.name, chat)))
                broadcast(TextMessage(serialized), Everyone)
              case "CHALLENGE_RECEIVED" =>
                val challenge = (json \ "payload").extract[ChallengeReceivedPayload]
                val to: Player = players(challenge.to)
                val msg = new ChallengeReceivedMessage(challenge)
                BroadcasterFactory.getDefault().lookup[Broadcaster](to.uuid).broadcast(write(msg))
              case "CHALLENGE_ACCEPTED" =>
                val challengePayload: ChallengeAcceptedPayload = (json \ "payload").extract[ChallengeAcceptedPayload]
                val player1: Player = players(challengePayload.player1)
                val player2: Player = players(challengePayload.player2)
                val playersInGame: Seq[Player] = Seq(player1, player2)
                val engine = new Engine(playersInGame)
                val gameId: String = UUID.randomUUID.toString
                games += ((gameId, engine))
                val msg = new ChallengeAcceptedMessage(
                  new ChallengeAcceptedPayload(player1.name, player2.name, gameId))
                BroadcasterFactory.getDefault.lookup[Broadcaster](player1.uuid).broadcast(write(msg))
                BroadcasterFactory.getDefault.lookup[Broadcaster](player2.uuid).broadcast(write(msg))
            }
          }
          else {
            val msg = UnauthorizedMessage("Invalid Token")
            broadcast(write(msg), OnlySelf)
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

  atmosphere("/ws/game") {
    new AtmosphereClient {
      def receive = {
        case JsonMessage(json) =>
          val player: Player = (json \ "player").extract[Player]
          if (playerService.authenticatePlayer(player, uuid)) {
            val gameId: String = (json \ "gameid").extract[String]
            val game: Engine = games(gameId)
            if (game.authenticate(player)) {
              val event: String = (json \ "event").extract[String]
              event match {
                case "GAME_START" =>

              }
            }
          }

      }
    }
  }
}