package org.ergoplatform.explorer.db.dao

import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.models.Transaction

class TransactionsDao {

  val fields: Seq[String] = TransactionsOps.fields

  def insert(t: Transaction): ConnectionIO[Transaction] =
    TransactionsOps.insert.withUniqueGeneratedKeys[Transaction](fields: _*)(t)

  def insertMany(txs: List[Transaction]): ConnectionIO[List[Transaction]] =
    TransactionsOps.insert
      .updateManyWithGeneratedKeys[Transaction](fields: _*)(txs)
      .compile
      .to[List]

  def findAllByBlockId(blockId: String): ConnectionIO[List[Transaction]] =
    TransactionsOps.findAllByBlockId(blockId).to[List]

  def countTxsNumbersByBlocksIds(ids: List[String]): ConnectionIO[List[(String, Long)]] =
    NonEmptyList.fromList(ids) match {
      case Some(l) => TransactionsOps.countTxsNumbersByBlocksIds(l).to[List]
      case None    => List.empty[(String, Long)].pure[ConnectionIO]
    }

  def getTxsByAddressId(
    addressId: String,
    offset: Int = 0,
    limit: Int = 20
  ): ConnectionIO[List[Transaction]] =
    TransactionsOps.getTxsByAddressId(addressId, offset, limit).to[List]

  def countTxsByAddressId(addressId: String): ConnectionIO[Long] =
    TransactionsOps.countTxsByAddressId(addressId).unique

  def find(id: String): ConnectionIO[Option[Transaction]] =
    TransactionsOps.select(id).option

  def get(id: String): ConnectionIO[Transaction] = find(id).flatMap {
    case Some(t) => t.pure[ConnectionIO]
    case None =>
      doobie.free.connection.raiseError(
        new NoSuchElementException(s"Cannot find transaction with id = $id")
      )
  }

  /** Search transaction identifiers by the fragment of the identifier
    */
  def searchById(substring: String): ConnectionIO[List[String]] = {
    TransactionsOps.searchById(substring).to[List]
  }

  /** Get all transactions appeared in the main-chain after given height.
    */
  def getTxsSince(
    height: Int,
    offset: Int = 0,
    limit: Int = 20
  ): ConnectionIO[List[Transaction]] =
    TransactionsOps.getTxsSince(height, offset, limit).to[List]

  def countTxsSince(ts: Long): ConnectionIO[Long] =
    TransactionsOps.countTxsSince(ts).unique

  def txsHeights(ids: NonEmptyList[String]): ConnectionIO[List[(String, Long)]] =
    TransactionsOps.txsHeight(ids).to[List]

  def txHeight(id: String): ConnectionIO[Long] =
    TransactionsOps.txHeight(id).unique

}
