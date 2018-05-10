package org.ergoplatform.explorer.models

case class Output(id: String, txId: String, value: Long, script: String) extends Entity[String]
