package demo

import java.util.concurrent.ThreadLocalRandom

import akka.NotUsed
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior}
import play.api.libs.json.Json

import scala.concurrent.duration._

object ClientAkka {

  def main(args: Array[String]): Unit = {
    val _ = ActorSystem(spawnMultiple(), Configs.actorSystemName, Configs.clients)
  }

  private[this] def spawnMultiple(): Behavior[NotUsed] = Behaviors.setup { ctx =>
    for (_ <- 1 to Configs.instances)
      ctx.spawnAnonymous(mainBehavior())
    Behaviors.ignore
  }

  private[this] def mainBehavior(): Behavior[ClientCommand] = Behaviors.setup { ctx =>
    ctx.scheduleOnce(ThreadLocalRandom.current().nextLong(Configs.awaitTime).millis, ctx.self, Tick)
    Behaviors.receiveMessage {
      case Tick =>
        Json.parse(
          """{"name":"Watership Down","location":{"lat":51.235685,"long":-1.309197},
            |"residents":[{"name":"Fiver","age":4,"role":null},{"name":"Bigwig","age":6,"role":"Owsla"}]}""".stripMargin)
        scheduleTick(ctx)
        Behaviors.same
      case _ =>
        Behaviors.same
    }
  }

  private[this] def scheduleTick(ctx: ActorContext[ClientCommand]) = {
    ctx.scheduleOnce(Configs.awaitTime.millis, ctx.self, Tick)
  }

  private[this] sealed trait ClientCommand

  private[this] final case object Tick extends ClientCommand

}
