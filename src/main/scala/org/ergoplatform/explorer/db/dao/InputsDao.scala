package org.ergoplatform.explorer.db.dao

import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.db.models.Input
import org.ergoplatform.explorer.db.models.composite.ExtendedInput

class InputsDao extends JsonMeta {

  val fields: Seq[String] = InputsOps.fields

  def insert(i: Input): ConnectionIO[Input] =
    InputsOps.insert.withUniqueGeneratedKeys[Input](fields: _*)(i)

  def insertMany(list: List[Input]): ConnectionIO[List[Input]] =
    InputsOps.insert.updateManyWithGeneratedKeys[Input](fields: _*)(list).compile.to[List]

  def findAllByTxId(txId: String): ConnectionIO[List[Input]] =
    InputsOps.findAllByTxId(txId).to[List]

  def findAllByTxsId(txsId: List[String]): ConnectionIO[List[Input]] =
    NonEmptyList.fromList(txsId) match {
      case Some(ids) => InputsOps.findAllByTxsId(ids).to[List]
      case None      => List.empty[Input].pure[ConnectionIO]
    }

  def findAllByTxIdExtended(txId: String): ConnectionIO[List[ExtendedInput]] =
    InputsOps.findAllByTxIdWithValue(txId).to[List]

  def findAllByTxsIdWithValue(txsId: List[String]): ConnectionIO[List[ExtendedInput]] =
    NonEmptyList.fromList(txsId) match {
      case Some(ids) => InputsOps.findAllByTxsIdWithValue(ids).to[List]
      case None      => List.empty[ExtendedInput].pure[ConnectionIO]
    }

}
