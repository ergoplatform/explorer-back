package org.ergoplatform.explorer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route}
import akka.stream.ActorMaterializer
import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.scalalogging.Logger
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.config.{DbConfig, ExplorerConfig}
import org.ergoplatform.explorer.grabber.{OffChainGrabberService, OnChainGrabberService}
import org.ergoplatform.explorer.http.handlers._
import org.ergoplatform.explorer.http.{CorsHandler, ErrorHandler}
import org.ergoplatform.explorer.persistence.TransactionsPool
import org.ergoplatform.explorer.services._
import org.flywaydb.core.Flyway
import pureconfig.loadConfigOrThrow

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object ExplorerApp extends IOApp with CorsHandler {


  implicit val system: ActorSystem = ActorSystem("explorer-system")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val logger = Logger("server")

  private def program: IO[Unit] = for {
    cfg <- IO(loadConfigOrThrow[ExplorerConfig])
    _ <- if (cfg.db.migrateOnStart) migrate(cfg) else IO.unit
    txPoolRef <- Ref.of[IO, TransactionsPool](TransactionsPool.empty)
    servicesXa <- createTransactor("Explorer-Hikari-Pool", maxPoolSize = 20, maxIdle = 5)(cfg.db)
    grabberXa <- createTransactor("Grabber-Hikari-Pool", maxPoolSize = 5, maxIdle = 3)(cfg.db)
    _ <- startApi(txPoolRef, servicesXa)(cfg)

    onChainEc = ExecutionContext.fromExecutor(Pools.offChainMonitoringPool)
    offChainEc = ExecutionContext.fromExecutor(Pools.offChainMonitoringPool)
    onChainGrabberService = new OnChainGrabberService(grabberXa, cfg)(onChainEc)
    offChainGrabberService = new OffChainGrabberService(txPoolRef, cfg)(offChainEc)

    _ <- List(onChainGrabberService.start, offChainGrabberService.start).parSequence.void
  } yield ()

  def migrate(cfg: ExplorerConfig): IO[Unit] = for {
    flyway <- IO {
      val f = new Flyway()
      f.setSqlMigrationSeparator("__")
      f.setLocations("classpath:db")
      f.setDataSource(cfg.db.url, cfg.db.user, cfg.db.pass)
      f
    }
    _ <- IO(flyway.clean())
    _ <- IO(flyway.migrate())
  } yield ()

  def createTransactor(name: String, maxPoolSize: Int, maxIdle: Int)
                      (cfg: DbConfig): IO[HikariTransactor[IO]] =
    for {
      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = cfg.driverClassName,
        url = cfg.url,
        user = cfg.user,
        pass = cfg.pass
      )
      _ <- xa.configure(c => IO {
        c.setPoolName(name)
        c.setAutoCommit(false)
        c.setMaximumPoolSize(maxPoolSize)
        c.setMinimumIdle(maxIdle)
        c.setMaxLifetime(1200000L)
      })
    } yield xa

  def startApi(txPoolRef: Ref[IO, TransactionsPool], xa: Transactor[IO])
              (cfg: ExplorerConfig): IO[Http.ServerBinding] = {

    implicit def eh: ExceptionHandler = ErrorHandler.exceptionHandler
    implicit def rh: RejectionHandler = ErrorHandler.rejectionHandler

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

  override def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)

}
