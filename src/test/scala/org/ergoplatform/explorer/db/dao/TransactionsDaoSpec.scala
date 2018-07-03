package org.ergoplatform.explorer.db.dao

import doobie.implicits._
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.Random

class TransactionsDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "find and query transactions" in new {

    val dao = new TransactionsDao

    val hDao = new HeadersDao
    val oDao = new OutputsDao

    val (headers, _, txs, inputs, outputs, _) = PreparedData.data

    hDao.insertMany(headers).transact(xa).unsafeRunSync()

    val head = txs.head
    val tail = txs.tail

    dao.insert(head).transact(xa).unsafeRunSync() shouldBe head
    dao.find(head.id).transact(xa).unsafeRunSync() shouldBe Some(head)
    dao.get(head.id).transact(xa).unsafeRunSync() shouldBe head

    dao.find(head.id + "1").transact(xa).unsafeRunSync() shouldBe None
    the[NoSuchElementException] thrownBy dao.get(head.id + "11").transact(xa).unsafeRunSync()

    val substring = head.id.substring(3, 8)
    val expectedToFind = txs collect { case tx if tx.id contains substring => tx.id }
    val foundTxs = dao.searchById(substring).transact(xa).unsafeRunSync()
    expectedToFind should contain theSameElementsAs foundTxs

    dao.insertMany(tail).transact(xa).unsafeRunSync() should contain theSameElementsAs tail

    oDao.insertMany(outputs).transact(xa).unsafeRunSync()

    headers.foreach { h =>
      val id = h.id
      val expected = txs.filter(_.headerId == id)
      val fromDb = dao.findAllByBlockId(id).transact(xa).unsafeRunSync()
      fromDb should contain theSameElementsAs expected
    }

    headers.foreach { h =>
      val id = h.id
      val expected = List(id -> txs.count(_.headerId == id))
      val fromDb = dao.countTxsNumbersByBlocksIds(List(id)).transact(xa).unsafeRunSync()
      fromDb should contain theSameElementsAs expected
    }

    val randomOs = Random.shuffle(outputs).head
    val txIds: Set[String] = outputs.filter(_.hash == randomOs.hash).map(_.txId).toSet
    val expected = txs.filter(tx => txIds(tx.id))
    val fromDb = dao.getTxsByAddressId(randomOs.hash, offset = 0, limit = Int.MaxValue).transact(xa).unsafeRunSync()
    fromDb should contain theSameElementsAs expected
    dao.countTxsByAddressId(randomOs.hash).transact(xa).unsafeRunSync() shouldBe expected.length.toLong
  }

}
