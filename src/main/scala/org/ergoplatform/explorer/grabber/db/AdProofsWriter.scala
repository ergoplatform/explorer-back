package org.ergoplatform.explorer.grabber.db

object AdProofsWriter extends BasicWriter {

  type ToInsert = (String, String, String)

  val insertSql = "INSERT INTO node_ad_proofs(header_id, proof_bytes, digest) VALUES (?, ?, ?)"

}
