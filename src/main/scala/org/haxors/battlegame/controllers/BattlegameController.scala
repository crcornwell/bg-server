package org.haxors.battlegame.controllers

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.scalatra._


class BattlegameController extends ScalatraServlet with ScalatraBase with JacksonJsonSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats

  case class Message(body: String)

  before() {
    contentType = formats("json")
  }

  get("/") {
  	contentType = "text/html"
  	"""
  	<p>Check out RESTful API at <a href="http://localhost:8080/messages">http://localhost:8080/messages</a>
  	"""
  }

  
  get("/messages") {
    List(Message("Great job!"), Message("Scalatra project generated by Yeoman!"))
  }
  
}