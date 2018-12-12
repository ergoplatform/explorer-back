package org.ergoplatform.explorer.db.dao

import doobie.implicits._
import org.ergoplatform.explorer.db.PreparedDB
import org.ergoplatform.explorer.db.models.{BlockInfo, Header, Miner, RawSearchBlock}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class BlockListDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "find all search blocks correctly" in {
    val h1 = Header("1", "0", 1: Short, 1L, 0L, 0L, 100L, "", "", "", "", "", "", "", "", List.empty, mainChain = true)
    val h2 = Header("2", "1", 1: Short, 2L, 0L, 0L, 150L, "", "", "", "", "", "", "", "", List.empty, mainChain = true)
    val h3 = Header("3", "2", 1: Short, 3L, 0L, 0L, 200L, "", "", "", "", "", "", "", "", List.empty, mainChain = true)

    val bi1 = BlockInfo("1", 100L, 1L, 0L, 0L, 0L, 0L, 0L, 0L, "addr1", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)
    val bi2 = BlockInfo("2", 150L, 2L, 0L, 0L, 0L, 0L, 0L, 0L, "addr2", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)
    val bi3 = BlockInfo("3", 120L, 3L, 0L, 0L, 0L, 0L, 0L, 0L, "addr1", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)

    val miner1 = Miner("addr1", "SUPSUPSUP")

    val hDao = new HeadersDao
    val mDao = new MinerDao
    val iDao = new BlockInfoDao

    val dao = new BlockListDao

    hDao.insertMany(List(h1, h2, h3)).transact(xa).unsafeRunSync()
    iDao.insertMany(List(bi1, bi2, bi3)).transact(xa).unsafeRunSync()
    mDao.insert(miner1).transact(xa).unsafeRunSync()

    val fromDb = dao.list(startTs = 0L, endTs = 300L).transact(xa).unsafeRunSync()

    val expectedResult = List(
      RawSearchBlock("3", 3L, 200L, 0L, "addr1", Some("SUPSUPSUP"), 0L, 0L, 0L),
      RawSearchBlock("2", 2L, 150L, 0L, "addr2", None, 0L, 0L, 0L),
      RawSearchBlock("1", 1L, 100L, 0L, "addr1", Some("SUPSUPSUP"), 0L, 0L, 0L)
    )

    fromDb should contain theSameElementsInOrderAs expectedResult
  }

  it should "search by id correctly" in {
    val h1 = Header("aaab", "0", 1: Short, 1L, 0L, 0L, 100L, "", "", "", "", "", "", "", "", List.empty, mainChain = true)
    val h2 = Header("aab", "1", 1: Short, 2L, 0L, 0L, 150L, "", "", "", "", "", "", "", "", List.empty, mainChain = true)
    val bi1 = BlockInfo("aaab", 100L, 1L, 0L, 0L, 0L, 0L, 0L, 0L, "addr3", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)
    val bi2 = BlockInfo("aab", 150L, 2L, 0L, 0L, 0L, 0L, 0L, 0L,  "addr4", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)

    val hDao = new HeadersDao
    val iDao = new BlockInfoDao

    hDao.insertMany(List(h1, h2)).transact(xa).unsafeRunSync()
    iDao.insertMany(List(bi1, bi2)).transact(xa).unsafeRunSync()

    val dao = new BlockListDao

    val r1 = RawSearchBlock("aaab", 1L, 100L, 0L, "addr3", None, 0L, 0L, 0L)
    val r2 = RawSearchBlock("aab", 2L, 150L, 0L, "addr4", None, 0L, 0L, 0L)

    dao.searchById("aa").transact(xa).unsafeRunSync() should contain theSameElementsAs List(r1, r2)
    dao.searchById("aaa").transact(xa).unsafeRunSync() should contain theSameElementsAs List(r1)
  }

}
