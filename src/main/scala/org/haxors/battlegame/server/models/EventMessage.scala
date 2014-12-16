package org.haxors.battlegame.server.models

sealed abstract class EventMessage

case class PlayerLeftMessage(payload: PlayerLeftPayload, event: String = "PLAYER_LEFT")
  extends EventMessage

case class PlayerLeftPayload(name: String, players: Iterable[String])

case class PlayerJoinedMessage(payload: Iterable[String], event: String = "PLAYER_JOINED")
  extends EventMessage

case class PlayerJoinedPayload(name: String)

case class ChatMessage(payload: ChatMessagePayload, event: String = "CHAT_MESSAGE")
  extends EventMessage

case class ChatMessagePayload(from: String, message: String)

case class ChallengeReceivedMessage(payload: ChallengeReceivedPayload, event: String = "CHALLENGE_RECEIVED")
  extends EventMessage

case class ChallengeReceivedPayload(from: String, to: String)

case class ChallengeAcceptedMessage(payload: ChallengeAcceptedPayload, event: String = "CHALLENGE_ACCEPTED")
  extends EventMessage

case class ChallengeAcceptedPayload(player1: String, player2: String, gameId: String)

case class UnauthorizedMessage(payload: String, event: String = "UNAUTHORIZED_MESSAGE")
  extends EventMessage