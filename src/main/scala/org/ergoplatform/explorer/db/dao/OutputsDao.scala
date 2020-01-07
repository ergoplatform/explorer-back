package org.ergoplatform.explorer.db.dao

import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.db.models.Output
import org.ergoplatform.explorer.db.models.composite.ExtendedOutput

class OutputsDao extends JsonMeta {

  val fields: Seq[String] = OutputsOps.fields

  def insert(i: Output): ConnectionIO[Output] =
    OutputsOps.insert.withUniqueGeneratedKeys[Output](fields: _*)(i)

  def insertMany(list: List[Output]): ConnectionIO[List[Output]] =
    OutputsOps.insert.updateManyWithGeneratedKeys[Output](fields: _*)(list).compile.to[List]

  def findByBoxId(boxId: String): ConnectionIO[ExtendedOutput] =
    OutputsOps.findByBoxId(boxId).unique

  def findAllByTxId(txId: String): ConnectionIO[List[Output]] =
    OutputsOps.findAllByTxId(txId).to[List]

  def findAllByAddress(address: String): ConnectionIO[List[ExtendedOutput]] =
    OutputsOps.findByAddress(address).to[List]

  def findAllByErgoTree(ergoTree: String): ConnectionIO[List[ExtendedOutput]] =
    OutputsOps.findByErgoTree(ergoTree).to[List]

  def findAllByErgoTreeRoot(ergoTreeRoot: String): ConnectionIO[List[ExtendedOutput]] =
    OutputsOps.findByErgoTreeRoot(ergoTreeRoot).to[List]

  def findUnspentByAddress(address: String): ConnectionIO[List[ExtendedOutput]] =
    OutputsOps.findUnspentByAddress(address).to[List]

  def findUnspentByErgoTree(ergoTree: String): ConnectionIO[List[ExtendedOutput]] =
    OutputsOps.findUnspentByErgoTree(ergoTree).to[List]

  def findUnspentByErgoTreeRoot(ergoTreeRoot: String): ConnectionIO[List[ExtendedOutput]] =
    OutputsOps.findUnspentByErgoTreeRoot(ergoTreeRoot).to[List]

  def findAllByTxsId(txsId: List[String]): ConnectionIO[List[Output]] =
    NonEmptyList.fromList(txsId) match {
      case Some(ids) => OutputsOps.findAllByTxsId(ids).to[List]
      case None      => List.empty[Output].pure[ConnectionIO]
    }

  def findAllByTxIdExtended(txId: String): ConnectionIO[List[ExtendedOutput]] =
    OutputsOps.findAllByTxIdWithSpent(txId).to[List]

  def findAllByTxsIdWithSpent(txsId: List[String]): ConnectionIO[List[ExtendedOutput]] =
    NonEmptyList.fromList(txsId) match {
      case Some(ids) => OutputsOps.findAllByTxsIdWithSpent(ids).to[List]
      case None      => List.empty[ExtendedOutput].pure[ConnectionIO]
    }

  def findAllByAddressId(
    address: String
  )(implicit r: Read[ExtendedOutput]): ConnectionIO[List[ExtendedOutput]] =
    OutputsOps.findByAddressWithSpent(address).to[List]

  /** Search address identifiers by the fragment of the identifier
    */
  def searchByAddressId(substring: String): ConnectionIO[List[String]] =
    OutputsOps.searchByAddress(substring).to[List]

  def sumOfAllUnspentOutputsSince(ts: Long): ConnectionIO[Long] =
    OutputsOps.sumOfAllUnspentOutputsSince(ts).unique

  def estimateOutputSince(ts: Long)(genesisAddress: String): ConnectionIO[BigDecimal] =
    OutputsOps.estimatedOutputsSince(ts)(genesisAddress).unique

}
