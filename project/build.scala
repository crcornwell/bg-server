import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._

object ServerBuild extends Build {

  val ScalatraVersion = "2.4.0.M2"

  lazy val project = Project(
    "server",
    file("."),
    settings = Defaults.defaultSettings ++ com.earldouglas.xsbtwebplugin.WebPlugin.webSettings
  )

}
