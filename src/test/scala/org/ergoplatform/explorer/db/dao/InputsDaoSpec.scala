package org.ergoplatform.explorer.db.dao

import cats.effect.IO
import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.PreparedDB
import org.ergoplatform.explorer.db.models.InputWithOutputInfo
import org.ergoplatform.explorer.utils.generators.{HeadersGen, TransactionsGenerator}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class InputsDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "insert and find" in new {
    val dao = new InputsDao

    val headers = HeadersGen.generateHeaders(2)
    val (txs, outputs, inputs) = TransactionsGenerator.generateSomeData(headers)



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
      val v = outputs.find(_.boxId == i.boxId).map(_.value).getOrElse(0L)
      val otxid = outputs.find(_.boxId == i.boxId).map(_.txId).getOrElse("")
      val hash = outputs.find(_.boxId == i.boxId).map(_.hash).getOrElse("")
      InputWithOutputInfo(i, v, otxid, hash)
    }

    dao.findAllByTxsIdWithValue(txs.map(_.id)).transact(xa).unsafeRunSync() should contain theSameElementsAs withValues
  }

}
