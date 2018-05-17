package org.ergoplatform.explorer.db.models

case class Transaction(id: String, blockId: String, isCoinbase: Boolean) extends Entity[String]
