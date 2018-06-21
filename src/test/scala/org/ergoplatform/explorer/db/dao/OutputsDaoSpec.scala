package org.ergoplatform.explorer.db.dao

import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.PreparedDB
import org.ergoplatform.explorer.utils.generators.{HeadersGen, TransactionsGenerator}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class OutputsDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "insert and find" in new {
    val dao = new OutputsDao

    val headers = HeadersGen.generateHeaders(2)
    val (txs, outputs, _) = TransactionsGenerator.generateSomeData(headers)

    val head = outputs.head
    val tail = outputs.tail

    val hDao  = new HeadersDao
    hDao.insertMany(headers).transact(xa).unsafeRunSync()
    val txDao = new TransactionsDao
    txDao.insertMany(txs).transact(xa).unsafeRunSync()

    dao.insert(head).transact(xa).unsafeRunSync() shouldBe head
    dao.insertMany(tail).transact(xa).unsafeRunSync() should contain theSameElementsAs tail

    txs.foreach { tx =>
      val id = tx.id
      val expected = outputs.filter(_.txId == id)
      val fromDb = dao.findAllByTxId(id).transact(xa).unsafeRunSync()
      expected should contain theSameElementsAs fromDb
    }

    val address = outputs.tail.head.hash

    val expected = outputs.filter(_.hash == address)
    val fromDb = dao.findAllByAddressId(address).transact(xa).unsafeRunSync()
    expected should contain theSameElementsAs fromDb

    val addressPart = address.substring(0, 5)
    val expectedToFind = outputs.collect { case o if o.hash contains addressPart => o.hash }
    val foundAddresses = dao.searchByAddressId(addressPart).transact(xa).unsafeRunSync()
    expectedToFind should contain theSameElementsAs foundAddresses

    dao.findAllByTxsId(txs.map(_.id)).transact(xa).unsafeRunSync() should contain theSameElementsAs outputs
  }

}
