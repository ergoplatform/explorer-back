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

  def findByBoxId(boxId: String): Query0[ExtendedOutput] =
    (fr"SELECT" ++ allFieldsRefFr("o") ++ fr", i.tx_id, h.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t ON o.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE o.box_id = $boxId").query[ExtendedOutput]

  def findAllByTxId(txId: String): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE tx_id = $txId").query[Output]

  def findAllByTxIdWithSpent(txId: String): Query0[ExtendedOutput] =
    (fr"SELECT" ++ allFieldsRefFr("o") ++ fr", i.tx_id, h.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t ON o.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE o.tx_id = $txId AND (h.main_chain = TRUE OR i.tx_id IS NULL)").query[ExtendedOutput]

  def findAllByTxsId(txsId: NonEmptyList[String]): Query0[Output] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_outputs WHERE" ++ Fragments.in(fr"tx_id", txsId))
      .query[Output]

  def findAllByTxsIdWithSpent(txsId: NonEmptyList[String]): Query0[ExtendedOutput] =
    (fr"SELECT" ++ allFieldsRefFr("o") ++ fr", i.tx_id, h.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id " ++
    fr"LEFT JOIN node_transactions t ON o.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE" ++ Fragments.in(fr"o.tx_id", txsId)).query[ExtendedOutput]

  def insert: Update[Output] = Update[Output](insertSql)

  def findByAddress(address: String): Query0[ExtendedOutput] =
    (fr"SELECT" ++ allFieldsRefFr("o") ++ fr", i.tx_id, h.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t ON o.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE o.address = $address").query[ExtendedOutput]

  def findByErgoTree(ergoTree: String): Query0[ExtendedOutput] =
    (fr"SELECT" ++ allFieldsRefFr("o") ++ fr", i.tx_id, h.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t ON o.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE o.ergo_tree = $ergoTree").query[ExtendedOutput]

  /** Finds all outputs that are protected with given ergo tree template
    * see https://github.com/ScorexFoundation/sigmastate-interpreter/issues/264
    * http://github.com/ScorexFoundation/sigmastate-interpreter/blob/633efcfd47f2fa4aa240eee2f774cc033cc241a5/sigmastate/src/main/scala/sigmastate/Values.scala#L828-L828
    *
    * Based on [[findByErgoTree]] with ergo_tree clause changed to a string suffix match instead of an exact match.
    *
    * @param ergoTreeTemplate Base16 encoded bytes of serialized ErgoTree prop after constant segregation
    * (see http://github.com/ScorexFoundation/sigmastate-interpreter/blob/633efcfd47f2fa4aa240eee2f774cc033cc241a5/sigmastate/src/main/scala/sigmastate/serialization/ErgoTreeSerializer.scala#L226-L226 )
    * @return
    */
  def findByErgoTreeTemplate(ergoTreeTemplate: String): Query0[ExtendedOutput] =
    (fr"SELECT" ++ allFieldsRefFr("o") ++ fr", i.tx_id, h.main_chain" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
      fr"LEFT JOIN node_transactions t ON o.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
      fr"WHERE o.ergo_tree LIKE ${"%" + ergoTreeTemplate}").query[ExtendedOutput]

  def findUnspentByAddress(address: String): Query0[ExtendedOutput] =
    (fr"SELECT" ++ allFieldsRefFr("o") ++ fr", i.tx_id, h_in.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t_in ON o.tx_id = t_in.id LEFT JOIN node_headers h_in ON h_in.id = t_in.header_id" ++
    fr"LEFT JOIN node_transactions t ON i.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE h_in.main_chain = TRUE AND (i.box_id IS NULL OR h.main_chain = FALSE) AND o.address = $address")
      .query[ExtendedOutput]

  def findUnspentByErgoTree(ergoTree: String): Query0[ExtendedOutput] =
    (fr"SELECT" ++ allFieldsRefFr("o") ++ fr", i.tx_id, h_in.main_chain" ++
    fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN node_transactions t_in ON o.tx_id = t_in.id LEFT JOIN node_headers h_in ON h_in.id = t_in.header_id" ++
    fr"LEFT JOIN node_transactions t ON i.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
    fr"WHERE h_in.main_chain = TRUE AND (i.box_id IS NULL OR h.main_chain = FALSE) AND o.ergo_tree = $ergoTree")
      .query[ExtendedOutput]

  /** Finds unspent outputs that are protected with given ergo tree template
    * see https://github.com/ScorexFoundation/sigmastate-interpreter/issues/264
    * http://github.com/ScorexFoundation/sigmastate-interpreter/blob/633efcfd47f2fa4aa240eee2f774cc033cc241a5/sigmastate/src/main/scala/sigmastate/Values.scala#L828-L828
    *
    * Based on [[findUnspentByErgoTree]] with ergo_tree clause changed to a string suffix match instead of an exact match.
    * @param ergoTreeTemplate Base16 encoded bytes of serialized ErgoTree prop after constant segregation
    * (see http://github.com/ScorexFoundation/sigmastate-interpreter/blob/633efcfd47f2fa4aa240eee2f774cc033cc241a5/sigmastate/src/main/scala/sigmastate/serialization/ErgoTreeSerializer.scala#L226-L226 )
    * @return
    */
  def findUnspentByErgoTreeTemplate(ergoTreeTemplate: String): Query0[ExtendedOutput] =
    (fr"SELECT" ++ allFieldsRefFr("o") ++ fr", i.tx_id, h_in.main_chain" ++
      fr"FROM node_outputs o LEFT JOIN node_inputs i ON o.box_id = i.box_id" ++
      fr"LEFT JOIN node_transactions t_in ON o.tx_id = t_in.id LEFT JOIN node_headers h_in ON h_in.id = t_in.header_id" ++
      fr"LEFT JOIN node_transactions t ON i.tx_id = t.id LEFT JOIN node_headers h ON h.id = t.header_id" ++
      fr"WHERE h_in.main_chain = TRUE AND (i.box_id IS NULL OR h.main_chain = FALSE) AND o.ergo_tree LIKE ${"%" + ergoTreeTemplate}")
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

  def estimatedOutputsSince(ts: Long)(genesisAddress: String): Query0[BigDecimal] =
    Fragment
      .const(
        s"""
          SELECT COALESCE(CAST(SUM(o.value) as DECIMAL),0)
          FROM node_outputs o
          LEFT JOIN node_inputs i ON (o.box_id = i.box_id AND i.box_id IS NULL)
          WHERE o.address <> '$genesisAddress' AND o.timestamp >= $ts
      """
      )
      .query[BigDecimal]

}
