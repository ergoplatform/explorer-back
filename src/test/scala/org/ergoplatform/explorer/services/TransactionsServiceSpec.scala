package org.ergoplatform.explorer.services

import cats.effect.IO
import cats.effect.concurrent.Ref
import doobie.implicits._
import org.ergoplatform.explorer.{Constants, Utils}
import org.ergoplatform.explorer.config.Config
import org.ergoplatform.explorer.db.dao.{
  AssetsDao,
  HeadersDao,
  InputsDao,
  OutputsDao,
  TransactionsDao
}
import org.ergoplatform.explorer.db.models.{Asset, Output}
import org.ergoplatform.explorer.db.models.composite.{ExtendedInput, ExtendedOutput}
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.ergoplatform.explorer.http.protocol.{TransactionInfo, TransactionSummaryInfo}
import org.ergoplatform.explorer.persistence.TransactionsPool
import org.ergoplatform.explorer.utils.Paging
import org.scalactic.Equality
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.Random

class TransactionsServiceSpec
  extends FlatSpec
  with Matchers
  with BeforeAndAfterAll
  with PreparedDB
  with Config {

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

  it should "get txs info correctly" ignore {

    val ec = scala.concurrent.ExecutionContext.Implicits.global

    val (h, _, tx, inputs, outputs, _, assets) = PreparedData.data

    val hDao = new HeadersDao
    val tDao = new TransactionsDao
    val iDao = new InputsDao
    val oDao = new OutputsDao
    val aDao = new AssetsDao

    hDao.insertMany(h).transact(xa).unsafeRunSync()
    tDao.insertMany(tx).transact(xa).unsafeRunSync()
    oDao.insertMany(outputs).transact(xa).unsafeRunSync()
    iDao.insertMany(inputs).transact(xa).unsafeRunSync()
    aDao.insertMany(assets).transact(xa).unsafeRunSync()

    val inputsWithOutputInfo = inputs
      .map { i =>
        val oOpt = outputs.find(_.boxId == i.boxId)
        ExtendedInput(i, oOpt.map(_.value), oOpt.map(_.txId), oOpt.map(_.address))
      }

    val outputsWithSpentTx = outputs
      .foldLeft(List.empty[(Output, List[Asset])]) {
        case (acc, out) =>
          val outAssets = assets.filter(_.boxId == out.boxId).reverse
          acc :+ (out -> outAssets)
      }
      .map {
        case (o, assets) =>
          ExtendedOutput(o, inputs.find(_.boxId == o.boxId).map(_.txId), mainChain = true) -> assets
      }
      .reverse

    val offChainStore =
      Ref.of[IO, TransactionsPool](TransactionsPool.empty).unsafeRunSync()

    val service = new TransactionsServiceImpl[IO](xa, offChainStore, ec, cfg)

    val randomTx1 = Random.shuffle(tx).head
    val height =
      h.find(_.id == randomTx1.headerId).map(_.height).getOrElse(Constants.GenesisHeight)
    val currentHeight = h.map(_.height).max
    val is = inputsWithOutputInfo.filter(_.input.txId == randomTx1.id)
    val os = outputsWithSpentTx.filter(_._1.output.txId == randomTx1.id)
    val expected1 =
      TransactionSummaryInfo.apply(randomTx1, height, currentHeight - height, is, os)

    val fromService1 = service.getTxInfo(randomTx1.id).unsafeRunSync()

    fromService1 shouldEqual expected1

    val randomHash = Random.shuffle(outputs).head.address

    val length = {
      val txs1 = inputsWithOutputInfo
        .filter(_.address.contains(randomHash))
        .map(_.input.txId)
        .toSet
      val txs2 = outputs.filter(_.address == randomHash).map(_.txId).toSet
      (txs1 ++ txs2).size
    }

    service.countTxsByAddressId(randomHash).unsafeRunSync() shouldBe length.toLong

    val fromService2 =
      service.getTxsByAddressId(randomHash, Paging(0, 100)).unsafeRunSync()

    val expected2 = {
      val txIds = outputs.filter(_.address == randomHash).map(_.txId).toSet
      val relatedTxs = tx.filter(t => txIds.apply(t.id))
      val height = h.map(_.height).max
      val confirmations = relatedTxs.map { tx =>
        tx.id -> (height - h.find(_.id == tx.headerId).get.height + 1L)
      }
      TransactionInfo.fromBatch(
        relatedTxs,
        confirmations,
        inputsWithOutputInfo,
        outputsWithSpentTx
      )
    }

    fromService2 should contain theSameElementsAs expected2

    val randomTxId2 = Random.shuffle(tx).head.id.take(5)

    val fromService3 = service.searchByIdSubstr(randomTxId2).unsafeRunSync()
    val expected3 = tx.map(_.id).filter(_.contains(randomTxId2))

    fromService3 should contain theSameElementsAs expected3

    val heightSince = 10

    val fromService4 = service.getTxsSince(heightSince, Paging(0, 100)).unsafeRunSync()

    val expected4 = {
      val headersSince = h.filter(_.height >= heightSince)
      val relatedTxs = tx.filter(x => headersSince.map(_.id).contains(x.headerId))
      val height = h.map(_.height).max
      val confirmations = relatedTxs.map { tx =>
        tx.id -> (height - h.find(_.id == tx.headerId).get.height + 1L)
      }
      TransactionInfo.fromBatch(
        relatedTxs,
        confirmations,
        inputsWithOutputInfo,
        outputsWithSpentTx
      )
    }

    fromService4 should contain theSameElementsAs expected4
  }

  it should "get outputs by ErgoTree" in {

    val ec = scala.concurrent.ExecutionContext.Implicits.global

    val (h, _, tx, inputs, outputs, _, assets) = PreparedData.data

    val hDao = new HeadersDao
    val tDao = new TransactionsDao
    val iDao = new InputsDao
    val oDao = new OutputsDao
    val aDao = new AssetsDao

    hDao.insertMany(h).transact(xa).unsafeRunSync()
    tDao.insertMany(tx).transact(xa).unsafeRunSync()
    oDao.insertMany(outputs).transact(xa).unsafeRunSync()
    iDao.insertMany(inputs).transact(xa).unsafeRunSync()
    aDao.insertMany(assets).transact(xa).unsafeRunSync()

    val offChainStore =
      Ref.of[IO, TransactionsPool](TransactionsPool.empty).unsafeRunSync()

    val service = new TransactionsServiceImpl[IO](xa, offChainStore, ec, cfg)

    service.getOutputsByErgoTree("31").unsafeRunSync() shouldBe empty
    service.getOutputsByErgoTree("31", unspentOnly = true).unsafeRunSync() shouldBe empty
    service.getOutputsByErgoTreeTemplate("31").unsafeRunSync() shouldBe empty
    service
      .getOutputsByErgoTreeTemplate("31", unspentOnly = true)
      .unsafeRunSync() shouldBe empty

    // Token seller contract from AssetsAtomicExchange
    // http://github.com/ScorexFoundation/sigmastate-interpreter/blob/633efcfd47f2fa4aa240eee2f774cc033cc241a5/contract-verification/src/main/scala/sigmastate/verification/contract/AssetsAtomicExchange.scala#L34-L34
    val treeDexSellerContract = outputs(2).ergoTree
    val treeTemplateDexSellerContract = Utils.ergoTreeTemplateBytes(treeDexSellerContract)

    val res1 = service.getOutputsByErgoTree(treeDexSellerContract).unsafeRunSync()
    res1.length shouldBe 1
    res1.head.ergoTree shouldEqual treeDexSellerContract

    service
      .getOutputsByErgoTree(
        "cd070305faeee1c4605730b42f7bbb8924fe47b1a545b68bbf845f22961e5748ace054",
        unspentOnly = true
      )
      .unsafeRunSync() should (not be empty)

    val res2 =
      service.getOutputsByErgoTreeTemplate(treeTemplateDexSellerContract).unsafeRunSync()
    res2.length shouldBe 1
    res2.head.ergoTree shouldEqual treeDexSellerContract

    service
      .getOutputsByErgoTreeTemplate(
        treeTemplateDexSellerContract,
        unspentOnly = true
      )
      .unsafeRunSync() shouldBe empty
  }

}
