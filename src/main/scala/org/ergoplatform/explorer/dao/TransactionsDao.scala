package org.ergoplatform.explorer.dao

import org.ergoplatform.explorer.models.Transaction

class TransactionsDao extends BaseDoobieDao[String, Transaction] {
  override def table: String = "transactions"

  override def fields: Seq[String] = Seq(
    "id",
    "block_id",
    "is_coinbase"
  )
}
