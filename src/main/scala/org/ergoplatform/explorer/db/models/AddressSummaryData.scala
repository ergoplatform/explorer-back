package org.ergoplatform.explorer.db.models

case class AddressSummaryData(hash: String, txsCount: Long, spent: Long, unspent: Long)
