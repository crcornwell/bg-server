package org.haxors.battlegame.server.helpers

import org.haxors.battlegame.model._

import scala.collection.concurrent.TrieMap


class PlayerService(players: TrieMap[String, Player]) {
  def getPlayerByUUID(uuid: String): Option[Player] = {
    players.values.find(_.uuid == uuid)
  }

  def getPlayersInLobby: Iterable[String] = {
    players.filter(_._2.inLobby.getOrElse(false)).keys
  }

  def authenticatePlayer(player: Player, uuid: String): Boolean = {
    val p = players.get(player.name)
    p match {
      case Some(p: Player) =>
        p.token == player.token && (p.uuid == None || p.uuid == uuid)
      case None => false
    }
  }
}
