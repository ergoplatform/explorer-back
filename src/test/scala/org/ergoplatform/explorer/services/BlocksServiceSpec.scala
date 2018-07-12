package org.ergoplatform.explorer.services

import cats.effect.IO
import doobie.implicits._
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.db.models.{InputWithOutputInfo, SpentOutput}
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.ergoplatform.explorer.http.protocol.{BlockReferencesInfo, BlockSummaryInfo, FullBlockInfo, TransactionInfo}
import org.ergoplatform.explorer.utils.{Desc, Paging, Sorting}
import org.scalactic.Equality
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.Random

class BlocksServiceSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  implicit object TxInfoEquals extends Equality[TransactionInfo] {
    override def areEqual(a: TransactionInfo, b: Any): Boolean = b match {
      case r: TransactionInfo =>
        a.id == r.id &&
          (a.outputs.sortBy(_.id) == r.outputs.sortBy(_.id)) &&
          (a.inputs.sortBy(_.id) == r.inputs.sortBy(_.id))
      case _ => false
    }
  }

  implicit object BlockInfoEquals extends Equality[BlockSummaryInfo] {
    override def areEqual(a: BlockSummaryInfo, b: Any): Boolean = b match {
      case r: BlockSummaryInfo =>
        a.references == r.references &&
        a.info.headerInfo == r.info.headerInfo &&
        a.info.adProof == r.info.adProof &&
        a.info.transactionsInfo.length == r.info.transactionsInfo.length &&
        a.info.transactionsInfo.forall{ t =>
          val rTx = r.info.transactionsInfo.find(_.id == t.id)
          rTx.nonEmpty && TxInfoEquals.areEqual(t, rTx.get)
        }
      case _ => false
    }
  }

  it should "find and list blocks correctly" in {

    val ec = scala.concurrent.ExecutionContext.Implicits.global

    val (h, info, tx, inputs, outputs, _) = PreparedData.data

    val hDao = new HeadersDao
    val tDao = new TransactionsDao
    val iDao = new InputsDao
    val oDao = new OutputsDao
    val infoDao = new BlockInfoDao

    hDao.insertMany(h).transact(xa).unsafeRunSync()
    tDao.insertMany(tx).transact(xa).unsafeRunSync()
    oDao.insertMany(outputs).transact(xa).unsafeRunSync()
    iDao.insertMany(inputs).transact(xa).unsafeRunSync()
    infoDao.insertMany(info).transact(xa).unsafeRunSync()

    val inputsWithOutputInfo = inputs
      .map { i =>
        val oOpt = outputs.find(_.boxId == i.boxId)
        InputWithOutputInfo(i, oOpt.map(_.value), oOpt.map(_.txId), oOpt.map(_.hash))
      }

    val outputsWithSpentTx = outputs.map { o => SpentOutput(o, inputs.find(_.boxId == o.boxId).map(_.txId)) }

    val service = new BlocksServiceIOImpl[IO](xa, ec)

    val randomBlockId = Random.shuffle(h).head.id
    val fromService1 = service.getBlock(randomBlockId).unsafeRunSync()

    val fullBlockInfo = {
      val header = h.find(_.id == randomBlockId).get
      val txs = tx.filter(_.headerId == randomBlockId)
      val size = info.find(_.headerId == randomBlockId).map(_.blockSize).getOrElse(0L)
      val height = h.map(_.height).max
      val confirmations = txs.map { tx => tx.id -> (height - h.find(_.id == tx.headerId).get.height + 1L) }
      FullBlockInfo(header, txs, confirmations, inputsWithOutputInfo, outputsWithSpentTx, None, size)
    }

    val references = {
      val prev = h.find(_.id == randomBlockId).get.parentId
      val next = h.find(_.parentId == randomBlockId).map(_.id)
      BlockReferencesInfo(prev, next)
    }

    val expected1 = BlockSummaryInfo(fullBlockInfo, references)
    fromService1 shouldEqual expected1

    service.count(0L, Long.MaxValue).unsafeRunSync() shouldBe h.length.toLong
    val fromService2 = service.getBlocks(Paging(0, 100), Sorting("height", Desc), 0L, Long.MaxValue).unsafeRunSync()

    fromService2.map(_.id) shouldBe h.sortBy(v => -v.height).map(_.id)

    val subId = randomBlockId.take(8)
    val fromService3 = service.searchById(subId).unsafeRunSync()
    fromService3.map(_.id) should contain theSameElementsAs h.filter(_.id.contains(subId)).map(_.id)
  }
}
