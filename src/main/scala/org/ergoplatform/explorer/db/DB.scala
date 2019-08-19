package org.ergoplatform.explorer.db

import cats.effect.IO
import doobie.hikari.HikariTransactor
import org.ergoplatform.explorer.config.{DbConfig, ExplorerConfig}
import org.flywaydb.core.Flyway

trait DB {

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

}
