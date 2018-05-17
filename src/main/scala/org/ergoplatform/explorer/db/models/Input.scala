package org.ergoplatform.explorer.db.models

case class Input(id: String, txId: String, outputId: String, signature: String) extends Entity[String]
