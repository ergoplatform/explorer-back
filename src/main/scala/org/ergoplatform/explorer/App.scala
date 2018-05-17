package org.ergoplatform.explorer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger

import scala.concurrent.Await
import scala.concurrent.duration._

object App extends Configuration with DbTransactor with Services with Rest {

  implicit val system = ActorSystem("explorer-system")
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher
  val logger = Logger("server")

  def main(args: Array[String]): Unit = {
    sys.addShutdownHook {
      Await.result(system.terminate(), 2 seconds);
      ()
    }

    implicit def eh: ExceptionHandler = ErrorHandler.exceptionHandler
    implicit def rh: RejectionHandler = ErrorHandler.rejectionHandler
    val host = cfg.http.host
    val port = cfg.http.port

    Http()
      .bindAndHandle(routes, host, port)
      .map { binding => logger.info(s"HTTP server started at ${binding.localAddress}") }
      .recover { case ex => logger.error("Could not start HTTP server", ex) }
    ()
  }
}
