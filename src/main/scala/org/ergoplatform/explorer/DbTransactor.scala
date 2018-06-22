package org.ergoplatform.explorer

import cats.effect.IO
import doobie.hikari.HikariTransactor

trait DbTransactor { self: Configuration =>

  lazy val transactor: HikariTransactor[IO] = (for {
    xa <- HikariTransactor.newHikariTransactor[IO](
      driverClassName = cfg.db.driverClassName,
      url = cfg.db.url,
      user = cfg.db.user,
      pass = cfg.db.pass
    )
    _ <- xa.configure(c => IO {
      c.setPoolName("Explorer-Hikari-Pool")
      c.setAutoCommit(false)
      c.setMaximumPoolSize(20)
      c.setMinimumIdle(5)
      c.setMaxLifetime(1200000L)
    })
  } yield xa).unsafeRunSync()

  lazy val transactor2: HikariTransactor[IO] = (for {
    xa <- HikariTransactor.newHikariTransactor[IO](
      driverClassName = cfg.db.driverClassName,
      url = cfg.db.url,
      user = cfg.db.user,
      pass = cfg.db.pass
    )
    _ <- xa.configure(c => IO {
      c.setPoolName("Grabber-Hikari-Pool")
      c.setAutoCommit(false)
      c.setMaximumPoolSize(5)
      c.setMinimumIdle(2)
      c.setMaxLifetime(1200000L)
    })
  } yield xa).unsafeRunSync()

}
