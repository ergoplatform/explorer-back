package org.ergoplatform.explorer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.db.DB
import org.ergoplatform.explorer.grabber.{OffChainGrabberService, OnChainGrabberService}
import org.ergoplatform.explorer.http.RestApi
import org.ergoplatform.explorer.persistence.TransactionsPool
import pureconfig.loadConfigOrThrow

import scala.concurrent.ExecutionContextExecutor

object ExplorerApp extends IOApp with DB with RestApi {

  implicit val system: ActorSystem = ActorSystem("explorer-system")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  private def program: IO[Unit] = for {
    cfg <- IO(loadConfigOrThrow[ExplorerConfig])
    _ <- if (cfg.db.migrateOnStart) migrate(cfg) else IO.unit
    txPoolRef <- Ref.of[IO, TransactionsPool](TransactionsPool.empty)
    servicesXa <- createTransactor("Explorer-Hikari-Pool", maxPoolSize = 20, maxIdle = 5)(cfg.db)
    grabberXa <- createTransactor("Grabber-Hikari-Pool", maxPoolSize = 5, maxIdle = 3)(cfg.db)
    _ <- startApi(txPoolRef, servicesXa)(cfg)

    onChainGrabberService = new OnChainGrabberService(grabberXa, cfg)(ec)
    offChainGrabberService = new OffChainGrabberService(txPoolRef, cfg)(ec)

    _ <- List(onChainGrabberService.start, offChainGrabberService.start).parSequence.void
  } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)

}
