package org.ergoplatform.explorer.db

import cats.effect.{ContextShift, IO, Resource}
import doobie.hikari.HikariTransactor
import org.ergoplatform.explorer.config.{DbConfig, ExplorerConfig}
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext
import scala.io.Source

trait DB {

  private def readCredentials(path: String): IO[String] =
    Resource.fromAutoCloseable(IO(Source.fromFile(path, "UTF8"))).use(s => IO(s.mkString))

  private[db] def credentials(cfg: DbConfig): IO[String] =
    (cfg.passOpt, cfg.passFilePathOpt) match {
      case (Some(pass), _) => IO.pure(pass)
      case (_, Some(passFilePath)) => readCredentials(passFilePath)
      case _ => IO.pure("pass")
    }

  def configure(xa: HikariTransactor[IO], name: String): IO[Unit] =
    xa.configure(c => IO {
      c.setAutoCommit(false)
      c.setPoolName(name)
      c.setMaxLifetime(1200000L)
    })

  def migrate(cfg: ExplorerConfig): IO[Unit] =
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

  def createTransactor(
    cfg: DbConfig,
    fixedThreadPool: ExecutionContext,
    cachedThreadPool: ExecutionContext
  )(implicit S: ContextShift[IO]): Resource[IO, HikariTransactor[IO]] =
    for {
      pass <- Resource.liftF(credentials(cfg))
      xa <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = cfg.driverClassName,
        url = cfg.url,
        user = cfg.user,
        pass = pass,
        connectEC = fixedThreadPool,
        transactEC = cachedThreadPool
      )
    } yield xa

}
