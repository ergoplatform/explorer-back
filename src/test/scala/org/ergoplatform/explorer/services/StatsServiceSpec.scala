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
      blocksCount = 21L,
      blocksAvgTime = 9150L,
      totalCoins = 157500000000L,
      totalTransactionsCount = 21L,
      totalFee = 0L,
      totalOutput = 270000000000000L,
      estimatedOutput = 5668425000000000L,
      totalMinerRevenue = 157500000000L,
      percentEarnedTransactionsFees = 0.0,
      percentTransactionVolume = 0.00277855,
      costPerTx = 7500000000L,
      lastDifficulty = 1L,
      totalHashrate = 1L
    )

    val fromService1 = service.findLastStats.unsafeRunSync()
    fromService1 shouldBe expected1

    val fromService2 = service.findBlockchainInfo.unsafeRunSync()
    val expected2 = BlockchainInfo("1", 1650000000000L, 21L, 1L)

    fromService2 shouldBe expected2
  }

}
