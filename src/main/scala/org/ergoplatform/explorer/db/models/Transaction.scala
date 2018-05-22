package org.ergoplatform.explorer.db.models

case class Transaction(id: String, blockId: String, isCoinbase: Boolean, timestamp: Long) extends Entity[String]
