package org.ergoplatform.explorer.db.dao

import cats.data._
import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.db.models.Input
import org.ergoplatform.explorer.db.models.composite.ExtendedInput

object InputsOps extends DaoOps with JsonMeta {

  val tableName: String = "node_inputs"

  val fields: Seq[String] = Seq(
    "box_id",
    "tx_id",
    "proof_bytes",
    "extension"
  )

  def findAllByTxId(txId: String): Query0[Input] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_inputs WHERE tx_id = $txId").query[Input]

  def findAllByTxsId(txsId: NonEmptyList[String]): Query0[Input] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_inputs WHERE" ++ Fragments.in(fr"tx_id", txsId))
      .query[Input]

  def insert: Update[Input] = Update[Input](insertSql)

  def findAllByTxIdWithValue(txId: String): Query0[ExtendedInput] =
    (fr"SELECT DISTINCT ON (i.box_id) i.box_id, i.tx_id, i.proof_bytes, i.extension, o.value, o.tx_id, o.address" ++
    fr"FROM node_inputs i JOIN node_outputs o ON i.box_id = o.box_id" ++
    fr"WHERE i.tx_id = $txId").query[ExtendedInput]

  def findAllByTxsIdWithValue(txsId: NonEmptyList[String]): Query0[ExtendedInput] =
    (fr"SELECT DISTINCT ON (i.box_id) i.box_id, i.tx_id, i.proof_bytes, i.extension, o.value, o.tx_id, o.address" ++
    fr"FROM node_inputs i LEFT JOIN node_outputs o ON i.box_id = o.box_id" ++
    fr"WHERE" ++ Fragments.in(fr"i.tx_id", txsId)).query[ExtendedInput]

}
