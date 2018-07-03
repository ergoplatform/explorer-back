package org.ergoplatform.explorer.db.dao

import cats.data._
import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.db.models.{Output, SpentOutput}

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

  def findAllByTxId(txId: String): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE tx_id = $txId").query[Output]

  def findAllByTxIdWithSpent(txId: String): Query0[SpentOutput] =
    (fr"SELECT o.box_id, o.tx_id, o.value, o.index, o.proposition, o.hash, o.additional_registers, i.tx_id" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id WHERE o.tx_id = $txId").query[SpentOutput]

  def findAllByTxsId(txsId: NonEmptyList[String]): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE" ++ Fragments.in(fr"tx_id", txsId)).query[Output]

  def findAllByTxsIdWithSpent(txsId: NonEmptyList[String]): Query0[SpentOutput] =
    (fr"SELECT o.box_id, o.tx_id, o.value, o.index, o.proposition, o.hash, o.additional_registers, i.tx_id" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id " ++
      fr"WHERE" ++ Fragments.in(fr"o.tx_id", txsId)).query[SpentOutput]

  def insert: Update[Output] = Update[Output](insertSql)

  def findByHash(hash: String): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE hash = $hash").query[Output]

  /** Search address identifiers by the fragment of the identifier */
  def searchByHash(substring: String): Query0[String] = {
    fr"SELECT hash FROM node_outputs WHERE hash LIKE ${"%" + substring +"%"}".query[String]
  }

  def sumOfAllUnspentOutputs: Query0[Long] = {
    fr"SELECT COALESCE(CAST(SUM(o.value) as BIGINT), 0) FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id WHERE i.box_id IS NULL"
      .query[Long]
  }

  //TODO: Make proper value evaluation, without tons of joins
  def estimatedOutputs: Query0[Long] = {
    Fragment.const("""
                  SELECT COALESCE(CAST(SUM(op.value) as BIGINT),0)
                  FROM node_outputs o
                  LEFT JOIN node_inputs i ON o.box_id = i.box_id
                  LEFT JOIN node_outputs op ON i.tx_id = op.tx_id
                  LEFT JOIN node_inputs ip ON op.box_id = ip.box_id
                  WHERE o.hash <> op.hash AND ip.box_id IS NULL""").query[Long]
  }

}
