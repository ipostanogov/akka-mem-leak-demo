package demo

import akka.NotUsed
import akka.actor.Scheduler
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.Receptionist.Subscribe
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

object Client {

  def main(args: Array[String]): Unit = {
    val _ = ActorSystem(spawnMultiple(), Configs.actorSystemName, Configs.clients)
  }

  private[this] def spawnMultiple(): Behavior[NotUsed] = Behaviors.setup { ctx =>
    for (_ <- 1 to 1000)
      ctx.spawnAnonymous(init())
    Behaviors.ignore
  }

  private[this] def init(): Behavior[ClientCommand] = Behaviors.setup { ctx =>
    val listingResponseMapper: ActorRef[Receptionist.Listing] = ctx.messageAdapter(listing => ListingEvent(listing))
    ctx.system.receptionist ! Subscribe(Server.serverServiceKey, listingResponseMapper)
    setup(None)
  }

  private[this] def setup(server: Option[ActorRef[Server.ServerCommand]]): Behavior[ClientCommand] = Behaviors.receive { (ctx, message) =>
    message match {
      case ListingEvent(listing) =>
        listing match {
          case Server.serverServiceKey.Listing(lst) if lst.nonEmpty =>
            scheduleTick(ctx)
            mainBehavior(lst.map(_.asInstanceOf[ActorRef[Server.ServerCommand]]))
          case _ =>
            Behaviors.same
        }
      case _ =>
        Behaviors.same
    }
  }

  private[this] def mainBehavior(exchangeServer: Set[ActorRef[Server.ServerCommand]]): Behavior[ClientCommand] = Behaviors.setup { ctx =>
    implicit val timeout: Timeout = 1.second
    implicit val scheduler: Scheduler = ctx.system.scheduler
    implicit val ec: ExecutionContextExecutor = ctx.system.executionContext
    Behaviors.receiveMessage {
      case Tick =>
        val multipleInfoF = exchangeServer.map(_ ? Server.GetServerInfo)
        Future.sequence(multipleInfoF) onComplete { _ => scheduleTick(ctx) }
        Behaviors.same
      case _ =>
        Behaviors.same
    }
  }

  private[this] def scheduleTick(ctx: ActorContext[ClientCommand]) = {
    ctx.scheduleOnce(20.millis, ctx.self, Tick)
  }

  private[this] sealed trait ClientCommand

  private[this] final case class ListingEvent(listing: Receptionist.Listing) extends ClientCommand

  private[this] final case object Tick extends ClientCommand

}
