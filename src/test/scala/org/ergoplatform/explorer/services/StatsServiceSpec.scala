package org.ergoplatform.explorer.services

import cats.effect.IO
import doobie.implicits._
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.ergoplatform.explorer.http.protocol.{BlockchainInfo, StatsSummary}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class StatsServiceSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "get right stats from db" in {

    val ec = scala.concurrent.ExecutionContext.Implicits.global

    val (h, info, tx, inputs, outputs, _) = PreparedData.data

    val hDao = new HeadersDao
    val tDao = new TransactionsDao
    val iDao = new InputsDao
    val oDao = new OutputsDao
    val infoDao = new BlockInfoDao

    val now = System.currentTimeMillis()

    hDao.insertMany(h.map(_.copy(timestamp = now + 1L))).transact(xa).unsafeRunSync()
    tDao.insertMany(tx.map(_.copy(timestamp = now + 1L))).transact(xa).unsafeRunSync()
    oDao.insertMany(outputs.map(_.copy(timestamp = now + 1L))).transact(xa).unsafeRunSync()
    iDao.insertMany(inputs).transact(xa).unsafeRunSync()
    infoDao.insertMany(info.map(_.copy(timestamp = now))).transact(xa).unsafeRunSync()

    val service = new StatsServiceIOImpl[IO](xa, ec)

    val expected1 = StatsSummary(
      21L, 9150L, 157500000000L, 21L, 0L, 270000000000000L, 157500000000L, 157500000000L, 0.0, 0.0, 7500000000L, 1L, 1L
    )

    val fromService1 = service.findLastStats.unsafeRunSync()
    fromService1 shouldBe expected1

    val fromService2 = service.findBlockchainInfo.unsafeRunSync()
    val expected2 = BlockchainInfo("1", 165000000000L, 21L, 1L)

    fromService2 shouldBe expected2
  }

}
