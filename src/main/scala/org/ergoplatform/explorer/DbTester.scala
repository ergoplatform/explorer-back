package org.ergoplatform.explorer

import cats.effect.IO
import cats._
import cats.data._
import cats.implicits._
import com.typesafe.scalalogging.Logger
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.dao.{HeadersDao, InterlinksDao, TransactionsDao}
import org.ergoplatform.explorer.models.Header
import org.ergoplatform.explorer.generators.{HeadersGen, InterlinksGenerator, TransactionsGenerator}
import pureconfig.loadConfigOrThrow

import scala.util.Try

object DbTester extends App {
  val logger = Logger("db")
  val cfg = loadConfigOrThrow[ExplorerConfig]
  logger.debug(cfg.toString)

  val xa = Transactor.fromDriverManager[IO](cfg.db.driverClassName, cfg.db.url, cfg.db.user, cfg.db.pass)
  val y = xa.yolo

  import y._

  val dao = new HeadersDao

  val blocks: List[Header] = HeadersGen.generateHeaders(10)

  dao.insertMany(blocks).quick.unsafeRunSync()

  val b1 = blocks.head
  val b1_1 = dao.get(b1.id).transact(xa).unsafeRunSync()

  println(b1)
  println(b1_1)

  dao.count.quick.unsafeRunSync()
  dao.list(0, 10).quick.unsafeRunSync()
  dao.list(10, 10).quick.unsafeRunSync()

  dao.updateById(b1.id, b1.copy(version = 2: Short)).quick.unsafeRunSync()
  dao.get(b1.id).quick.unsafeRunSync()
  dao.update(b1.copy(version = 3: Short)).quick.unsafeRunSync()
  dao.get(b1.id).quick.unsafeRunSync()

  dao.getLastN(5).quick.unsafeRunSync()



  dao.find("1").quick.unsafeRunSync()
  Try(dao.get("1").quick.unsafeRunSync())

  val iDao = new InterlinksDao

  val interlinksData = InterlinksGenerator.generateInterlinks(blocks)

  iDao.insertManyData(interlinksData).quick.unsafeRunSync()


  val tDao = new TransactionsDao

  val txs = TransactionsGenerator.generateForBlocks(blocks)
  tDao.insertMany(txs).quick.unsafeRunSync()


  sql"DELETE FROM transactions".update.quick.unsafeRunSync()
  sql"DELETE FROM interlinks".update.quick.unsafeRunSync()
  sql"DELETE FROM headers".update.quick.unsafeRunSync()

}
