package org.ergoplatform.explorer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import akka.stream.ActorMaterializer
import cats.effect.IO
import com.typesafe.scalalogging.Logger
import doobie.hikari.implicits._
import org.ergoplatform.explorer.grabber.GrabberService
import org.flywaydb.core.Flyway

import scala.concurrent.{Await, ExecutionContext}
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

    val migrate: IO[Unit] = for {
      flyway <- IO {
        val f = new Flyway()
        f.setSqlMigrationSeparator("__")
        f.setLocations("classpath:db")
        f.setDataSource(cfg.db.url, cfg.db.user, cfg.db.pass)
        f
      }
      _ <- IO {
        flyway.clean()
      }
      _ <- IO {
        flyway.migrate()
      }
    } yield ()

    //if (cfg.db.migrateOnStart) { migrate.unsafeRunSync() }

    val binding = Http().bindAndHandle(routes, host, port)

    binding.onComplete {
      case Success(b) =>
        logger.info(s"HTTP server started at ${b.localAddress}")
      case Failure(ex) =>
        logger.error("Could not start HTTP server", ex)
    }

    val grabberEc = ExecutionContext.fromExecutor(Pools.grabberPool)

    val grabberService = new GrabberService(transactor2, grabberEc, cfg.grabber)
    grabberService.start

    sys.addShutdownHook {
      grabberService.stop
      val stop = binding.flatMap { x => x.unbind() }
      stop.onComplete { _ =>
        Pools.shutdown
        transactor.shutdown.unsafeRunSync()
        transactor2.shutdown.unsafeRunSync()
        system.terminate()
      }
      Await.result(stop, 5 seconds)
      ()
    }
  }
}
