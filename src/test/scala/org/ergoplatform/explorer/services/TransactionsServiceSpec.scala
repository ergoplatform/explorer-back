package org.ergoplatform.explorer.services

import cats.effect.IO
import doobie.implicits._
import org.ergoplatform.explorer.Constants
import org.ergoplatform.explorer.db.dao.{HeadersDao, InputsDao, OutputsDao, TransactionsDao}
import org.ergoplatform.explorer.db.models.{InputWithOutputInfo, SpentOutput}
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.ergoplatform.explorer.http.protocol.{TransactionInfo, TransactionSummaryInfo}
import org.ergoplatform.explorer.utils.Paging
import org.scalactic.Equality
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.Random

class TransactionsServiceSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  implicit object TxInfoEquals extends Equality[TransactionInfo] {
    override def areEqual(a: TransactionInfo, b: Any): Boolean = b match {
      case r: TransactionInfo =>
        a.id == r.id &&
          (a.outputs.sortBy(_.id) == r.outputs.sortBy(_.id)) &&
          (a.inputs.sortBy(_.id) == r.inputs.sortBy(_.id))
      case _ => false
    }
  }

  implicit object TxSummaryInfoEquals extends Equality[TransactionSummaryInfo] {
    override def areEqual(a: TransactionSummaryInfo, b: Any): Boolean = b match {
      case r: TransactionSummaryInfo =>
        a.id == r.id &&
        a.timestamp == r.timestamp &&
        a.size == r.size &&
        a.confirmationsCount == r.confirmationsCount &&
        a.miniBlockInfo == r.miniBlockInfo &&
        a.inputs.sortBy(_.id) == r.inputs.sortBy(_.id) &&
        a.outputs.sortBy(_.id) == r.outputs.sortBy(_.id) &&
        a.totalCoins == r.totalCoins &&
        a.totalFee == r.totalFee &&
        a.feePerByte == r.feePerByte
      case _ => false
    }
  }

  it should "get txs info correctly" in {

    val ec = scala.concurrent.ExecutionContext.Implicits.global

    val (h, _, tx, inputs, outputs, _) = PreparedData.data

    val hDao = new HeadersDao
    val tDao = new TransactionsDao
    val iDao = new InputsDao
    val oDao = new OutputsDao

    hDao.insertMany(h).transact(xa).unsafeRunSync()
    tDao.insertMany(tx).transact(xa).unsafeRunSync()
    oDao.insertMany(outputs).transact(xa).unsafeRunSync()
    iDao.insertMany(inputs).transact(xa).unsafeRunSync()

    val inputsWithOutputInfo = inputs
      .map { i =>
        val oOpt = outputs.find(_.boxId == i.boxId)
        InputWithOutputInfo(i, oOpt.map(_.value), oOpt.map(_.txId), oOpt.map(_.hash))
      }

    val outputsWithSpentTx = outputs
      .map { o => SpentOutput(o, inputs.find(_.boxId == o.boxId).map(_.txId)) }

    val service = new TransactionsServiceIOImpl[IO](xa, ec)

    val randomTx1 = Random.shuffle(tx).head
    val height = h.find(_.id == randomTx1.headerId).map(_.height).getOrElse(Constants.GenesisHeight)
    val currentHeight = h.map(_.height).max
    val is = inputsWithOutputInfo.filter(_.input.txId == randomTx1.id)
    val os = outputsWithSpentTx.filter(_.output.txId == randomTx1.id)
    val expected1 = TransactionSummaryInfo.fromDb(randomTx1, height, currentHeight - height, is, os)

    val fromService1 = service.getTxInfo(randomTx1.id).unsafeRunSync()

    fromService1 shouldEqual expected1

    val randomHash = Random.shuffle(outputs).head.hash

    val length = {
      val txs1 = inputsWithOutputInfo.filter(_.address.contains(randomHash)).map(_.input.txId).toSet
      val txs2 = outputs.filter(_.hash == randomHash).map(_.txId).toSet
      (txs1 ++ txs2).size
    }

    service.countTxsByAddressId(randomHash).unsafeRunSync() shouldBe length.toLong

    val fromService2 = service.getTxsByAddressId(randomHash, Paging(0, 100)).unsafeRunSync()

    val expected2 = {
      val txIds = outputs.filter(_.hash == randomHash).map(_.txId).toSet
      val relatedTxs = tx.filter(t => txIds.apply(t.id))
      val height = h.map(_.height).max
      val confirmations = relatedTxs.map { tx => tx.id -> (height - h.find(_.id == tx.headerId).get.height + 1L) }
      TransactionInfo.extractInfo(relatedTxs, confirmations, inputsWithOutputInfo, outputsWithSpentTx)
    }

    fromService2 should contain theSameElementsAs expected2


    val randomTxId2 = Random.shuffle(tx).head.id.take(5)

    val fromService3 = service.searchById(randomTxId2).unsafeRunSync()
    val expected3 = tx.map(_.id).filter(_.contains(randomTxId2))

    fromService3 should contain theSameElementsAs expected3
  }
}
