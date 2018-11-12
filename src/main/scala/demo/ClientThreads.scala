package demo

import play.api.libs.json.Json

object ClientThreads {
  def main(args: Array[String]): Unit = {
    val threads = (1 to Configs.instances).map { _ =>
      new Thread {
        override def run(): Unit = {
          while (true) {
            Json.parse(
              """{"name":"Watership Down","location":{"lat":51.235685,"long":-1.309197},
                |"residents":[{"name":"Fiver","age":4,"role":null},{"name":"Bigwig","age":6,"role":"Owsla"}]}""".stripMargin)
            Thread.sleep(Configs.awaitTime)
          }
        }
      }
    }
    threads.foreach(_.start())
    threads.foreach(_.join())
  }
}
