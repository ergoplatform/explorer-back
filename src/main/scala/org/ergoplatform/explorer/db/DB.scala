package org.ergoplatform.explorer.db

import cats.effect.{Blocker, ContextShift, IO, Resource}
import doobie.hikari.HikariTransactor
import org.ergoplatform.explorer.config.{DbConfig, ExplorerConfig}
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext
import scala.io.Source

trait DB {

  private def readCredentials(path: String): IO[String] =
    Resource.fromAutoCloseable(IO(Source.fromFile(path, "UTF8"))).use(s => IO(s.mkString))

  private[db] def credentials(cfg: DbConfig): IO[String] =
    (cfg.pass, cfg.passFilePath) match {
      case (Some(pass), _)         => IO.pure(pass)
      case (_, Some(passFilePath)) => readCredentials(passFilePath)
      case _                       => IO.pure("pass")
    }

  final def configure(
    xa: HikariTransactor[IO]
  )(name: String, maxPoolSize: Int): IO[Unit] =
    xa.configure(c =>
      IO {
        c.setAutoCommit(false)
        c.setPoolName(name)
        c.setMaxLifetime(600000)
        c.setMaximumPoolSize(maxPoolSize)
        c.setMinimumIdle(math.max(2, maxPoolSize / 2))
      }
    )

  final def migrate(cfg: ExplorerConfig): IO[Unit] =
    for {
      pass <- credentials(cfg.db)
      flyway <- IO {
        val f = new Flyway()
        f.setSqlMigrationSeparator("__")
        f.setLocations("classpath:db")
        f.setDataSource(cfg.db.url, cfg.db.user, pass)
        f
      }
      _ <- IO(flyway.clean())
      _ <- IO(flyway.migrate())
    } yield ()

  final def createTransactor(
    cfg: DbConfig,
    fixedThreadPool: ExecutionContext,
    blocker: Blocker
  )(implicit S: ContextShift[IO]): Resource[IO, HikariTransactor[IO]] =
    for {
      pass <- Resource.liftF(credentials(cfg))
      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = cfg.driverClassName,
        url = cfg.url,
        user = cfg.user,
        pass = pass,
        connectEC = fixedThreadPool,
        blocker = blocker
      )
    } yield xa

}
