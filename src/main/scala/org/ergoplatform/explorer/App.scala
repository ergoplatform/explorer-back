package org.ergoplatform.explorer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import doobie.hikari.implicits._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object App extends Configuration with DbTransactor with Services with Rest {

  implicit val system = ActorSystem("explorer-system")
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher
  val logger = Logger("server")

  def main(args: Array[String]): Unit = {

    implicit def eh: ExceptionHandler = ErrorHandler.exceptionHandler
    implicit def rh: RejectionHandler = ErrorHandler.rejectionHandler
    val host = cfg.http.host
    val port = cfg.http.port

    val binding = Http().bindAndHandle(routes, host, port)

    binding.onComplete {
      case Success(b) =>
        logger.info(s"HTTP server started at ${b.localAddress}")
      case Failure(ex) =>
        logger.error("Could not start HTTP server", ex)
    }

    sys.addShutdownHook {
      val stop = binding.flatMap { x => x.unbind() }
      stop.onComplete { _ =>
        Pools.shutdown
        transactor.shutdown.unsafeRunSync()
        system.terminate()
      }
      Await.result(stop, 5 seconds)
      ()
    }
  }
}
