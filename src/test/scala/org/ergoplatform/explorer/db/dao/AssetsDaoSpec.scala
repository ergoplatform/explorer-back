package org.ergoplatform.explorer.db.dao

import org.ergoplatform.explorer.db.{PreparedDB, PreparedData}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import doobie.implicits._

import scala.util.Try

class AssetsDaoSpec
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll
    with PreparedDB {

  it should "get asset by box id" in {

    val (_, _, _, _, _, _, assets) = PreparedData.data

    val assetsDao = new AssetsDao

    Try(assetsDao.insertMany(assets).transact(xa).unsafeRunSync())

    val asset = assets.head
    val expected = assets.filter(_.boxId == asset.boxId)

    assetsDao.getByBoxId(asset.boxId).transact(xa).unsafeRunSync() should contain theSameElementsAs expected
  }

  it should "get asset holders" in {

    val (h, _, txs, _, outputs, _, assets) = PreparedData.data

    val headersDao = new HeadersDao
    val txsDao = new TransactionsDao
    val outputsDao = new OutputsDao
    val assetsDao = new AssetsDao

    Try {
      Seq(
        headersDao.insertMany(h),
        txsDao.insertMany(txs),
        outputsDao.insertMany(outputs),
        assetsDao.insertMany(assets),
      ).foreach(_.transact(xa).unsafeRunSync())
    }

    val asset = assets.head
    val outputsContainingAsset = assets.filter(_.id == asset.id).map(_.boxId)
    val expected = outputs.filter(x => outputsContainingAsset.contains(x.boxId)).map(_.address)
    val result = assetsDao.holderAddresses(asset.id).transact(xa).unsafeRunSync()

    result should contain theSameElementsAs expected

  }

}
