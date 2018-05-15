package org.ergoplatform.explorer.dao

import doobie.Composite
import doobie.free.connection.ConnectionIO
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
    "extension_hash",
    "block_size",
    "equihash_solution",
    "ad_proofs"
  )

  def getLastN(count: Int = 20)
              (implicit e: Composite[Header]): ConnectionIO[List[Header]] = {
    val sql = selectAllFromFr ++ sortBy("height") ++ limitFr(count)
    sql.query[Header].stream.compile.toList
  }

}
