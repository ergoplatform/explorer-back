package org.ergoplatform.explorer.db.models

case class SpentOutput(output: Output, spentTxId: Option[String])
