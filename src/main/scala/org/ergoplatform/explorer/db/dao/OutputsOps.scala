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
    "additional_registers",
    "timestamp"
  )

  private val BYTES = "968400020191a3c6a70300059784000201968400030193c2a7c2b2a505000000000000000093958fa30500000000000027600500000001bf08eb00990500000001bf08eb009c050000000011e1a3009a0500000000000000019d99a305000000000000276005000000000000087099c1a7c1b2a505000000000000000093c6b2a5050000000000000000030005a390c1a7050000000011e1a300"

  val fieldsString = fields.mkString(", ")
  val holdersString = fields.map(_ => "?").mkString(", ")
  val fieldsFr = Fragment.const(fieldsString)

  val insertSql = s"INSERT INTO node_outputs ($fieldsString) VALUES ($holdersString)"

  def findAllByTxId(txId: String): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE tx_id = $txId").query[Output]

  def findAllByTxIdWithSpent(txId: String): Query0[SpentOutput] =
    (fr"SELECT o.box_id, o.tx_id, o.value, o.index, o.proposition, o.hash, o.additional_registers, o.timestamp, i.tx_id" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
      fr"LEFT JOIN node_transactions t ON i.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
      fr"WHERE o.tx_id = $txId AND (h.main_chain = TRUE OR i.tx_id IS NULL)").query[SpentOutput]

  def findAllByTxsId(txsId: NonEmptyList[String]): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE" ++ Fragments.in(fr"tx_id", txsId)).query[Output]

  def findAllByTxsIdWithSpent(txsId: NonEmptyList[String]): Query0[SpentOutput] =
    (fr"SELECT o.box_id, o.tx_id, o.value, o.index, o.proposition, o.hash, o.additional_registers, o.timestamp, i.tx_id" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id " ++
      fr"WHERE" ++ Fragments.in(fr"o.tx_id", txsId)).query[SpentOutput]

  def insert: Update[Output] = Update[Output](insertSql)

  def findByHash(hash: String): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE hash = $hash").query[Output]

  def findByHashWithSpent(hash: String): Query0[SpentOutput] =
    (fr"SELECT o.box_id, o.tx_id, o.value, o.index, o.proposition, o.hash, o.additional_registers, o.timestamp, i.tx_id" ++
     fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id " ++
     fr"WHERE o.hash = $hash").query[SpentOutput]

  /** Search address identifiers by the fragment of the identifier */
  def searchByHash(substring: String): Query0[String] = {
    fr"SELECT hash FROM node_outputs WHERE hash LIKE ${"%" + substring +"%"}".query[String]
  }

  def sumOfAllUnspentOutputsSince(ts: Long): Query0[Long] =
   (fr"SELECT COALESCE(CAST(SUM(o.value) as BIGINT), 0)" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"WHERE i.box_id IS NULL AND o.timestamp >= $ts").query[Long]

  def estimatedOutputsSince(ts: Long): Query0[Long] = {
    Fragment.const(s"""
                  SELECT COALESCE(CAST(SUM(o.value) as BIGINT),0)
                  FROM node_outputs o
                  LEFT JOIN node_inputs i ON (o.box_id = i.box_id AND i.box_id IS NULL)
                  WHERE o.hash <> '$BYTES' AND o.timestamp >= $ts""").query[Long]
  }

  def addressStats(hash: String): Query0[AddressSummaryData] =
    fr"""
        SELECT
        o.hash as hash,
        COUNT(o.tx_id),
        CAST(SUM(CASE WHEN i.box_id IS NOT NULL THEN o.value ELSE 0 END) AS DECIMAL) as spent,
        CAST(SUM(CASE WHEN i.box_id IS NULL THEN o.value ELSE 0 END) AS BIGINT) as unspent
        FROM node_outputs o
        LEFT JOIN node_inputs i ON o.box_id = i.box_id
        WHERE hash = $hash
        AND (
          SELECT h.main_chain FROM node_headers h
          WHERE h.id = (
            SELECT tx.header_id FROM node_transactions tx
            WHERE tx.id = o.tx_id
          )
        ) = TRUE
        GROUP BY (hash)""".query[AddressSummaryData]

}
