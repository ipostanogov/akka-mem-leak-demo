package demo

import com.typesafe.config.{Config, ConfigFactory}

object Configs {
  lazy val server: Config = root.getConfig("server")
  lazy val clients: Config = root.getConfig("clients")
  lazy val actorSystemName: String = root.getString("actor-system-name")
  private val root = ConfigFactory.load().resolve()
}
