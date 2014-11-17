package org.haxors.battlegame.server.helpers

import org.haxors.battlegame.server.models._

import scala.collection.concurrent.TrieMap


class PlayerService(players: TrieMap[String, Player]) {
  def getPlayerByUUID(uuid: String): Option[Player] = {
    players.values.find(_.uuid == uuid)
  }

  def disconnectPlayer(uuid: String): EventMessage = {
    val player: Option[Player] = getPlayerByUUID(uuid)
    player match {
      case Some(p) =>
        val name = players.remove(p.name).get.name
        val playersInLobby = getPlayersInLobby
        val payload = new PlayerLeftPayload(name, playersInLobby)
        new PlayerLeftMessage(payload)
      case None => null
    }
  }

  def addPlayerToLobby(name: String, uuid: String): EventMessage = {
    if (getPlayerByUUID(uuid).nonEmpty) {
      new UnauthorizedMessage("Only one player per connection")
    }
    else {
      val player = players(name)
      player.inLobby = true
      player.uuid = uuid
      new PlayerJoinedMessage(getPlayersInLobby)
    }
  }

  def getPlayersInLobby: Iterable[String] = {
    players.filter(_._2.inLobby).keys
  }

  def authenticatePlayer(player: Player): Boolean = {
    if (players.contains(player.name))
      players(player.name).token == player.token
    else false
  }
}
