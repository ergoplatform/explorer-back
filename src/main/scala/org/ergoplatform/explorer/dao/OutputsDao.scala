package org.ergoplatform.explorer.dao

import org.ergoplatform.explorer.models.Output

class OutputsDao extends BaseDoobieDao[String, Output] {
  override def table: String = "outputs"

  override def fields: Seq[String] = Seq(
    "id",
    "tx_id",
    "value",
    "script"
  )
}
