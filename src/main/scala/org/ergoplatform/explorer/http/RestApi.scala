package org.ergoplatform.explorer.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cats.effect.IO
import cats.effect.concurrent.Ref
import com.typesafe.scalalogging.Logger
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.Pools
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.http.handlers._
import org.ergoplatform.explorer.persistence.TransactionsPool
import org.ergoplatform.explorer.services._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait RestApi extends CorsHandler with ErrorHandler {

  implicit val system: ActorSystem
  implicit val mat: ActorMaterializer
  implicit val ec: ExecutionContextExecutor

  def startApi(txPoolRef: Ref[IO, TransactionsPool], xa: Transactor[IO])
              (cfg: ExplorerConfig): IO[Http.ServerBinding] = {

    val logger = Logger("server")

    val host = cfg.http.host
    val port = cfg.http.port

    val servicesEc: ExecutionContextExecutor = ExecutionContext.fromExecutor(Pools.dbCallsFixedThreadPool)

    val blocksService = new BlocksServiceIOImpl[IO](xa, servicesEc)
    val txService = new TransactionsServiceIOImpl[IO](xa, txPoolRef, servicesEc, cfg.grabber)
    val addressesService = new AddressesServiceIOImpl[IO](xa, txPoolRef, servicesEc, cfg.protocol)
    val statsService = new StatsServiceIOImpl[IO](cfg.protocol)(xa, servicesEc)
    val minerService = new MinerServiceIOImpl[IO](xa, servicesEc)

    val handlers = List(
      new BlocksHandler(blocksService).route,
      new TransactionsHandler(txService).route,
      new AddressesHandler(addressesService, txService).route,
      new StatsHandler(statsService).route,
      new ChartsHandler(statsService).route,
      new InfoHandler(statsService).route,
      new SearchHandler(blocksService, txService, addressesService, minerService).route
    )

    val routes: Route = corsHandler(handlers.reduce(_ ~ _))

    IO.fromFuture(IO(Http().bindAndHandle(routes, host, port)))
      .flatMap { b =>
        IO {
          logger.info(s"HTTP server started at ${b.localAddress}")
          b
        }
      }
  }

}
