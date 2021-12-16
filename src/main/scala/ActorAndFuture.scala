import akka.actor._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

case object StartConversations
case class End(receivePings: Int)
case class GetPongSum(sum: Option[Int])
case class ThrowException()

object ActorAndFuture extends App {

  val system = ActorSystem("PingPongSystem")

  class pingActor() extends Actor {
    var sum = 0
    val pong: ActorRef = context.actorOf(Props[pongActor], name = "pong")

    def receive: PartialFunction[Any,Unit] = {
      case StartConversation => for (x <- 1 to 10000) pong ! "pingActor"
      case message: String => sum += 1
      case End(s) => println("Sum:" + s)

      case GetPongSum(s) => println(s)
        pong ! GetPongSum(None)
        sender ! End(sum)
        pong ! ThrowException()
        pong ! GetPongSum(None)
        pong ! GetPongSum(Some(sum))

    }
  }

  class pongActor extends Actor {
    var sum = 0
    def doWork(): Int = {
      1
    }

    def receive: PartialFunction[Any,Unit] = {
      case ThrowException() => println(Try(throw new Exception()))
      case End(counter) => println("Counter:" + counter)
      case GetPongSum(value) => println(value)
      case message: String => val future: Future[Int] = Future {
        sum += doWork()
        sum
      }

        Await.result(future, Duration.Inf)
        if (sum < 10000)
          sender ! "pongActor"
        else if (sum == 10000) {
          sender ! End(sum)
          sender ! GetPongSum(Some(sum))
        }
    }
  }

  val ping = system.actorOf(Props[pingActor], name = "pingActor")
  ping ! StartConversation

}