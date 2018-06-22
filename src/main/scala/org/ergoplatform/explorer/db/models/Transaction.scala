package org.ergoplatform.explorer.db.models

case class Transaction(id: String, headerId: String, isCoinbase: Boolean, timestamp: Long)
