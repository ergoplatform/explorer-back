package org.ergoplatform.explorer.db.dao

import cats.data._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.db.models.{Input, InputWithOutputInfo}

object InputsOps extends JsonMeta {

  val fields: Seq[String] = Seq(
    "box_id",
    "tx_id",
    "proof_bytes",
    "extension"
  )

  val fieldsString = fields.mkString(", ")
  val holdersString = fields.map(_ => "?").mkString(", ")
  val fieldsFr = Fragment.const(fieldsString)

  val insertSql = s"INSERT INTO node_inputs ($fieldsString) VALUES ($holdersString)"

  def findAllByTxId(txId: String)(implicit c: Composite[Input]): Query0[Input] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_inputs WHERE tx_id = $txId").query[Input]


  def findAllByTxsId(txsId: NonEmptyList[String])(implicit c: Composite[Input]): Query0[Input] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_inputs WHERE" ++ Fragments.in(fr"tx_id", txsId)).query[Input]

  def insert: Update[Input] = Update[Input](insertSql)

  def findAllByTxIdWithValue(txId: String)(implicit c: Composite[InputWithOutputInfo]): Query0[InputWithOutputInfo] =
    (fr"SELECT i.box_id, i.tx_id, i.proof_bytes, i.extension, o.value, o.tx_id" ++
      fr"FROM node_inputs i JOIN node_outputs o ON i.box_id = o.box_id" ++
      fr"WHERE i.tx_id = $txId").query[InputWithOutputInfo]


  def findAllByTxsIdWithValue(txsId: NonEmptyList[String])(implicit c: Composite[InputWithOutputInfo]): Query0[InputWithOutputInfo] =
    (fr"SELECT i.box_id, i.tx_id, i.proof_bytes, i.extension, o.value, o.tx_id" ++
      fr"FROM node_inputs i JOIN node_outputs o ON i.box_id = o.box_id" ++
      fr"WHERE" ++ Fragments.in(fr"i.tx_id", txsId)).query[InputWithOutputInfo]

}
