package org.ergoplatform.explorer.db.dao

import doobie.implicits._
import org.ergoplatform.explorer.db.models.composite
import org.ergoplatform.explorer.db.models.composite.ExtendedOutput
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import scorex.util.encode.Base16
import sigmastate.serialization.{ErgoTreeSerializer, SigmaSerializer}

class OutputsDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  private def ergoTreeTemplateBytes(ergoTree: String): String =
    Base16.encode(
      (new ErgoTreeSerializer).deserializeHeaderWithTreeBytes(
        SigmaSerializer.startReader(Base16.decode(ergoTree).get)
      )._3
    )

  it should "insert and find" in new {
    val dao = new OutputsDao
    val inputDao = new InputsDao

    val (headers, _, txs, inputs, outputsWithoutTs, _, _) = PreparedData.data

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

    val withSpent = outputs.map{ o => composite.ExtendedOutput(o, inputs.find(_.boxId == o.boxId).map(_.txId), mainChain = true)}

    dao.findAllByTxsId(txs.map(_.id)).transact(xa).unsafeRunSync() should contain theSameElementsAs outputs
    dao.findAllByTxsIdWithSpent(txs.map(_.id)).transact(xa).unsafeRunSync() should contain theSameElementsAs withSpent

    val inputIds = inputs.map { _.boxId }
    val unspent = outputs.filterNot{ o => inputIds.contains(o.boxId)}
    val unspentSum = unspent.map{_.value}.sum

    dao.sumOfAllUnspentOutputsSince(0L).transact(xa).unsafeRunSync() shouldBe unspentSum

    // Token seller contract from AssetsAtomicExchange
    // http://github.com/ScorexFoundation/sigmastate-interpreter/blob/633efcfd47f2fa4aa240eee2f774cc033cc241a5/contract-verification/src/main/scala/sigmastate/verification/contract/AssetsAtomicExchange.scala#L34-L34
    val treeDexSellerContract = outputs(2).ergoTree
    val treeTemplateDexSellerContract = ergoTreeTemplateBytes(treeDexSellerContract)

    dao.findAllByErgoTreeTemplate(treeTemplateDexSellerContract).transact(xa).unsafeRunSync()
      .map(_.output) should
      contain theSameElementsAs outputs.filter(_.ergoTree == treeDexSellerContract)

    dao.findUnspentByErgoTreeTemplate(treeTemplateDexSellerContract).transact(xa).unsafeRunSync()
      .map(_.output) should
      contain theSameElementsAs outputs.filter(_.ergoTree == treeDexSellerContract)
        .filterNot(o => inputIds.contains(o.boxId))
  }

}
