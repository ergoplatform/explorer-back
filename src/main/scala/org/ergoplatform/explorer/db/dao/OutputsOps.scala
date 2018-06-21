package org.ergoplatform.explorer.db.dao

import cats.data._
import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.models.Output

object OutputsOps {

  val fields: Seq[String] = Seq(
    "id",
    "tx_id",
    "value",
    "script",
    "hash"
  )

  val insertSql = s"INSERT INTO outputs (id, tx_id, value, script, hash) VALUES (?, ?, ?, ?, ?)"

  def findAllByTxId(txId: String)(implicit c: Composite[Output]): Query0[Output] =
    fr"SELECT id, tx_id, value, script, hash FROM outputs WHERE tx_id = $txId".query[Output]


  def findAllByTxsId(txsId: NonEmptyList[String])(implicit c: Composite[Output]): Query0[Output] =
    (fr"SELECT id, tx_id, value, script, hash FROM outputs WHERE" ++ Fragments.in(fr"tx_id", txsId)).query[Output]

  def insert: Update[Output] = Update[Output](insertSql)

  def findByHash(hash: String)(implicit c: Composite[Output]): Query0[Output] =
    fr"SELECT id, tx_id, value, script, hash FROM outputs WHERE hash = $hash".query[Output]

  /** Search address identifiers by the fragment of the identifier */
  def searchByHash(substring: String): Query0[String] = {
    fr"SELECT hash FROM outputs WHERE hash LIKE ${"%" + substring +"%"}".query[String]
  }

}
