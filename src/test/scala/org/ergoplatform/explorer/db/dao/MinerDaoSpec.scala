package org.ergoplatform.explorer.db.dao

import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.PreparedDB
import org.ergoplatform.explorer.db.models.Miner
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class MinerDaoSpec extends FlatSpec with Matchers with BeforeAndAfterAll with PreparedDB {

  it should "perform crud operations on miners" in {

    val dao = new MinerDao

    val miner1 = Miner("1", "1")
    val miner2 = Miner("2", "2")
    val miner3 = Miner("3", "3")
    val miner4 = Miner("4", "4")
    val miner5 = Miner("5", "5")

    dao.insert(miner1).transact(xa).unsafeRunSync() shouldBe miner1

    dao.find(miner1.address).transact(xa).unsafeRunSync() shouldBe Some(miner1)
    dao.find(miner2.address).transact(xa).unsafeRunSync() shouldBe None

    val savedList = dao.insertMany(List(miner2, miner3, miner4, miner5)).transact(xa).unsafeRunSync()
    savedList should contain theSameElementsAs List(miner2, miner3, miner4, miner5)

    val updated = dao.update(miner1.copy(name = "not 1")).transact(xa).unsafeRunSync()
    updated shouldBe Miner(miner1.address, "not 1")

    noException should be thrownBy dao.delete(miner1.address).transact(xa).unsafeRunSync()
    noException should be thrownBy dao.delete(miner1.address).transact(xa).unsafeRunSync()

    dao.find(miner1.address).transact(xa).unsafeRunSync() shouldBe None

  }

  it should "search address" in {
    val m1 = Miner("1234", "1")
    val m2 = Miner("2345", "2")
    val m3 = Miner("3456", "3")

    val dao = new MinerDao
    dao.insertMany(List(m1, m2, m3)).transact(xa).unsafeRunSync()

    dao.searchAddress("234").transact(xa).unsafeRunSync() should contain theSameElementsAs List(m1, m2).map(_.address)
    dao.searchAddress("2345").transact(xa).unsafeRunSync() should contain only(m2.address)
    dao.searchAddress("12345").transact(xa).unsafeRunSync() shouldBe empty
  }

}
