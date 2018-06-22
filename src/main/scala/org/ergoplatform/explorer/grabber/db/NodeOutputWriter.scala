package org.ergoplatform.explorer.grabber.db

import io.circe.Json

object NodeOutputWriter extends BasicWriter {

  type ToInsert = (String, String, Long, Int, String, String, Json)

  val insertSql = "INSERT INTO node_outputs (box_id, tx_id, value, index, proposition, hash, additional_registers)" +
    " VALUES (?, ?, ?, ?, ?, ?, ?)"
}
