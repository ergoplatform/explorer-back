package org.ergoplatform.explorer.db.dao

import cats.data._
import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.db.models.Output
import org.ergoplatform.explorer.db.models.composite.ExtendedOutput

object OutputsOps extends DaoOps with JsonMeta {

  val tableName: String = "node_outputs"

  val fields: Seq[String] = Seq(
    "box_id",
    "tx_id",
    "value",
    "creation_height",
    "index",
    "ergo_tree",
    "address",
    "additional_registers",
    "timestamp"
  )

  private val GenesisAddress = "101004020e36100204a00b08cd0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798ea" +
  "02d192a39a8cc7a7017300730110010204020404040004c0fd4f05808c82f5f6030580b8c9e5ae040580f882ad16040204c0944004c0f407" +
  "040004000580f882ad16d19683030191a38cc7a7019683020193c2b2a57300007473017302830108cdeeac93a38cc7b2a573030001978302" +
  "019683040193b1a5730493c2a7c2b2a573050093958fa3730673079973089c73097e9a730a9d99a3730b730c0599c1a7c1b2a5730d00938c" +
  "c7b2a5730e0001a390c1a7730f"

  def findByBoxId(boxId: String): Query0[ExtendedOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id, h.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t ON o.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE o.box_id = $boxId").query[ExtendedOutput]

  def findAllByTxId(txId: String): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE tx_id = $txId").query[Output]

  def findAllByTxIdWithSpent(txId: String): Query0[ExtendedOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id, h.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t ON o.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE o.tx_id = $txId AND (h.main_chain = TRUE OR i.tx_id IS NULL)").query[ExtendedOutput]

  def findAllByTxsId(txsId: NonEmptyList[String]): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE" ++ Fragments.in(fr"tx_id", txsId))
      .query[Output]

  def findAllByTxsIdWithSpent(txsId: NonEmptyList[String]): Query0[ExtendedOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id, h.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id " ++
    fr"LEFT JOIN node_transactions t ON o.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE" ++ Fragments.in(fr"o.tx_id", txsId)).query[ExtendedOutput]

  def insert: Update[Output] = Update[Output](insertSql)

  def findByAddress(address: String): Query0[ExtendedOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id, h.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t ON o.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE o.address = $address").query[ExtendedOutput]

  def findByErgoTree(ergoTree: String): Query0[ExtendedOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id, h.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t ON o.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE o.ergo_tree = $ergoTree").query[ExtendedOutput]

  def findUnspentByAddress(address: String): Query0[ExtendedOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id, h_in.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t_in ON o.tx_id = t_in.id LEFT JOIN node_headers h_in ON h_in.id = t_in.header_id" ++
    fr"LEFT JOIN node_transactions t ON i.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE (i.box_id IS NULL OR h.main_chain = FALSE) AND o.address = $address")
      .query[ExtendedOutput]

  def findUnspentByErgoTree(ergoTree: String): Query0[ExtendedOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id, h_in.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t_in ON o.tx_id = t_in.id LEFT JOIN node_headers h_in ON h_in.id = t_in.header_id" ++
    fr"LEFT JOIN node_transactions t ON i.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE (i.box_id IS NULL OR h.main_chain = FALSE) AND o.ergo_tree = $ergoTree")
      .query[ExtendedOutput]

  def findByAddressWithSpent(address: String): Query0[ExtendedOutput] =
    (fr"SELECT " ++ allFieldsRefFr("o") ++ fr", i.tx_id, h.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id " ++
    fr"LEFT JOIN node_transactions t ON o.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE o.address = $address").query[ExtendedOutput]

  /** Search address identifiers by the fragment of the identifier */
  def searchByAddress(substring: String): Query0[String] =
    fr"SELECT address FROM node_outputs WHERE address LIKE ${"%" + substring + "%"}".query[String]

  def sumOfAllUnspentOutputsSince(ts: Long): Query0[Long] =
    (fr"SELECT COALESCE(CAST(SUM(o.value) as BIGINT), 0)" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"WHERE i.box_id IS NULL AND o.timestamp >= $ts").query[Long]

  def estimatedOutputsSince(ts: Long): Query0[BigDecimal] =
    Fragment
      .const(
        s"""
          SELECT COALESCE(CAST(SUM(o.value) as DECIMAL),0)
          FROM node_outputs o
          LEFT JOIN node_inputs i ON (o.box_id = i.box_id AND i.box_id IS NULL)
          WHERE o.address <> '$GenesisAddress' AND o.timestamp >= $ts
      """
      )
      .query[BigDecimal]

}
