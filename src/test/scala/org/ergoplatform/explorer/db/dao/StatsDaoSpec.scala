package org.ergoplatform.explorer.db.dao

import cats.effect.IO
import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.PreparedDB
import org.ergoplatform.explorer.db.models.{Header, Output}
import org.ergoplatform.explorer.utils.generators._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class StatsDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "insert, find" in new {
    val dao = new StatsDao
    val stats = StatsGenerator.generateStats(20).zipWithIndex.map { case (s, i) => s.copy(id = i.toLong) }
    val head = stats.head
    val tail = stats.tail

    dao.insert(head).transact(xa).unsafeRunSync() shouldBe head
    dao.insertMany(tail).transact(xa).unsafeRunSync() should contain theSameElementsAs tail

    dao.findLast.transact(xa).unsafeRunSync() shouldBe Some(stats.maxBy(_.timestamp))

    dao.difficultiesSumSince(System.currentTimeMillis() + 1000000L).transact(xa).unsafeRunSync() shouldBe 0L
    dao.circulatingSupplySince(System.currentTimeMillis() + 1000000L).transact(xa).unsafeRunSync() shouldBe 0L
  }

  it should "count circulating supply and difficulries" in new {
    val hDao = new HeadersDao
    val tDao = new TransactionsDao
    val iDao = new InputsDao
    val oDao = new OutputsDao

    val blocks: List[Header] = HeadersGen
      .generateHeaders(10)
      .zipWithIndex
      .map { case (h, i) => h.copy(timestamp = i.toLong) }

    hDao.insertMany(blocks).transact(xa).unsafeRunSync()

    val data = TransactionsGenerator.generateSomeData(blocks)
    val txs = data._1.zipWithIndex.map { case (tx, i) => tx.copy(timestamp = i.toLong) }

    tDao.insertMany(txs).transact(xa).unsafeRunSync()
    oDao.insertMany(data._2).transact(xa).unsafeRunSync()
    iDao.insertMany(data._3).transact(xa).unsafeRunSync()

    val dao = new StatsDao
    val stats = StatsGenerator
      .generateStats(blocks.length)
      .zipWithIndex
      .map { case (s, i) => s.copy(id = i.toLong, timestamp = i.toLong) }

    dao.insertMany(stats).transact(xa).unsafeRunSync()

    dao.circulatingSupplySince(0L).transact(xa).unsafeRunSync() shouldBe data._2.map(_.value).sum
    dao.difficultiesSumSince(0L).transact(xa).unsafeRunSync() shouldBe stats.map(_.difficulty).sum

    val supply = osValueSumByTxIds(txs.filter(_.timestamp >= 4L).map(_.id), data._2)

    dao.circulatingSupplySince(4L).transact(xa).unsafeRunSync() shouldBe supply
    dao.difficultiesSumSince(4L).transact(xa).unsafeRunSync() shouldBe stats.filter(_.timestamp >= 4L).map(_.difficulty).sum

    def osValueSumByTxIds(txIds: List[String], os: List[Output]): Long = os
      .filter(v => txIds.contains(v.txId))
      .map(_.value)
      .sum
  }
}
