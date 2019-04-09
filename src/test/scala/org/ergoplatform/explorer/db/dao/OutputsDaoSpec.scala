package org.ergoplatform.explorer.db.dao

import doobie.implicits._
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.ergoplatform.explorer.db.models.SpentOutput
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class OutputsDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "insert and find" in new {
    val dao = new OutputsDao
    val inputDao = new InputsDao

    val (headers, _, txs, inputs, outputsWithoutTs, _) = PreparedData.data

    val outputs = outputsWithoutTs.map { o =>
      val ts = txs.find(_.id == o.txId).map(_.timestamp).getOrElse(0L)
      o.copy(timestamp = ts)
    }

    inputDao.insertMany(inputs).transact(xa).unsafeRunSync()

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

    val address = outputs.tail.head.address

    val expected = outputs.filter(_.address == address)
    val fromDb = dao.findAllByAddressId(address).transact(xa).unsafeRunSync()
    expected should contain theSameElementsAs fromDb.map(_.output)

    val addressPart = address.substring(5, 10)
    val expectedToFind = outputs.collect { case o if o.address contains addressPart => o.address }
    val foundAddresses = dao.searchByAddressId(addressPart).transact(xa).unsafeRunSync()
    expectedToFind should contain theSameElementsAs foundAddresses

    val withSpent = outputs.map{ o => SpentOutput(o, inputs.find(_.boxId == o.boxId).map(_.txId))}

    dao.findAllByTxsId(txs.map(_.id)).transact(xa).unsafeRunSync() should contain theSameElementsAs outputs
    dao.findAllByTxsIdWithSpent(txs.map(_.id)).transact(xa).unsafeRunSync() should contain theSameElementsAs withSpent

    val inputIds = inputs.map { _.boxId }
    val unspent = outputs.filterNot{ o => inputIds.contains(o.boxId)}
    val unspentSum = unspent.map{_.value}.sum

    dao.sumOfAllUnspentOutputsSince(0L).transact(xa).unsafeRunSync() shouldBe unspentSum
  }

}
