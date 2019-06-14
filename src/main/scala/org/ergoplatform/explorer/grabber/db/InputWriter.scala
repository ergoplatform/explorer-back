package org.ergoplatform.explorer.grabber.db

import io.circe.Json

object InputWriter extends BasicWriter {

  type ToInsert = (String, String, String, Json)

  val insertSql = "INSERT INTO node_inputs (box_id, tx_id, proof_bytes, extension) VALUES (?, ?, ?, ?)"

}
