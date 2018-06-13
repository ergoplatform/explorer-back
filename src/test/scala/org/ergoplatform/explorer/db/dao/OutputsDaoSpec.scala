package org.ergoplatform.explorer.db.dao

import cats.effect.IO
import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.PreparedDB
import org.ergoplatform.explorer.utils.generators.{HeadersGen, TransactionsGenerator}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class OutputsDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "insert and find" in new {
    val dao = new OutputsDao

    val headers = HeadersGen.generateHeaders(2)
    val (txs, oss, _) = TransactionsGenerator.generateSomeData(headers)

    val head = oss.head
    val tail = oss.tail

    val hDao  = new HeadersDao
    hDao.insertMany(headers).transact(xa).unsafeRunSync()
    val txDao = new TransactionsDao
    txDao.insertMany(txs).transact(xa).unsafeRunSync()

    dao.insert(head).transact(xa).unsafeRunSync() shouldBe head
    dao.insertMany(tail).transact(xa).unsafeRunSync() should contain theSameElementsAs tail

    txs.foreach { tx =>
      val id = tx.id
      val expected = oss.filter(_.txId == id)
      val fromDb = dao.findAllByTxId(id).transact(xa).unsafeRunSync()
      expected should contain theSameElementsAs fromDb
    }

    val address = oss.tail.head.hash

    val expected = oss.filter(_.hash == address)
    val fromDb = dao.findAllByAddressId(address).transact(xa).unsafeRunSync()
    expected should contain theSameElementsAs fromDb

    dao.findAllByTxsId(txs.map(_.id)).transact(xa).unsafeRunSync() should contain theSameElementsAs oss
  }

}
