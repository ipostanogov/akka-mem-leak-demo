package demo

import akka.NotUsed
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior}
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

object Client {

  def main(args: Array[String]): Unit = {
    val _ = ActorSystem(spawnMultiple(), Configs.actorSystemName, Configs.clients)
  }

  private[this] def spawnMultiple(): Behavior[NotUsed] = Behaviors.setup { ctx =>
    for (_ <- 1 to 1000)
      ctx.spawnAnonymous(mainBehavior())
    Behaviors.ignore
  }

  private[this] def mainBehavior(): Behavior[ClientCommand] = Behaviors.setup { ctx =>
    scheduleTick(ctx)
    Behaviors.receiveMessage {
      case Tick =>
        implicit val ec: ExecutionContextExecutor = ctx.system.executionContext
        Future {
          Json.parse(
            """{"name":"Watership Down","location":{"lat":51.235685,"long":-1.309197},
              |"residents":[{"name":"Fiver","age":4,"role":null},{"name":"Bigwig","age":6,"role":"Owsla"}]}""".stripMargin)
        } onComplete { _ => scheduleTick(ctx) }
        Behaviors.same
      case _ =>
        Behaviors.same
    }
  }

  private[this] def scheduleTick(ctx: ActorContext[ClientCommand]) = {
    ctx.scheduleOnce(10.millis, ctx.self, Tick)
  }

  private[this] sealed trait ClientCommand

  private[this] final case object Tick extends ClientCommand

}
