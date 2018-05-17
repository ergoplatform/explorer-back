package org.ergoplatform.explorer.db.dao

import doobie.free.connection.ConnectionIO
import doobie.util.composite.Composite
import doobie.util.fragment.Fragment
import org.ergoplatform.explorer.db.models.Input

class InputsDao extends BaseDoobieDao[String, Input] {
  override def table: String = "inputs"

  override def fields: Seq[String] = Seq(
    "id",
    "tx_id",
    "output",
    "signature"
  )

  def findAllByTxId(txId: String)(implicit c: Composite[Input]): ConnectionIO[List[Input]] = {
    (selectAllFromFr ++ Fragment.const(s"WHERE tx_id = '$txId'")).query[Input].to[List]
  }

  def findAllByTxsId(txsId: List[String])(implicit c: Composite[Input]): ConnectionIO[List[Input]] = {
    val inList = txsId.map { v => "'" + v + "'" }.mkString("(", ", ", ")")
    (selectAllFromFr ++ Fragment.const(s"WHERE tx_id in $inList")).query[Input].to[List]
  }
}
