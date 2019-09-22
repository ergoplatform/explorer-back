package org.ergoplatform.explorer.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cats.effect.concurrent.Ref
import cats.effect.{IO, Resource}
import cats.implicits._
import com.typesafe.scalalogging.Logger
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.http.handlers._
import org.ergoplatform.explorer.persistence.TransactionsPool
import org.ergoplatform.explorer.services._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait RestApi extends CorsHandler with ErrorHandler {

  final def startApi(
    txPoolRef: Ref[IO, TransactionsPool],
    xa: Transactor[IO],
    servicesEc: ExecutionContext,
    cfg: ExplorerConfig
  )(implicit system: ActorSystem): Resource[IO, Http.ServerBinding] = {

    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val logger = Logger("server")

    val host = cfg.http.host
    val port = cfg.http.port

    val blocksService = new BlocksServiceImpl[IO](xa, servicesEc)
    val txService = new TransactionsServiceImpl[IO](xa, txPoolRef, servicesEc, cfg)
    val addressesService = new AddressesServiceImpl[IO](xa, txPoolRef, servicesEc, cfg.protocol)
    val statsService = new StatsServiceImpl[IO](cfg.protocol)(xa, servicesEc)
    val minerService = new MinerServiceImpl[IO](xa, servicesEc)

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

    Resource.make(
      IO.fromFuture(IO(Http().bindAndHandle(routes, host, port)))
        .flatMap { b =>
          IO(logger.info(s"HTTP server started at ${b.localAddress}")) *> IO(b)
        }
    )(x => IO.fromFuture(IO(x.unbind())).as(()))
  }

}
