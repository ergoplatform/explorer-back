package org.ergoplatform.explorer

import cats.effect.IO
import com.typesafe.scalalogging.Logger
import doobie._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.dao.BlocksDao
import pureconfig.loadConfigOrThrow

object DbTester extends App {
  val logger = Logger("db")
  val cfg = loadConfigOrThrow[ExplorerConfig]
  logger.debug(cfg.toString)

  val xa = Transactor.fromDriverManager[IO](cfg.db.driverClassName, cfg.db.url, cfg.db.user, cfg.db.pass)
  val y = xa.yolo

  import y._

  val dao = new BlocksDao

  val realId = "675b0532fe48bd13d6728f1830b92da606a423714104d0e4c20bc496639f1e4e"
  val fakeId = "ooopsi"

  dao.find(realId).quick.unsafeRunSync()
  dao.find(fakeId).quick.unsafeRunSync()
}
