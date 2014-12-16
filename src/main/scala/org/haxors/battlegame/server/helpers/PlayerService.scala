package org.haxors.battlegame.server.helpers

import org.haxors.battlegame.model._

import scala.collection.concurrent.TrieMap


class PlayerService(players: TrieMap[String, Player]) {
  def getPlayerByUUID(uuid: String): Option[Player] = {
    players.values.find(_.uuid == uuid)
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
