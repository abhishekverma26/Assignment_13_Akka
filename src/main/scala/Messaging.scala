import akka.actor._
import akka.event.{Logging, LoggingAdapter}

case object StartConversation

object Messaging extends App {

  val actorSystem = ActorSystem("PingPongSystem")
  class PongActor extends Actor {
    val log: LoggingAdapter = Logging(context.system, this)

    def receive: PartialFunction[Any,Unit] = {
      case message: String => log.info(message)
        sender ! "Pong"

    }
  }
  class PingActor extends Actor {
    val log: LoggingAdapter = Logging(context.system, this)
    val pongActor: ActorRef = context.actorOf(Props[PongActor], name = "pongActor")

    def receive: PartialFunction[Any,Unit] = {
      case StartConversation => pongActor ! "Ping"
      case message: String => log.info(message)
    }
  }

  val pingActor = actorSystem.actorOf(Props[PingActor], name = "pingActor")
  pingActor ! StartConversation

}
