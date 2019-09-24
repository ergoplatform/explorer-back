package org.ergoplatform.explorer.services

import cats.effect.IO
import cats.effect.concurrent.Ref
import doobie.implicits._
import org.ergoplatform.explorer.config.ProtocolConfig
import org.ergoplatform.explorer.db.dao.{HeadersDao, InputsDao, OutputsDao, TransactionsDao}
import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.ergoplatform.explorer.grabber.protocol.ApiAsset
import org.ergoplatform.explorer.http.protocol.AddressInfo
import org.ergoplatform.explorer.persistence.TransactionsPool
import org.ergoplatform.settings.MonetarySettings
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.Random

class AddressesServiceSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "search address by substring and get address info by full id" in {

    val ec = scala.concurrent.ExecutionContext.Implicits.global

    val (h, _, tx, inputs, outputs, _, _) = PreparedData.data

    val hDao = new HeadersDao
    val tDao = new TransactionsDao
    val iDao = new InputsDao
    val oDao = new OutputsDao

    hDao.insertMany(h).transact(xa).unsafeRunSync()
    tDao.insertMany(tx).transact(xa).unsafeRunSync()
    oDao.insertMany(outputs).transact(xa).unsafeRunSync()
    iDao.insertMany(inputs).transact(xa).unsafeRunSync()

    val offChainStore = Ref.of[IO, TransactionsPool](TransactionsPool.empty).unsafeRunSync()

    val cfg = ProtocolConfig(testnet = true, "", monetary = MonetarySettings())

    val service = new AddressesServiceImpl[IO](xa, offChainStore, ec, cfg)

    val random = Random.shuffle(outputs).head.address

    val addressInfo = service.getAddressInfo(random).unsafeRunSync()

    val expected = {
      val id = random
      val txsCount = outputs.count(_.address == random)
      val totalReceived = outputs.filter(_.address == random).map(_.value).sum
      val inputsBoxIds = inputs.map(_.boxId)
      val balance = outputs
        .filter { o =>
          o.address == random && !inputsBoxIds.contains(o.boxId)
        }
        .map(_.value)
        .sum
      val tokensBalance = outputs
        .filter(o => o.address == random && !inputsBoxIds.contains(o.boxId))
        .foldLeft(Map.empty[String, Long]) {
          case (acc, _) =>
            acc
        }
      val assets = tokensBalance.map(x => ApiAsset(x._1, x._2)).toList
      AddressInfo(id, txsCount, totalReceived, balance, balance, assets, assets)
    }

    addressInfo shouldBe expected

    val random2 = Random.shuffle(outputs).head.address.take(6)

    val expected2 = outputs.filter(_.address.startsWith(random2)).map(_.address)
    val searchResults = service.searchById(random2).unsafeRunSync()

    searchResults should contain theSameElementsAs expected2
  }
}
