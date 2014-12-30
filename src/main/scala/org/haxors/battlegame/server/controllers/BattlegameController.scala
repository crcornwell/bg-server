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
    players += ((name, Player(token, name)))
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
          val event: String = (json \ "event").extract[String]
          val jsonPlayer: Player = (json \ "player").extract[Player]
          if (playerService.authenticatePlayer(jsonPlayer, uuid)) {
            val player: Player = players(jsonPlayer.name)
            event match {
              case "PLAYER_JOINED" =>
                addPlayerToLobby(player, uuid)
              case "CHAT_MESSAGE" =>
                val chat = (json \ "payload").extract[String]
                val serialized: String = write(ChatMessage(ChatMessagePayload(player.name, chat)))
                broadcast(TextMessage(serialized), Everyone)
              case "CHALLENGE_RECEIVED" =>
                val challenge = (json \ "payload").extract[ChallengeReceivedPayload]
                if (players.contains(challenge.to)) {
                  val to: Player = players(challenge.to)
                  val msg = ChallengeReceivedMessage(challenge)
                  BroadcasterFactory.getDefault().lookup[Broadcaster](to.uuid)
                    .broadcast(TextMessage(write(msg)))
                }
                else {
                  val msg = write(InvalidMessage("Player %s not found" format challenge.to))
                  broadcast(TextMessage(msg), OnlySelf)
                }
              case "CHALLENGE_ACCEPTED" =>
                val challengePayload: ChallengeAcceptedPayload = (json \ "payload")
                  .extract[ChallengeAcceptedPayload]
                val player1: Player = players(challengePayload.player1)
                val player2: Player = players(challengePayload.player2)
                val playersInGame: Seq[Player] = Seq(player1, player2)
                val engine = new Engine(playersInGame)
                val gameId: String = UUID.randomUUID.toString
                games += ((gameId, engine))
                val msg = ChallengeAcceptedMessage(
                  ChallengeAcceptedPayload(player1.name, player2.name, gameId))
                BroadcasterFactory.getDefault.lookup[Broadcaster](player1.uuid).broadcast(write(msg))
                BroadcasterFactory.getDefault.lookup[Broadcaster](player2.uuid).broadcast(write(msg))
            }
          }
          else {
            val msg = write(UnauthorizedMessage("Invalid token or UUID"))
            broadcast(TextMessage(msg), OnlySelf)
          }
      }

      def disconnectPlayer(uuid: String) = {
        val player: Option[Player] = playerService.getPlayerByUUID(uuid)
        player match {
          case Some(p) =>
            val name = players.remove(p.name).get.name
            val playersInLobby = playerService.getPlayersInLobby
            val payload = PlayerLeftPayload(name, playersInLobby)
            val msg = write(PlayerLeftMessage(payload))
            broadcast(TextMessage(msg), Everyone)
          case None =>
        }
      }

      def addPlayerToLobby(player: Player, uuid: String) = {
        if (playerService.getPlayerByUUID(uuid).nonEmpty) {
          val msg = write(UnauthorizedMessage("Only one player per connection"))
          broadcast(TextMessage(msg), OnlySelf)
        }
        else {
          player.inLobby = Some(true)
          player.uuid = Some(uuid)
          val msg = write(PlayerJoinedMessage(playerService.getPlayersInLobby))
          broadcast(TextMessage(msg), Everyone)
        }
      }
    }
  }

  atmosphere("/ws/game") {
    new AtmosphereClient {
      def receive = {
        case JsonMessage(json) =>
          val jsonPlayer = (json \ "player").extract[Player]
          if (playerService.authenticatePlayer(jsonPlayer, uuid)) {
            val player: Player = players(jsonPlayer.name)
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