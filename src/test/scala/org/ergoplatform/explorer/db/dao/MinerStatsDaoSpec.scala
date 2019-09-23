package org.ergoplatform.explorer.db.dao

import doobie.implicits._
import org.ergoplatform.explorer.db.models.composite.MinerStats
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class MinerStatsDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "extract miner stats correctly" in {

    val (headers, infos, _, _, _, _, _) = PreparedData.data

    val hDao = new HeadersDao
    val iDao = new BlockInfoDao

    hDao.insertMany(headers).transact(xa).unsafeRunSync
    iDao.insertMany(infos).transact(xa).unsafeRunSync

    val dao = new MinerStatsDao

    val fromDb = dao.minerStatsAfter(0L).transact(xa).unsafeRunSync

    val expected = infos.groupBy(_.minerAddress).map { case (address, info) =>
        val count = info.length
        val d = info.map(_.difficulty).sum
        val t = info.map(_.blockMiningTime).sum
        MinerStats(address, d, t , count, None)
    }

    fromDb should contain theSameElementsAs expected
  }

}
