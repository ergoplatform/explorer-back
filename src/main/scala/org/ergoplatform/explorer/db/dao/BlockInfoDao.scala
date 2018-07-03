package org.ergoplatform.explorer.db.dao

import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import org.ergoplatform.explorer.db.models.BlockInfo

class BlockInfoDao {

  val fields = BlockInfoOps.fields


  def find(headerId: String): ConnectionIO[Option[BlockInfo]] = BlockInfoOps.select(headerId).option

  def get(headerId: String): ConnectionIO[BlockInfo] = find(headerId).flatMap{
    case Some(h) => h.pure[ConnectionIO]
    case None => doobie.free.connection.raiseError(
      new NoSuchElementException(s"Cannot find block info for block with id = $headerId")
    )
  }

  def insert(bi: BlockInfo): ConnectionIO[BlockInfo] = {
    BlockInfoOps
      .insert
      .withUniqueGeneratedKeys[BlockInfo](fields: _*)(bi)
  }

  def insertMany(list: List[BlockInfo]): ConnectionIO[List[BlockInfo]] = {
    BlockInfoOps
      .insert
      .updateManyWithGeneratedKeys[BlockInfo](fields: _*)(list)
      .compile
      .to[List]
  }

  def list(headerIds: List[String]): ConnectionIO[List[BlockInfo]] = BlockInfoOps.select(headerIds).to[List]

  def findLast: ConnectionIO[Option[BlockInfo]] = BlockInfoOps.findLast(1).option

  def totalCoinsGroupedByDay(lastDays: Int): ConnectionIO[List[BlockInfoOps.SingleDataType]] =
    BlockInfoOps.totalCoinsGroupedByDay(lastDays).to[List]

  def avgBlockSizeGroupedByDay(lastDays: Int): ConnectionIO[List[BlockInfoOps.SingleDataType]] =
    BlockInfoOps.avgBlockSizeGroupedByDay(lastDays).to[List]

  def avgTxsCountGroupedByDay(lastDays: Int): ConnectionIO[List[BlockInfoOps.SingleDataType]] =
    BlockInfoOps.avgTxsGroupedByDay(lastDays).to[List]

  def blockchainSizeGroupedByDay(lastDays: Int): ConnectionIO[List[BlockInfoOps.SingleDataType]] =
    BlockInfoOps.totalBlockchainSizeGroupedByDay(lastDays).to[List]

  def avgDifficultyGroupedByDay(lastDays: Int): ConnectionIO[List[BlockInfoOps.SingleDataType]] =
    BlockInfoOps.avgDifficultyGroupedByDay(lastDays).to[List]

  def minerRevenueGroupedByDay(lastDays: Int): ConnectionIO[List[BlockInfoOps.SingleDataType]] =
    BlockInfoOps.minerRevenueGroupedByDay(lastDays).to[List]

  def sumDifficultiesGroupedByDay(lastDays: Int): ConnectionIO[List[BlockInfoOps.SingleDataType]] =
    BlockInfoOps.sumDifficultyGroupedByDay(lastDays).to[List]

  def difficultiesSumSince(ts: Long): ConnectionIO[Long] = BlockInfoOps.difficultiesSumSince(ts).unique

  def circulatingSupplySince(ts: Long): ConnectionIO[Long] = BlockInfoOps.circulatingSupplySince(ts).unique
}
