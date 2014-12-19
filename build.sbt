organization := "org.haxors.battlegame"

name := "server"

version := "0.0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.haxors.battlegame" %% "engine" % "0.0.0-SNAPSHOT",
  "org.scalatra" %% "scalatra" % ScalatraVersion,
  "org.scalatra" %% "scalatra-json" % ScalatraVersion,
  "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
  "org.scalatra" %% "scalatra-atmosphere" % "2.3.0",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
  "org.eclipse.jetty" % "jetty-plus" % "9.1.3.v20140225" % "container;provided",
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.8.v20121106" % "container",
  "org.eclipse.jetty.websocket" % "websocket-server" % "9.1.3.v20140225" % "container;provided",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
)

scalaVersion := "2.11.4"
