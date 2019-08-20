package org.ergoplatform.explorer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import doobie.util.ExecutionContexts
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.db.DB
import org.ergoplatform.explorer.grabber.{LoopedIO, OffChainGrabberService, OnChainGrabberService}
import org.ergoplatform.explorer.http.RestApi
import org.ergoplatform.explorer.persistence.TransactionsPool
import pureconfig.loadConfigOrThrow

import scala.concurrent.ExecutionContextExecutor

object ExplorerApp extends IOApp with DB with RestApi {

  implicit val system: ActorSystem = ActorSystem("explorer-system")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  private def program: Resource[IO, List[LoopedIO]] = for {
    cfg <- Resource.liftF(IO(loadConfigOrThrow[ExplorerConfig]))
    servicesFp  <- ExecutionContexts.fixedThreadPool[IO](cfg.db.servicesConnPoolSize)
    servicesCp <- ExecutionContexts.cachedThreadPool[IO]
    grabberFp  <- ExecutionContexts.fixedThreadPool[IO](cfg.db.grabberConnPoolSize)
    grabberCp <- ExecutionContexts.cachedThreadPool[IO]
    _ <-  Resource.liftF(if (cfg.db.migrateOnStart) migrate(cfg) else IO.unit)
    txPoolRef <- Resource.liftF(Ref.of[IO, TransactionsPool](TransactionsPool.empty))
    servicesXa <- createTransactor(cfg.db, servicesFp, servicesCp)
    grabberXa <- createTransactor(cfg.db, grabberFp, grabberCp)
    _ <- Resource.liftF(configure(servicesXa, "ServicesPool"))
    _ <- Resource.liftF(configure(grabberXa, "GrabberPool"))
    _ <- Resource.liftF(startApi(txPoolRef, servicesXa)(cfg))

    onChainGrabberService = new OnChainGrabberService(grabberXa, cfg)(ec)
    offChainGrabberService = new OffChainGrabberService(txPoolRef, cfg)(ec)
  } yield List(onChainGrabberService, offChainGrabberService)

  override def run(args: List[String]): IO[ExitCode] =
    program.use {
      _.parTraverse(_.start).void
    }.as(ExitCode.Success)

}
