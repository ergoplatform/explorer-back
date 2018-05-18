package org.ergoplatform.explorer.db.dao

import doobie.{Composite, ConnectionIO, Fragment}
import org.ergoplatform.explorer.db.models.Transaction

class TransactionsDao extends BaseDoobieDao[String, Transaction] {
  override def table: String = "transactions"

  override def fields: Seq[String] = Seq(
    "id",
    "block_id",
    "is_coinbase"
  )

  def findAllByBLockId(blockId: String)(implicit c: Composite[Transaction]): ConnectionIO[List[Transaction]] = {
    (selectAllFromFr ++ Fragment.const(s"WHERE block_id = '$blockId'")).query[Transaction].to[List]
  }

  def countTxsNumbersByBlocksIds(ids: List[String]): ConnectionIO[List[(String, Int)]] = {
    val blockIds = ids.map(v => "'" + v + "'").mkString("(", ", ", ")")
    val sql = s"SELECT block_id, count(*) FROM $table WHERE block_id in $blockIds GROUP BY block_id";
    Fragment.const(sql).query[(String, Int)].to[List]
  }
}
