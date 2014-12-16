import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._

object ServerBuild extends Build {
  val Organization = "org.haxors"
  val Name = "battlegame-server"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.10.3"
  val ScalatraVersion = "2.2.2"

  lazy val project = Project (
    "battlegame-server",
    file("."),
    settings = Defaults.defaultSettings ++ com.earldouglas.xsbtwebplugin.WebPlugin.webSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "org.scalatra" %% "scalatra-atmosphere" % "2.3.0",
        "org.json4s" %% "json4s-jackson" % "3.1.0",
        "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
        "org.eclipse.jetty" % "jetty-plus" % "9.1.3.v20140225" % "container;provided",
        "org.eclipse.jetty" % "jetty-webapp" % "8.1.8.v20121106" % "container",
        "org.eclipse.jetty.websocket" % "websocket-server" % "9.1.3.v20140225" % "container;provided",
        "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
      )
    )
  ).dependsOn(gameEngine).dependsOn(common)

  lazy val gameEngine = RootProject(uri("git://github.com/0xfadedace/battlegame-engine.git"))
  lazy val common = RootProject(uri("git://github.com/0xfadedace/battlegame-common.git"))
}
