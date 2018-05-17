package org.ergoplatform.explorer.db.models

case class Output(id: String, txId: String, value: Long, spent: Boolean, script: String) extends Entity[String]
