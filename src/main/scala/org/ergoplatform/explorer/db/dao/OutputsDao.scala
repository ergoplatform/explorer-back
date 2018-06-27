package org.ergoplatform.explorer.db.dao

import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.db.models.{Output, SpentOutput}

class OutputsDao extends JsonMeta {

  val fields = OutputsOps.fields

  def insert(i: Output): ConnectionIO[Output] = OutputsOps.insert.withUniqueGeneratedKeys[Output](fields: _*)(i)

  def insertMany(list: List[Output]): ConnectionIO[List[Output]] =
    OutputsOps.insert.updateManyWithGeneratedKeys[Output](fields: _*)(list).compile.to[List]

  def findAllByTxId(txId: String): ConnectionIO[List[Output]] = OutputsOps.findAllByTxId(txId).to[List]

  def findAllByTxsId(txsId: List[String]): ConnectionIO[List[Output]] = NonEmptyList.fromList(txsId) match {
    case Some(ids) => OutputsOps.findAllByTxsId(ids).to[List]
    case None => List.empty[Output].pure[ConnectionIO]
  }

  def findAllByTxIdWithSpent(txId: String): ConnectionIO[List[SpentOutput]] =
    OutputsOps.findAllByTxIdWithSpent(txId).to[List]

  def findAllByTxsIdWithSpent(txsId: List[String]): ConnectionIO[List[SpentOutput]] =
    NonEmptyList.fromList(txsId) match {
      case Some(ids) => OutputsOps.findAllByTxsIdWithSpent(ids).to[List]
      case None => List.empty[SpentOutput].pure[ConnectionIO]
    }

  def findAllByAddressId(address: String)(implicit c: Composite[Output]): ConnectionIO[List[Output]] =
    OutputsOps.findByHash(address).to[List]

  /** Search address identifiers by the fragment of the identifier */
  def searchByAddressId(substring: String): ConnectionIO[List[String]] = {
    OutputsOps.searchByHash(substring).to[List]
  }

  def sumOfAllUnspentOutputs: ConnectionIO[Long] = OutputsOps.sumOfAllUnspentOutputs.unique

}
