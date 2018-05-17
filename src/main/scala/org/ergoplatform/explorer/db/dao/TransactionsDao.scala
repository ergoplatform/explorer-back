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
}
