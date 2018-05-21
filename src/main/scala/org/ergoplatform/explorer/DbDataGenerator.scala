package org.ergoplatform.explorer

import cats.effect.IO
import cats._, cats.data._, cats.implicits._
import doobie._, doobie.implicits._, doobie.postgres.implicits._

import com.typesafe.scalalogging.Logger

import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.db.models.Header
import org.ergoplatform.explorer.utils.generators.{HeadersGen, InterlinksGenerator, TransactionsGenerator}

import pureconfig.loadConfigOrThrow

object DbDataGenerator extends App {

  val logger = Logger("db")
  val cfg = loadConfigOrThrow[ExplorerConfig]
  logger.debug(cfg.toString)

  val xa = Transactor.fromDriverManager[IO](cfg.db.driverClassName, cfg.db.url, cfg.db.user, cfg.db.pass)
  val y = xa.yolo

  import y._

  if (args.headOption.contains("clear")) {
    logger.info("Deleting data from db.")
    sql"DELETE FROM inputs".update.quick.unsafeRunSync()
    sql"DELETE FROM outputs".update.quick.unsafeRunSync()
    sql"DELETE FROM transactions".update.quick.unsafeRunSync()
    sql"DELETE FROM interlinks".update.quick.unsafeRunSync()
    sql"DELETE FROM headers".update.quick.unsafeRunSync()
    logger.info("Done")
  } else {

    logger.info("Generating data.")
    val dao = new HeadersDao
    val linksDao = new InterlinksDao
    val tDao = new TransactionsDao
    val iDao = new InputsDao
    val oDao = new OutputsDao

    logger.info("blocks...")
    val blocks: List[Header] = HeadersGen.generateHeaders(10)
    val blocksCount = dao.insertMany(blocks).transact(xa).unsafeRunSync().length

    logger.info("interlinks...")
    val interlinksData = InterlinksGenerator.generateInterlinks(blocks)
    val linksCount = linksDao.insertManyData(interlinksData).transact(xa).unsafeRunSync().length

    logger.info("transaction related data...")
    val data = TransactionsGenerator.generateSomeData(blocks)
    val tCount = tDao.insertMany(data._1).transact(xa).unsafeRunSync().length
    val oCount = oDao.insertMany(data._2).transact(xa).unsafeRunSync().length
    val iCount = iDao.insertMany(data._3).transact(xa).unsafeRunSync().length
    logger.info("Done.")

    logger.info(
      s"""
        |Totally generated:
        |Blocks: $blocksCount
        |Transactions: $tCount
        |Inputs: $iCount
        |Outputs: $oCount
        |Interlinks: $linksCount
      """.stripMargin)
  }
}
