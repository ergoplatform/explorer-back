package org.ergoplatform.explorer.db.dao

import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.BlockInfo

class BlockInfoDao {


  def find(headerId: String): ConnectionIO[Option[BlockInfo]] = BlockInfoOps.select(headerId).option

  def get(headerId: String): ConnectionIO[BlockInfo] = find(headerId).flatMap{
    case Some(h) => h.pure[ConnectionIO]
    case None => doobie.free.connection.raiseError(
      new NoSuchElementException(s"Cannot find block info for block with id = $headerId")
    )
  }

  def list(headerIds: List[String]): ConnectionIO[List[BlockInfo]] = BlockInfoOps.select(headerIds).to[List]
}
