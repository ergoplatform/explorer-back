package org.ergoplatform.explorer.services

import cats.effect.IO
import doobie.implicits._
import org.ergoplatform.explorer.db.PreparedDB
import org.ergoplatform.explorer.db.dao.MinerDao
import org.ergoplatform.explorer.db.models.Miner
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class MinerServiceSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "search for known miners names" in {

    val ec = scala.concurrent.ExecutionContext.Implicits.global

    val dao = new MinerDao

    val miner1 = Miner("011111", "hi!")
    val miner2 = Miner("011112", "hello!")

    dao.insertMany(List(miner1, miner2)).transact(xa).unsafeRunSync()

    val service = new MinerServiceImpl[IO](xa, ec)

    service.searchAddress("01111").unsafeRunSync() should contain theSameElementsAs List(
      miner1,
      miner2
    ).map(_.address)
    service.searchAddress("00000000000000000000").unsafeRunSync() shouldBe empty
    service.searchAddress("011111").unsafeRunSync() should contain theSameElementsAs List(miner1)
      .map(_.address)
    service.searchAddress("011112").unsafeRunSync() should contain theSameElementsAs List(miner2)
      .map(_.address)
    service.searchAddress("0111110").unsafeRunSync() shouldBe empty
  }

}
