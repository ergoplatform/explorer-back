package org.ergoplatform.explorer.db

import cats.effect.{ContextShift, IO, Resource}
import doobie.hikari.HikariTransactor
import org.ergoplatform.explorer.config.{DbConfig, ExplorerConfig}
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

trait DB {

  def configure(xa: HikariTransactor[IO], name: String): IO[Unit] =
    xa.configure(c => IO {
      c.setAutoCommit(false)
      c.setPoolName(name)
      c.setMaxLifetime(1200000L)
    })

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

  def createTransactor(
    cfg: DbConfig,
    fixedThreadPool: ExecutionContext,
    cachedThreadPool: ExecutionContext
  )(implicit S: ContextShift[IO]): Resource[IO, HikariTransactor[IO]] =
    HikariTransactor.newHikariTransactor[IO](
      driverClassName = cfg.driverClassName,
      url = cfg.url,
      user = cfg.user,
      pass = cfg.pass,
      connectEC = fixedThreadPool,
      transactEC = cachedThreadPool
    )

}
