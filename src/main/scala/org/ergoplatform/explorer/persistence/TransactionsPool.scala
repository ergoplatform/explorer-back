package org.ergoplatform.explorer.persistence

import org.ergoplatform.explorer.grabber.protocol.ApiTransaction

/** Unconfirmed transactions pool.
  */
final case class TransactionsPool(
  txs: Map[String, ApiTransaction],
  indexes: Map[String, List[String]]
) {

  def put(txs: List[ApiTransaction]): TransactionsPool =
    copy(this.txs ++ txs.map(x => x.id -> x), this.indexes ++ extractIndexes(txs))

  def get(id: String): Option[ApiTransaction] = txs.get(id)

  def getAll: List[ApiTransaction] = txs.values.toList

  def getByErgoTree(ergoTree: String): List[ApiTransaction] =
    indexes
      .get(ergoTree)
      .toList
      .flatten
      .flatMap(get)

  private def extractIndexes(txs: List[ApiTransaction]): Map[String, List[String]] =
    txs
      .flatMap(tx => tx.outputs.map(_ -> tx.id))
      .foldLeft(Map.empty[String, List[String]]) {
        case (acc, (out, txId)) =>
          acc.updated(out.ergoTree, acc.get(out.ergoTree).toList.flatten :+ txId)
      }

}

object TransactionsPool {
  def empty: TransactionsPool = TransactionsPool(Map.empty, Map.empty)
}
