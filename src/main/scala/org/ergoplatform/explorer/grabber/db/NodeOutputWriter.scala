package org.ergoplatform.explorer.grabber.db

import io.circe.Json

object NodeOutputWriter extends BasicWriter {

  type ToInsert = (String, String, Long, Int, Int, String, String, Json, Json, Long)

  val insertSql: String = "INSERT INTO node_outputs (box_id, tx_id, value, creation_height, index," +
    " ergo_tree, address, assets, additional_registers, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
}
