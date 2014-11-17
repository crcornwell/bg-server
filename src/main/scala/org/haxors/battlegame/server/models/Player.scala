package org.haxors.battlegame.server.models


class Player(val token: String, val name: String) {
  var uuid: String = null
  var inLobby: Boolean = false
}
