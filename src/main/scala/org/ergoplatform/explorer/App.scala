package org.ergoplatform.explorer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import org.ergoplatform.explorer.config.ExplorerConfig
import pureconfig.loadConfigOrThrow

import scala.concurrent.Await
import scala.concurrent.duration._

object App extends Setup with Rest with Configuration {

  override implicit val system = ActorSystem("explorer-system")
  override implicit val mat = ActorMaterializer()
  override implicit val ec = system.dispatcher

  val logger = Logger("server")
  override val cfg: ExplorerConfig = loadConfigOrThrow[ExplorerConfig]

  def main(args: Array[String]): Unit = {
    sys.addShutdownHook {
      Await.result(system.terminate(), 2 seconds);
      ()
    }

    implicit def eh: ExceptionHandler = ErrorHandler.exceptionHandler
    val host = cfg.http.host
    val port = cfg.http.port

    Http()
      .bindAndHandle(routes, host, port)
      .map { binding => logger.info(s"HTTP server started at ${binding.localAddress}") }
      .recover { case ex => logger.error("Could not start HTTP server", ex) }
    ()
  }
}
