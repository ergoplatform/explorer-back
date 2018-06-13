package org.ergoplatform.explorer.db.dao

import cats.data._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.Input

object InputsOps {

  val fields: Seq[String] = Seq(
    "id",
    "tx_id",
    "output",
    "signature"
  )

  val insertSql = s"INSERT INTO inputs (id, tx_id, output, signature) VALUES (?, ?, ?, ?)"

  def findAllByTxId(txId: String)(implicit c: Composite[Input]): Query0[Input] =
    fr"SELECT id, tx_id, output, signature FROM inputs WHERE tx_id = $txId".query[Input]


  def findAllByTxsId(txsId: NonEmptyList[String])(implicit c: Composite[Input]): Query0[Input] =
    (fr"SELECT id, tx_id, output, signature FROM inputs WHERE" ++ Fragments.in(fr"tx_id", txsId)).query[Input]

  def insert: Update[Input] = Update[Input](insertSql)

}
