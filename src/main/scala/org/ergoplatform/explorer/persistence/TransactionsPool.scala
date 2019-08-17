package org.ergoplatform.explorer.persistence

import org.ergoplatform.explorer.grabber.protocol.ApiTransaction

/** Unconfirmed transactions pool.
  */
final case class TransactionsPool(txs: Map[String, ApiTransaction]) {

  def put(txs: List[ApiTransaction]): TransactionsPool =
    copy(this.txs ++ txs.map(x => x.id -> x))

  def get(id: String): Option[ApiTransaction] = txs.get(id)

  def getAll: List[ApiTransaction] = txs.values.toList

}

object TransactionsPool {
  def empty: TransactionsPool = TransactionsPool(Map.empty)
}
