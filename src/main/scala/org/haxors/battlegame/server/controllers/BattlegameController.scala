package org.haxors.battlegame.server.controllers

import java.util.UUID
import org.json4s._
import org.json4s.JsonDSL._
import org.scalatra.json._
import org.scalatra._
import org.scalatra.atmosphere._


class BattlegameController extends ScalatraServlet
  with ScalatraBase with JacksonJsonSupport
  with AtmosphereSupport with JValueResult {

  protected implicit val jsonFormats: Formats = DefaultFormats

  case class Message(body: String)

  before() {
    contentType = formats("json")
  }

  get("/api/accesstoken") {
    ("accesstoken") -> (UUID.randomUUID.toString)
  }

  atmosphere("/ws/lobby") {
    new AtmosphereClient {
      def receive = {
        case Connected =>
      }
    }
  }
}