package org.ergoplatform.explorer.db.dao

import cats.data._
import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.db.models.{AddressSummaryData, Output, SpentOutput}

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

  def findByHashWithSpent(hash: String): Query0[SpentOutput] =
    (fr"SELECT o.box_id, o.tx_id, o.value, o.index, o.proposition, o.hash, o.additional_registers, i.tx_id" ++
     fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id " ++
     fr"WHERE o.hash = $hash").query[SpentOutput]

  /** Search address identifiers by the fragment of the identifier */
  def searchByHash(substring: String): Query0[String] = {
    fr"SELECT hash FROM node_outputs WHERE hash LIKE ${"%" + substring +"%"}".query[String]
  }

  def sumOfAllUnspentOutputsSince(ts: Long): Query0[Long] =
   (fr"SELECT COALESCE(CAST(SUM(o.value) as BIGINT), 0)" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t ON o.tx_id = t.id" ++
    fr"WHERE i.box_id IS NULL AND t.timestamp >= $ts").query[Long]

  //TODO: Make proper value evaluation, without tons of joins
  def estimatedOutputsSince(ts: Long): Query0[Long] = {
    Fragment.const(s"""
                  SELECT COALESCE(CAST(SUM(op.value) as BIGINT),0)
                  FROM node_outputs o
                  LEFT JOIN node_inputs i ON o.box_id = i.box_id
                  LEFT JOIN node_outputs op ON i.tx_id = op.tx_id
                  LEFT JOIN node_inputs ip ON op.box_id = ip.box_id
                  LEFT JOIN node_transactions t ON o.tx_id = t.id
                  WHERE o.hash <> op.hash AND ip.box_id IS NULL AND t.timestamp >= $ts""").query[Long]
  }

  def addressStats(hash: String): Query0[AddressSummaryData] =
    fr"""
        SELECT
        o.hash as hash,
        COUNT(o.tx_id),
        CAST(SUM(CASE WHEN i.box_id IS NOT NULL THEN o.value ELSE 0 END) AS BIGINT) as spent,
        CAST(SUM(CASE WHEN i.box_id IS NULL THEN o.value ELSE 0 END) AS BIGINT) as unspent
        FROM node_outputs o
        LEFT JOIN node_inputs i ON o.box_id = i.box_id
        WHERE hash = $hash
        GROUP BY (hash)""".query[AddressSummaryData]

}
