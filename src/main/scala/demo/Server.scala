package demo

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

object Server {
  val serverServiceKey = ServiceKey("Server")

  def main(args: Array[String]): Unit = {
    val _ = ActorSystem(init(), Configs.actorSystemName, Configs.server)
  }

  private def init(): Behavior[ServerCommand] = Behaviors.setup { ctx =>
    ctx.system.receptionist ! Receptionist.Register(serverServiceKey, ctx.self)
    mainBehavior()
  }

  private[this] def mainBehavior(): Behavior[ServerCommand] = Behaviors.receiveMessage {
    case GetServerInfo(replyTo) =>
      replyTo ! ServerInfo()
      Behaviors.same
  }

  sealed trait ServerCommand

  final case class ServerInfo(info: String = "FOO")

  final case class GetServerInfo(replyTo: ActorRef[ServerInfo]) extends ServerCommand

}
