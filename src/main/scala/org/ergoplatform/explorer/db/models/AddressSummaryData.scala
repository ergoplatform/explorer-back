package org.ergoplatform.explorer.db.models

import doobie.util.composite.Composite

case class AddressSummaryData(hash: String, txsCount: Long, spent: BigInt, unspent: Long)

object AddressSummaryData {

  implicit val c: Composite[AddressSummaryData] =
    Composite[(String, Long, BigDecimal, Long)].imap(
      (t: (String, Long, BigDecimal, Long)) => AddressSummaryData(t._1, t._2, t._3.toBigInt(), t._4))(
      (s: AddressSummaryData) => (s.hash, s.txsCount, BigDecimal.apply(s.spent), s.unspent))
}
