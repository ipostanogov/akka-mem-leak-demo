package demo

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
    val _ = ActorSystem(init(), Configs.actorSystemName, Configs.clients)
  }

  private[this] def init(): Behavior[ClientCommand] = Behaviors.setup { ctx =>
    val listingResponseMapper: ActorRef[Receptionist.Listing] = ctx.messageAdapter(listing => ListingEvent(listing))
    ctx.system.receptionist ! Subscribe(Server.serverServiceKey, listingResponseMapper)
    Behaviors.receive { (ctx, message) =>
      message match {
        case ListingEvent(listing) =>
          listing match {
            case Server.serverServiceKey.Listing(lst) if lst.nonEmpty =>
              for (_ <- 1 to 5000)
                ctx.spawnAnonymous(mainBehavior(lst.map(_.asInstanceOf[ActorRef[Server.ServerCommand]])))
              Behaviors.ignore
            case _ =>
              Behaviors.same
          }
        case _ =>
          Behaviors.same
      }
    }
  }

  private[this] def mainBehavior(exchangeServer: Set[ActorRef[Server.ServerCommand]]): Behavior[ClientCommand] = Behaviors.setup { ctx =>
    scheduleTick(ctx)
    implicit val timeout: Timeout = 1.second
    implicit val scheduler: Scheduler = ctx.system.scheduler
    implicit val ec: ExecutionContextExecutor = ctx.system.executionContext
    Behaviors.receiveMessage {
      case Tick =>
        val multipleInfoF = exchangeServer.map(_ ? Server.GetServerInfo)
        Future.sequence(multipleInfoF.toList) onComplete { _ => scheduleTick(ctx) }
        Behaviors.same
      case _ =>
        Behaviors.same
    }
  }

  private[this] def scheduleTick(ctx: ActorContext[ClientCommand]) = {
    ctx.scheduleOnce(50.millis, ctx.self, Tick)
  }

  private[this] sealed trait ClientCommand

  private[this] final case class ListingEvent(listing: Receptionist.Listing) extends ClientCommand

  private[this] final case object Tick extends ClientCommand

}
