package org.ergoplatform.explorer.db.dao

import doobie.implicits._
import org.ergoplatform.explorer.db.models.composite
import org.ergoplatform.explorer.db.models.composite.ExtendedInput
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class InputsDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "insert and find" in new {
    val dao = new InputsDao

    val (headers, _, txs, inputs, outputs, _) = PreparedData.data

    val head = inputs.head
    val tail = inputs.tail

    val hDao  = new HeadersDao
    hDao.insertMany(headers).transact(xa).unsafeRunSync()
    val txDao = new TransactionsDao
    txDao.insertMany(txs).transact(xa).unsafeRunSync()
    val oDao = new OutputsDao
    oDao.insertMany(outputs).transact(xa).unsafeRunSync()

    dao.insert(head).transact(xa).unsafeRunSync() shouldBe head
    dao.insertMany(tail).transact(xa).unsafeRunSync() should contain theSameElementsAs tail

    txs.foreach { tx =>
      val id = tx.id
      val expected = inputs.filter(_.txId == id)
      val fromDb = dao.findAllByTxId(id).transact(xa).unsafeRunSync()
      expected should contain theSameElementsAs fromDb
    }

    dao.findAllByTxsId(txs.map(_.id)).transact(xa).unsafeRunSync() should contain theSameElementsAs inputs

    val withValues = inputs.map { i =>
      val oOpt = outputs.find(_.boxId == i.boxId)
      val v = oOpt.map(_.value)
      val otxid = oOpt.map(_.txId)
      val hash = oOpt.map(_.address)
      composite.ExtendedInput(i, v, otxid, hash)
    }

    val fromDb = dao.findAllByTxsIdWithValue(txs.map(_.id)).transact(xa).unsafeRunSync()
    fromDb should contain theSameElementsAs withValues
  }

}
