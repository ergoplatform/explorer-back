package org.ergoplatform.explorer.db.dao

import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import org.ergoplatform.explorer.db.models.Asset

class AssetsDao {

  def getByBoxId(boxId: String): ConnectionIO[List[Asset]] =
    AssetsOps.getByBoxId(boxId).to[List]

  def insertMany(list: List[Asset]): ConnectionIO[List[Asset]] =
    AssetsOps.insert
      .updateManyWithGeneratedKeys[Asset](AssetsOps.fields: _*)(list)
      .compile
      .to[List]

  def holderAddresses(
    assetId: String,
    offset: Int = 0,
    limit: Int = 20
  ): ConnectionIO[List[String]] =
    AssetsOps.holderAddresses(assetId, offset, limit).to[List]

}
