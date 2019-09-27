package org.ergoplatform.explorer

import akka.actor.ActorSystem
import cats.effect.concurrent.Ref
import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import doobie.util.ExecutionContexts
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.db.DB
import org.ergoplatform.explorer.grabber.{LoopedIO, OffChainGrabberService, OnChainGrabberService}
import org.ergoplatform.explorer.http.RestApi
import org.ergoplatform.explorer.persistence.TransactionsPool
import pureconfig.loadConfigOrThrow

object App extends IOApp with DB with RestApi {

  private def program: Resource[IO, List[LoopedIO]] =
    for {
      cfg        <- Resource.liftF(IO(loadConfigOrThrow[ExplorerConfig]))
      servicesFp <- ExecutionContexts.fixedThreadPool[IO](cfg.db.servicesConnPoolSize)
      grabberFp  <- ExecutionContexts.fixedThreadPool[IO](cfg.db.grabberConnPoolSize)
      blocker    <- Blocker[IO]
      servicesXa <- createTransactor(cfg.db, servicesFp, blocker)
      grabberXa  <- createTransactor(cfg.db, grabberFp, blocker)
      _          <- Resource.liftF(if (cfg.db.migrateOnStart) migrate(cfg) else IO.unit)
      txPoolRef  <- Resource.liftF(Ref.of[IO, TransactionsPool](TransactionsPool.empty))
      _          <- Resource.liftF(configure(servicesXa)("ServicesPool", cfg.db.servicesConnPoolSize))
      _          <- Resource.liftF(configure(grabberXa)("GrabberPool", cfg.db.grabberConnPoolSize))
      _          <- startApi(txPoolRef, servicesXa, servicesFp, cfg)

      onChainGrabberService = new OnChainGrabberService(grabberXa, cfg)(grabberFp)
      offChainGrabberService = new OffChainGrabberService(txPoolRef, cfg)(grabberFp)
    } yield List(onChainGrabberService, offChainGrabberService)

  override def run(args: List[String]): IO[ExitCode] =
    program
      .use(_.parTraverse(_.start).void)
      .as(ExitCode.Success)

}
