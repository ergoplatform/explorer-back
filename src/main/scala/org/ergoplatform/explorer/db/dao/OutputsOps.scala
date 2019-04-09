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
    "creation_height",
    "index",
    "ergo_tree",
    "address",
    "assets",
    "additional_registers",
    "timestamp"
  )

  def allFieldsRefFr(ref: String): Fragment = Fragment.const(fields.map(field => s"$ref.$field").mkString(", "))

  private val BYTES = "101004020e36100204a00b08cd0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798ea" +
    "02d192a39a8cc7a7017300730110010204020404040004c0fd4f05808c82f5f6030580b8c9e5ae040580f882ad16040204c0944004c0f407" +
    "040004000580f882ad16d19683030191a38cc7a7019683020193c2b2a57300007473017302830108cdeeac93a38cc7b2a573030001978302" +
    "019683040193b1a5730493c2a7c2b2a573050093958fa3730673079973089c73097e9a730a9d99a3730b730c0599c1a7c1b2a5730d00938c" +
    "c7b2a5730e0001a390c1a7730f"

  val fieldsString: String = fields.mkString(", ")
  val holdersString: String = fields.map(_ => "?").mkString(", ")
  val fieldsFr: Fragment = Fragment.const(fieldsString)

  val insertSql = s"INSERT INTO node_outputs ($fieldsString) VALUES ($holdersString)"

  def findAllByTxId(txId: String): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE tx_id = $txId").query[Output]

  def findAllByTxIdWithSpent(txId: String): Query0[SpentOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
      fr"LEFT JOIN node_transactions t ON i.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
      fr"WHERE o.tx_id = $txId AND (h.main_chain = TRUE OR i.tx_id IS NULL)").query[SpentOutput]

  def findAllByTxsId(txsId: NonEmptyList[String]): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE" ++ Fragments.in(fr"tx_id", txsId)).query[Output]

  def findAllByTxsIdWithSpent(txsId: NonEmptyList[String]): Query0[SpentOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id " ++
      fr"WHERE" ++ Fragments.in(fr"o.tx_id", txsId)).query[SpentOutput]

  def insert: Update[Output] = Update[Output](insertSql)

  def findByAddress(address: String): Query0[SpentOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
      fr"WHERE o.address = $address").query[SpentOutput]

  def findByErgoTree(ergoTree: String): Query0[SpentOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
      fr"WHERE o.ergo_tree = $ergoTree").query[SpentOutput]

  def findUnspentByAddress(address: String): Query0[SpentOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
      fr"WHERE i.box_id IS NULL AND o.address = $address").query[SpentOutput]

  def findUnspentByErgoTree(ergoTree: String): Query0[SpentOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
      fr"WHERE i.box_id IS NULL AND o.ergo_tree = $ergoTree").query[SpentOutput]

  def findByAddressWithSpent(address: String): Query0[SpentOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id " ++
      fr"WHERE o.address = $address").query[SpentOutput]

  /** Search address identifiers by the fragment of the identifier */
  def searchByAddress(substring: String): Query0[String] =
    fr"SELECT address FROM node_outputs WHERE address LIKE ${"%" + substring +"%"}".query[String]

  def sumOfAllUnspentOutputsSince(ts: Long): Query0[Long] =
    (fr"SELECT COALESCE(CAST(SUM(o.value) as BIGINT), 0)" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
      fr"WHERE i.box_id IS NULL AND o.timestamp >= $ts").query[Long]

  def estimatedOutputsSince(ts: Long): Query0[Long] =
    Fragment.const(
      s"""
          SELECT COALESCE(CAST(SUM(o.value) as BIGINT),0)
          FROM node_outputs o
          LEFT JOIN node_inputs i ON (o.box_id = i.box_id AND i.box_id IS NULL)
          WHERE o.address <> '$BYTES' AND o.timestamp >= $ts
      """
    ).query[Long]

  def addressStats(address: String): Query0[AddressSummaryData] =
    fr"""
        SELECT
        o.address as address,
        COUNT(o.tx_id),
        CAST(SUM(CASE WHEN i.box_id IS NOT NULL THEN o.value ELSE 0 END) AS DECIMAL) as spent,
        CAST(SUM(CASE WHEN i.box_id IS NULL THEN o.value ELSE 0 END) AS BIGINT) as unspent
        FROM node_outputs o
        LEFT JOIN node_inputs i ON o.box_id = i.box_id
        WHERE address = $address
        AND TRUE = ANY(
          SELECT h.main_chain FROM node_headers h
          WHERE h.id IN (
            SELECT tx.header_id FROM node_transactions tx
            WHERE tx.id = o.tx_id
          )
        )
        GROUP BY (address)
    """.query[AddressSummaryData]
}
