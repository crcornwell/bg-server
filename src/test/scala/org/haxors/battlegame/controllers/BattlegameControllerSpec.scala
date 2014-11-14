package org.haxors.battlegame.controllers

import org.scalatra.test.specs2._

class BattlegameControllerSpec extends MutableScalatraSpec {
  addServlet(classOf[BattlegameController], "/*")

  "GET / on BattlegameController" should {
    "return status 200" in {
      get("/") {
        status must_== 200
      }
    }
  }
  "GET /messages on BattlegameController" should {
  	"return status 200" in {
  	  get("/messages") {
      	status must_== 200
      }
  	}
  }
}