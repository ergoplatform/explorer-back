package org.ergoplatform.explorer.db.dao

import doobie.{Composite, ConnectionIO, Fragment}
import org.ergoplatform.explorer.db.models.Transaction
import org.ergoplatform.explorer.utils.Paging

class TransactionsDao extends BaseDoobieDao[String, Transaction] {
  override def table: String = "transactions"

  override def fields: Seq[String] = Seq(
    "id",
    "block_id",
    "is_coinbase",
    "ts"
  )

  def findAllByBlockId(blockId: String)(implicit c: Composite[Transaction]): ConnectionIO[List[Transaction]] = {
    (selectAllFromFr ++ Fragment.const(s"WHERE block_id = '$blockId'")).query[Transaction].to[List]
  }

  def countTxsNumbersByBlocksIds(ids: List[String]): ConnectionIO[List[(String, Int)]] = {
    val blockIds = BaseDoobieDao.collectionToInArgument(ids)
    val sql = s"SELECT block_id, count(*) FROM $table WHERE block_id in $blockIds GROUP BY block_id";
    Fragment.const(sql).query[(String, Int)].to[List]
  }

  def getTxsByAddressId(addressId: String, offset: Int = 0, limit: Int = 20): ConnectionIO[List[Transaction]] = {
    val sql =
      s"""
         |SELECT t.id, t.block_id, t.is_coinbase, t.ts
         |FROM transactions t
         |WHERE EXISTS (
         |  SELECT 1
         |  FROM transactions ti LEFT JOIN outputs os
         |  ON t.id = os.tx_id
         |  WHERE os.hash = '$addressId'
         |)
         |ORDER BY t.ts DESC
         |OFFSET $offset LIMIT $limit
         """.stripMargin
    Fragment.const(sql).query[Transaction].to[List]
  }

  def countTxsByAddressId(addressId: String, offset: Int = 0, limit: Int = 20): ConnectionIO[Long] = {
    val sql =
      s"""
         |SELECT COUNT(t.id)
         |FROM transactions t
         |WHERE EXISTS (
         |  SELECT 1
         |  FROM transactions ti LEFT JOIN outputs os
         |  ON t.id = os.tx_id
         |  WHERE os.hash = '$addressId'
         |)
         """.stripMargin
    Fragment.const(sql).query[Long].unique
  }
}
