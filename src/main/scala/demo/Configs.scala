package demo

import com.typesafe.config.{Config, ConfigFactory}

object Configs {
  lazy val clients: Config = root.getConfig("clients")
  lazy val actorSystemName: String = root.getString("actor-system-name")
  private val root = ConfigFactory.load().resolve()
  val instances = 10000
  val awaitTime = 10L
}
