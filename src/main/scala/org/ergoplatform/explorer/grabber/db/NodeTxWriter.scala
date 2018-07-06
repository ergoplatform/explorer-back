package org.ergoplatform.explorer.grabber.db

object NodeTxWriter  extends BasicWriter {

  type ToInsert = (String, String, Boolean, Long, Long)

  val insertSql = "INSERT INTO node_transactions (id, header_id, coinbase, timestamp, size) VALUES (?, ?, ?, ?, ?)"

}
