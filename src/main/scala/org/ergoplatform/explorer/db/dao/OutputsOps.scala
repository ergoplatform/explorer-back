package org.ergoplatform.explorer.db.dao

import cats.data._
import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.db.models.Output

object OutputsOps extends JsonMeta {

  val fields: Seq[String] = Seq(
    "box_id",
    "tx_id",
    "value",
    "index",
    "proposition",
    "hash",
    "additional_registers"
  )

  val fieldsString = fields.mkString(", ")
  val holdersString = fields.map(_ => "?").mkString(", ")
  val fieldsFr = Fragment.const(fieldsString)

  val insertSql = s"INSERT INTO node_outputs ($fieldsString) VALUES ($holdersString)"

  def findAllByTxId(txId: String)(implicit c: Composite[Output]): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE tx_id = $txId").query[Output]


  def findAllByTxsId(txsId: NonEmptyList[String])(implicit c: Composite[Output]): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE" ++ Fragments.in(fr"tx_id", txsId)).query[Output]

  def insert: Update[Output] = Update[Output](insertSql)

  def findByHash(hash: String)(implicit c: Composite[Output]): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE hash = $hash").query[Output]

  /** Search address identifiers by the fragment of the identifier */
  def searchByHash(substring: String): Query0[String] = {
    fr"SELECT hash FROM outputs WHERE hash LIKE ${"%" + substring +"%"}".query[String]
  }

}
