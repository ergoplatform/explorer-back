package org.ergoplatform.explorer.dao

import org.ergoplatform.explorer.models.Header

class HeadersDao extends BaseDoobieDao[String, Header] {

  override def table: String = "headers"
  override def fields: Seq[String] = Seq(
    "id",
    "parent_id",
    "version",
    "height",
    "ad_proofs_root",
    "state_root",
    "transactions_root",
    "ts",
    "n_bits",
    "nonce",
    "block_size",
    "votes",
    "equihash_solution",
    "ad_proofs"
  )
}
