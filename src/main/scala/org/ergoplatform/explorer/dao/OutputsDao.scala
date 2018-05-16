package org.ergoplatform.explorer.dao

import doobie.{Composite, ConnectionIO, Fragment}
import org.ergoplatform.explorer.models.Output
import org.specs2.specification.core.Fragments

class OutputsDao extends BaseDoobieDao[String, Output] {
  override def table: String = "outputs"

  override def fields: Seq[String] = Seq(
    "id",
    "tx_id",
    "value",
    "script"
  )

  def findAllByTxId(txId: String)(implicit c: Composite[Output]): ConnectionIO[List[Output]] = {
    (selectAllFromFr ++ Fragment.const(s"WHERE tx_id = '$txId'")).query[Output].to[List]
  }

  def findAllByTxsId(txsId: List[String])(implicit c: Composite[Output]): ConnectionIO[List[Output]] = {
    val inList = txsId.map { v => "'" + v + "'" }.mkString("(", ", ", ")")
    (selectAllFromFr ++ Fragment.const(s"WHERE tx_id in $inList")).query[Output].to[List]
  }
}
