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
    //TODO: Tune HikariCP config when needed here.
    _ <- xa.configure(c => IO {
      c.setAutoCommit(false)
      c.setMaximumPoolSize(20)
      c.setMinimumIdle(5)
      c.setMaxLifetime(1200000L)
    })
  } yield xa).unsafeRunSync()

}
