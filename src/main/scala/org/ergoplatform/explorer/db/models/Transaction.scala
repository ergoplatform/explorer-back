package org.ergoplatform.explorer.db.models

final case class Transaction(
  id: String,
  headerId: String,
  isCoinbase: Boolean,
  timestamp: Long,
  size: Long
)
