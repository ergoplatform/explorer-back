package org.ergoplatform.explorer.dao

import org.ergoplatform.explorer.models.Input

class InputsDao extends BaseDoobieDao[String, Input] {
  override def table: String = "inputs"

  override def fields: Seq[String] = Seq(
    "id",
    "tx_id",
    "output",
    "signature"
  )
}
